package com.hostel.tracker.servlet;

import com.hostel.tracker.model.*;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Everything about a single complaint.
 *
 * GET  /complaint?id=7              view details + timeline
 * GET  /complaint?action=new        "raise complaint" form   (student)
 * GET  /complaint?action=edit&id=7  edit form                (student, only while OPEN)
 * POST /complaint  action=create | update | delete | assign | resolve | reopen
 *
 * After every POST we redirect (Post/Redirect/Get) so pressing F5 never submits twice.
 */
@WebServlet(urlPatterns = "/complaint",
        initParams = {
                @WebInitParam(name = "maxTitleLength", value = "120"),
                @WebInitParam(name = "maxDescriptionLength", value = "1000")
        })
public class ComplaintServlet extends BaseServlet {

    private int maxTitle;
    private int maxDescription;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        maxTitle = Integer.parseInt(config.getInitParameter("maxTitleLength"));
        maxDescription = Integer.parseInt(config.getInitParameter("maxDescriptionLength"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String action = trim(req.getParameter("action"));
        Long id = parseLong(req.getParameter("id"));
        req.setAttribute("maxTitle", maxTitle);
        req.setAttribute("maxDescription", maxDescription);
        req.setAttribute("priorities", Priority.values());

        if ("new".equals(action)) {
            if (!user.isStudent()) { denied(req, resp); return; }
            render(req, resp, "complaint-form");
            return;
        }

        Complaint c = id == null ? null : complaintDao.findWithTimeline(id);
        if (c == null || !canView(user, c)) {
            flash(req, "error", "Complaint not found.");
            redirect(req, resp, "/dashboard");
            return;
        }

        if ("edit".equals(action)) {
            if (!user.isStudent() || c.getStatus() != Status.OPEN) { denied(req, resp); return; }
            req.setAttribute("complaint", c);
            render(req, resp, "complaint-form");
            return;
        }

        req.setAttribute("complaint", c);
        if (user.isWarden()) {
            try {
                req.setAttribute("staff", userDao.findByRole(Role.STAFF));
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
        render(req, resp, "complaint");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String action = trim(req.getParameter("action"));
        Long id = parseLong(req.getParameter("id"));

        switch (action) {
            case "create" -> {
                if (!user.isStudent()) { denied(req, resp); return; }
                String error = validate(req);
                if (error != null) { backToForm(req, resp, error, null); return; }
                Complaint c = complaintDao.create(trim(req.getParameter("title")),
                        trim(req.getParameter("description")), req.getParameter("category"),
                        Priority.valueOf(req.getParameter("priority")), user.getRoomNo(), user.getId());
                flash(req, "success", "Complaint #" + c.getId() + " raised. The warden has been notified.");
                redirect(req, resp, "/complaint?id=" + c.getId());
            }
            case "update" -> {
                String error = validate(req);
                if (error != null) { backToForm(req, resp, error, id); return; }
                boolean ok = id != null && user.isStudent() && complaintDao.editByStudent(id, user.getId(),
                        trim(req.getParameter("title")), trim(req.getParameter("description")),
                        req.getParameter("category"), Priority.valueOf(req.getParameter("priority")));
                done(req, resp, ok, "Complaint updated.", "This complaint can no longer be edited.", "/complaint?id=" + id);
            }
            case "delete" -> {
                boolean ok = id != null && (user.isWarden() || user.isStudent())
                        && complaintDao.delete(id, user.isWarden() ? null : user.getId());
                done(req, resp, ok, "Complaint #" + id + " deleted.", "Only OPEN complaints can be deleted.", "/dashboard");
            }
            case "assign" -> {
                Long staffId = parseLong(req.getParameter("staffId"));
                boolean ok = id != null && staffId != null && user.isWarden()
                        && complaintDao.assign(id, staffId, user.getId(), trim(req.getParameter("note")));
                done(req, resp, ok, "Complaint assigned. Status is now In Progress.", "Could not assign this complaint.",
                        "/complaint?id=" + id);
            }
            case "resolve" -> {
                boolean ok = id != null && (user.isStaff() || user.isWarden())
                        && complaintDao.resolve(id, user, trim(req.getParameter("note")));
                done(req, resp, ok, "Marked as resolved. Nice work!", "Could not resolve this complaint.",
                        "/complaint?id=" + id);
            }
            case "reopen" -> {
                String reason = trim(req.getParameter("reason"));
                boolean ok = id != null && user.isStudent() && !reason.isEmpty()
                        && complaintDao.reopen(id, user.getId(), reason);
                done(req, resp, ok, "Complaint re-opened. The warden will re-assign it.",
                        "Please give a reason to re-open.", "/complaint?id=" + id);
            }
            default -> redirect(req, resp, "/dashboard");
        }
    }

    private boolean canView(User u, Complaint c) {
        return switch (u.getRole()) {
            case WARDEN -> true;
            case STUDENT -> c.getStudent().getId().equals(u.getId());
            case STAFF -> c.getAssignedTo() != null && c.getAssignedTo().getId().equals(u.getId());
        };
    }

    @SuppressWarnings("unchecked")
    private String validate(HttpServletRequest req) {
        String title = trim(req.getParameter("title"));
        String desc = trim(req.getParameter("description"));
        List<String> categories = (List<String>) getServletContext().getAttribute("categories");
        if (title.length() < 5) return "Title should be at least 5 characters.";
        if (title.length() > maxTitle) return "Title can be at most " + maxTitle + " characters.";
        if (desc.length() < 10) return "Please describe the problem (at least 10 characters).";
        if (desc.length() > maxDescription) return "Description can be at most " + maxDescription + " characters.";
        if (!categories.contains(req.getParameter("category"))) return "Please choose a category.";
        try { Priority.valueOf(req.getParameter("priority")); }
        catch (Exception e) { return "Please choose a priority."; }
        return null;
    }

    private void backToForm(HttpServletRequest req, HttpServletResponse resp, String error, Long id)
            throws ServletException, IOException {
        req.setAttribute("error", error);
        req.setAttribute("formId", id);
        req.setAttribute("maxTitle", maxTitle);
        req.setAttribute("maxDescription", maxDescription);
        req.setAttribute("priorities", Priority.values());
        render(req, resp, "complaint-form"); // forward keeps what the user typed (param.*)
    }

    private void done(HttpServletRequest req, HttpServletResponse resp, boolean ok,
                      String success, String failure, String next) throws IOException {
        flash(req, ok ? "success" : "error", ok ? success : failure);
        redirect(req, resp, next);
    }

    private void denied(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        flash(req, "error", "You are not allowed to do that.");
        redirect(req, resp, "/dashboard");
    }
}
