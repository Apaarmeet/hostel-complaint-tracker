package com.hostel.tracker.servlet;

import com.hostel.tracker.dao.ComplaintDao;
import com.hostel.tracker.dao.UserDao;
import com.hostel.tracker.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

/**
 * Common helpers for all our servlets.
 *
 * Servlet life cycle: the container calls init() once, service() -> doGet()/doPost() for every
 * request, and destroy() once at shutdown. We use init() to look up the DAOs.
 */
public abstract class BaseServlet extends HttpServlet {

    protected UserDao userDao;
    protected ComplaintDao complaintDao;

    @Override
    public void init() throws ServletException {
        // Servlets are created by Tomcat, not Spring, so we fetch the DAOs from the Spring
        // context that Spring Boot stored inside the ServletContext.
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        userDao = ctx.getBean(UserDao.class);
        complaintDao = ctx.getBean(ComplaintDao.class);
        log(getServletName() + " initialised");
    }

    @Override
    public void destroy() {
        log(getServletName() + " destroyed");
    }

    /** The logged-in user stored in the HttpSession at login (null if not logged in). */
    protected User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute("user");
    }

    /** Server-side forward to a JSP: same request, URL in the browser does not change. */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String page)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/" + page + ".jsp").forward(req, resp);
    }

    /** Client-side redirect: the browser makes a NEW request to the given URL. */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** One-time message shown on the next page (stored in the session, removed once displayed). */
    protected void flash(HttpServletRequest req, String type, String message) {
        req.getSession().setAttribute("flash", message);
        req.getSession().setAttribute("flashType", type);
    }

    protected static Long parseLong(String s) {
        try { return s == null || s.isBlank() ? null : Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    protected static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
