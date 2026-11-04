package com.hostel.tracker.servlet;

import com.hostel.tracker.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Warden-only user list. The table on this page is built with ResultSetMetaData:
 * the JSP does not know the column names in advance, it reads them from the query result.
 */
@WebServlet("/admin/users")
public class UsersServlet extends BaseServlet {

    private static final String REPORT_SQL =
            "SELECT u.id, u.name, u.email, u.role, u.room_no, u.speciality, "
          + "       COUNT(DISTINCT r.id) AS raised, COUNT(DISTINCT a.id) AS assigned "
          + "FROM users u "
          + "LEFT JOIN complaints r ON r.student_id = u.id "
          + "LEFT JOIN complaints a ON a.assigned_to = u.id "
          + "GROUP BY u.id, u.name, u.email, u.role, u.room_no, u.speciality "
          + "ORDER BY u.role, u.name";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("table", userDao.runReport(REPORT_SQL));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        render(req, resp, "users");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        Long id = parseLong(req.getParameter("id"));
        try {
            boolean ok = id != null && !id.equals(me.getId()) && userDao.delete(id);
            flash(req, ok ? "success" : "error",
                    ok ? "User #" + id + " deleted." : "Cannot delete: this user still has complaints (or is a warden).");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        redirect(req, resp, "/admin/users");
    }
}
