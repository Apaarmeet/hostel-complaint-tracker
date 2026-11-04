package com.hostel.tracker.servlet;

import com.hostel.tracker.model.Complaint;
import com.hostel.tracker.model.Role;
import com.hostel.tracker.model.Status;
import com.hostel.tracker.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * GET /dashboard?status=OPEN&category=Plumbing
 * Filters arrive as GET query parameters, so a filtered view can be bookmarked or shared.
 * What you see depends on your role:
 *   STUDENT -> your own complaints, WARDEN -> every complaint, STAFF -> work assigned to you
 */
@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);

        Status status = null;
        try { if (req.getParameter("status") != null && !req.getParameter("status").isBlank())
                  status = Status.valueOf(req.getParameter("status")); }
        catch (IllegalArgumentException ignored) { }
        String category = trim(req.getParameter("category"));
        if (category.isEmpty()) category = null;

        Long studentId = user.getRole() == Role.STUDENT ? user.getId() : null;
        Long staffId = user.getRole() == Role.STAFF ? user.getId() : null;

        List<Complaint> complaints = complaintDao.search(studentId, staffId, status, category);
        req.setAttribute("complaints", complaints);
        req.setAttribute("counts", complaintDao.countByStatus(studentId, staffId));
        req.setAttribute("statuses", Status.values());
        req.setAttribute("selectedStatus", status == null ? "" : status.name());
        req.setAttribute("selectedCategory", category == null ? "" : category);

        if (user.getRole() == Role.WARDEN) {
            try {
                req.setAttribute("staffCount", userDao.findByRole(Role.STAFF).size());
                req.setAttribute("studentCount", userDao.findByRole(Role.STUDENT).size());
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
        render(req, resp, "dashboard");
    }
}
