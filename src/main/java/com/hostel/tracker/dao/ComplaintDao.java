package com.hostel.tracker.dao;

import com.hostel.tracker.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Unit 2.2: Hibernate.
 * Insert, select, update and delete complaints through a Hibernate Session using HQL
 * (queries written against Java classes, not tables). Compare with UserDao (raw JDBC).
 */
@Repository
@Transactional
public class ComplaintDao {

    @PersistenceContext
    private EntityManager entityManager;

    /** The underlying Hibernate Session. */
    private Session session() {
        return entityManager.unwrap(Session.class);
    }

    /** INSERT: Hibernate writes the complaint and its first timeline entry in one go. */
    public Complaint create(String title, String description, String category, Priority priority,
                            String roomNo, long studentId) {
        User student = session().getReference(User.class, studentId);
        LocalDateTime now = LocalDateTime.now();
        Complaint c = new Complaint();
        c.setTitle(title);
        c.setDescription(description);
        c.setCategory(category);
        c.setPriority(priority);
        c.setRoomNo(roomNo);
        c.setStatus(Status.OPEN);
        c.setStudent(student);
        c.setCreatedAt(now);
        c.addUpdate(Status.OPEN, "Complaint raised", student, now);
        session().persist(c);
        return c;
    }

    /** SELECT one complaint together with its timeline. */
    @Transactional(readOnly = true)
    public Complaint findWithTimeline(long id) {
        return session().createQuery(
                        "select distinct c from Complaint c left join fetch c.updates where c.id = :id", Complaint.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    /** SELECT with optional filters (all filters may be null). */
    @Transactional(readOnly = true)
    public List<Complaint> search(Long studentId, Long staffId, Status status, String category) {
        StringBuilder hql = new StringBuilder("from Complaint c where 1 = 1");
        if (studentId != null) hql.append(" and c.student.id = :studentId");
        if (staffId != null)   hql.append(" and c.assignedTo.id = :staffId");
        if (status != null)    hql.append(" and c.status = :status");
        if (category != null)  hql.append(" and c.category = :category");
        hql.append(" order by c.createdAt desc");

        Query<Complaint> q = session().createQuery(hql.toString(), Complaint.class);
        if (studentId != null) q.setParameter("studentId", studentId);
        if (staffId != null)   q.setParameter("staffId", staffId);
        if (status != null)    q.setParameter("status", status);
        if (category != null)  q.setParameter("category", category);
        List<Complaint> list = new ArrayList<>(q.list());
        // Pending work first, resolved complaints at the bottom (stable sort keeps newest-first order)
        list.sort(Comparator.comparing(c -> c.getStatus() == Status.RESOLVED));
        return list;
    }

    @Transactional(readOnly = true)
    public List<Complaint> findAll() {
        return search(null, null, null, null);
    }

    /** UPDATE by the student: only allowed while the complaint is still OPEN. */
    public boolean editByStudent(long id, long studentId, String title, String description,
                                 String category, Priority priority) {
        Complaint c = session().get(Complaint.class, id);
        if (c == null || !c.getStudent().getId().equals(studentId) || c.getStatus() != Status.OPEN) return false;
        c.setTitle(title);
        c.setDescription(description);
        c.setCategory(category);
        c.setPriority(priority);
        return true; // no explicit update call: Hibernate "dirty checking" saves the changes on commit
    }

    /** Warden assigns a staff member -> IN_PROGRESS. */
    public boolean assign(long id, long staffId, long wardenId, String note) {
        Complaint c = session().get(Complaint.class, id);
        if (c == null || c.getStatus() == Status.RESOLVED) return false;
        User staff = session().get(User.class, staffId);
        if (staff == null || staff.getRole() != Role.STAFF) return false;
        LocalDateTime now = LocalDateTime.now();
        boolean reassign = c.getAssignedTo() != null;
        c.setAssignedTo(staff);
        c.setAssignedAt(now);
        c.setStatus(Status.IN_PROGRESS);
        String text = (reassign ? "Re-assigned to " : "Assigned to ") + staff.getName()
                + (note == null || note.isBlank() ? "" : " - " + note);
        c.addUpdate(Status.IN_PROGRESS, text, session().getReference(User.class, wardenId), now);
        return true;
    }

    /** Staff (or warden) marks the work as done -> RESOLVED. */
    public boolean resolve(long id, User by, String note) {
        Complaint c = session().get(Complaint.class, id);
        if (c == null || c.getStatus() != Status.IN_PROGRESS) return false;
        if (by.getRole() == Role.STAFF && (c.getAssignedTo() == null || !c.getAssignedTo().getId().equals(by.getId())))
            return false;
        LocalDateTime now = LocalDateTime.now();
        c.setStatus(Status.RESOLVED);
        c.setResolvedAt(now);
        c.setResolutionNote(note);
        c.addUpdate(Status.RESOLVED, note == null || note.isBlank() ? "Marked as resolved" : note,
                session().getReference(User.class, by.getId()), now);
        return true;
    }

    /** Student is not satisfied -> back to OPEN. */
    public boolean reopen(long id, long studentId, String reason) {
        Complaint c = session().get(Complaint.class, id);
        if (c == null || !c.getStudent().getId().equals(studentId) || c.getStatus() != Status.RESOLVED) return false;
        c.setStatus(Status.OPEN);
        c.setResolvedAt(null);
        c.setAssignedTo(null);
        c.setAssignedAt(null);
        c.addUpdate(Status.OPEN, "Re-opened by student: " + reason,
                session().getReference(User.class, studentId), LocalDateTime.now());
        return true;
    }

    /** DELETE: student can delete own OPEN complaint, warden can delete any (user == null means warden). */
    public boolean delete(long id, Long onlyIfStudentId) {
        Complaint c = session().get(Complaint.class, id);
        if (c == null) return false;
        if (onlyIfStudentId != null
                && (!c.getStudent().getId().equals(onlyIfStudentId) || c.getStatus() != Status.OPEN)) return false;
        session().remove(c); // cascade removes the timeline entries too
        return true;
    }

    /** Count of complaints per status, e.g. {OPEN=4, IN_PROGRESS=2, RESOLVED=9}. String keys so JSP EL can read counts['OPEN']. */
    @Transactional(readOnly = true)
    public Map<String, Long> countByStatus(Long studentId, Long staffId) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Status s : Status.values()) map.put(s.name(), 0L);
        for (Complaint c : search(studentId, staffId, null, null)) map.merge(c.getStatus().name(), 1L, Long::sum);
        return map;
    }

    /** Used only by DataSeeder to insert demo complaints with past timestamps. */
    public void persistSeed(Complaint c) {
        c.setStudent(session().getReference(User.class, c.getStudent().getId()));
        if (c.getAssignedTo() != null) c.setAssignedTo(session().getReference(User.class, c.getAssignedTo().getId()));
        for (ComplaintUpdate u : c.getUpdates())
            if (u.getUpdatedBy() != null) u.setUpdatedBy(session().getReference(User.class, u.getUpdatedBy().getId()));
        session().persist(c);
    }
}
