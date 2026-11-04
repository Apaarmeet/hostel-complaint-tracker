package com.hostel.tracker.api;

import com.hostel.tracker.api.Dtos.*;
import com.hostel.tracker.dao.ComplaintDao;
import com.hostel.tracker.model.*;
import com.hostel.tracker.web.StatRow;
import jakarta.servlet.ServletContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Unit 3.2: RESTful web service.
 *
 *   GET    /api/complaints              list (filters: ?status=OPEN&category=Plumbing)
 *   GET    /api/complaints/{id}         one complaint with its timeline
 *   POST   /api/complaints              create   (student)
 *   PUT    /api/complaints/{id}         edit     (student, while OPEN)
 *   PUT    /api/complaints/{id}/assign  assign to staff (warden)
 *   PUT    /api/complaints/{id}/resolve mark resolved   (assigned staff / warden)
 *   DELETE /api/complaints/{id}         delete
 *   GET    /api/stats                   numbers for dashboards
 *
 * Add ?format=xml to any GET to receive XML instead of JSON.
 * Authentication re-uses the normal login session (the JSESSIONID cookie).
 */
@RestController
@RequestMapping("/api")
public class ComplaintApiController {

    private final ComplaintDao complaintDao;
    private final ServletContext servletContext;

    public ComplaintApiController(ComplaintDao complaintDao, ServletContext servletContext) {
        this.complaintDao = complaintDao;
        this.servletContext = servletContext;
    }

    @GetMapping("/complaints")
    public ComplaintList list(@SessionAttribute("user") User user,
                              @RequestParam(required = false) Status status,
                              @RequestParam(required = false) String category) {
        List<ComplaintDto> items = complaintDao
                .search(user.isStudent() ? user.getId() : null, user.isStaff() ? user.getId() : null, status, category)
                .stream().map(c -> ComplaintDto.of(c, false)).toList();
        return new ComplaintList(items.size(), items);
    }

    @GetMapping("/complaints/{id}")
    public ResponseEntity<?> one(@SessionAttribute("user") User user, @PathVariable long id) {
        Complaint c = complaintDao.findWithTimeline(id);
        if (c == null || !visible(user, c)) return error(HttpStatus.NOT_FOUND, "Complaint " + id + " not found");
        return ResponseEntity.ok(ComplaintDto.of(c, true));
    }

    @PostMapping("/complaints")
    public ResponseEntity<?> create(@SessionAttribute("user") User user, @RequestBody ComplaintRequest body) {
        if (!user.isStudent()) return error(HttpStatus.FORBIDDEN, "Only students can raise complaints");
        String problem = validate(body);
        if (problem != null) return error(HttpStatus.BAD_REQUEST, problem);
        Complaint c = complaintDao.create(body.title().trim(), body.description().trim(), body.category(),
                Priority.valueOf(body.priority()), user.getRoomNo(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ComplaintDto.of(complaintDao.findWithTimeline(c.getId()), true));
    }

    @PutMapping("/complaints/{id}")
    public ResponseEntity<?> update(@SessionAttribute("user") User user, @PathVariable long id,
                                    @RequestBody ComplaintRequest body) {
        String problem = validate(body);
        if (problem != null) return error(HttpStatus.BAD_REQUEST, problem);
        boolean ok = user.isStudent() && complaintDao.editByStudent(id, user.getId(), body.title().trim(),
                body.description().trim(), body.category(), Priority.valueOf(body.priority()));
        return ok ? ResponseEntity.ok(ComplaintDto.of(complaintDao.findWithTimeline(id), true))
                  : error(HttpStatus.CONFLICT, "Only your own OPEN complaints can be edited");
    }

    @PutMapping("/complaints/{id}/assign")
    public ResponseEntity<?> assign(@SessionAttribute("user") User user, @PathVariable long id,
                                    @RequestBody AssignRequest body) {
        if (!user.isWarden()) return error(HttpStatus.FORBIDDEN, "Only the warden can assign complaints");
        boolean ok = body.staffId() != null && complaintDao.assign(id, body.staffId(), user.getId(), body.note());
        return ok ? ResponseEntity.ok(ComplaintDto.of(complaintDao.findWithTimeline(id), true))
                  : error(HttpStatus.CONFLICT, "Could not assign (check staffId, or complaint already resolved)");
    }

    @PutMapping("/complaints/{id}/resolve")
    public ResponseEntity<?> resolve(@SessionAttribute("user") User user, @PathVariable long id,
                                     @RequestBody(required = false) ResolveRequest body) {
        if (user.isStudent()) return error(HttpStatus.FORBIDDEN, "Students cannot resolve complaints");
        boolean ok = complaintDao.resolve(id, user, body == null ? null : body.note());
        return ok ? ResponseEntity.ok(ComplaintDto.of(complaintDao.findWithTimeline(id), true))
                  : error(HttpStatus.CONFLICT, "Complaint must be In Progress (and assigned to you)");
    }

    @DeleteMapping("/complaints/{id}")
    public ResponseEntity<?> delete(@SessionAttribute("user") User user, @PathVariable long id) {
        if (user.isStaff()) return error(HttpStatus.FORBIDDEN, "Staff cannot delete complaints");
        boolean ok = complaintDao.delete(id, user.isWarden() ? null : user.getId());
        return ok ? ResponseEntity.ok(new Message("Complaint " + id + " deleted"))
                  : error(HttpStatus.CONFLICT, "Not found, or not an OPEN complaint of yours");
    }

    @GetMapping("/stats")
    public Stats stats(@SessionAttribute("user") User user) {
        List<Complaint> all = complaintDao.search(user.isStudent() ? user.getId() : null,
                user.isStaff() ? user.getId() : null, null, null);
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Status s : Status.values()) byStatus.put(s.name(), 0L);
        Map<String, StatRow> rows = new LinkedHashMap<>();
        for (Complaint c : all) {
            byStatus.merge(c.getStatus().name(), 1L, Long::sum);
            rows.computeIfAbsent(c.getCategory(), StatRow::new).add(c);
        }
        Map<String, Long> byCategory = new LinkedHashMap<>();
        Map<String, Double> avg = new LinkedHashMap<>();
        rows.forEach((k, r) -> {
            byCategory.put(k, r.getTotal());
            if (r.getResolved() > 0) avg.put(k, Math.round(r.getAvgHours() * 10) / 10.0);
        });
        return new Stats(all.size(), byStatus, byCategory, avg);
    }

    // ---- helpers ----

    private boolean visible(User u, Complaint c) {
        return switch (u.getRole()) {
            case WARDEN -> true;
            case STUDENT -> c.getStudent().getId().equals(u.getId());
            case STAFF -> c.getAssignedTo() != null && c.getAssignedTo().getId().equals(u.getId());
        };
    }

    @SuppressWarnings("unchecked")
    private String validate(ComplaintRequest b) {
        if (b == null || b.title() == null || b.title().trim().length() < 5) return "title must be at least 5 characters";
        if (b.description() == null || b.description().trim().length() < 10) return "description must be at least 10 characters";
        List<String> categories = (List<String>) servletContext.getAttribute("categories");
        if (!categories.contains(b.category())) return "category must be one of " + categories;
        try { Priority.valueOf(b.priority()); } catch (Exception e) { return "priority must be LOW, MEDIUM or HIGH"; }
        return null;
    }

    private ResponseEntity<Message> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new Message(message));
    }
}
