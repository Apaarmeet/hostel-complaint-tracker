package com.hostel.tracker.servlet;

import com.hostel.tracker.model.User;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.sql.SQLException;

/**
 * GET  /login -> show the form
 * POST /login -> check email + password with JDBC, start a session
 *
 * ServletConfig demo: "maxAttempts" is an init parameter that belongs ONLY to this servlet.
 * On failure we FORWARD back to the form (keeps the typed email, shows the error).
 * On success we REDIRECT to the dashboard (so refreshing does not re-submit the password).
 */
@WebServlet(urlPatterns = "/login",
        initParams = @WebInitParam(name = "maxAttempts", value = "5"))
public class LoginServlet extends BaseServlet {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private int maxAttempts;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); // stores config and calls init() in BaseServlet
        maxAttempts = Integer.parseInt(config.getInitParameter("maxAttempts"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (currentUser(req) != null) { redirect(req, resp, "/dashboard"); return; }
        render(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        HttpSession session = req.getSession();

        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        if (attempts == null) attempts = 0;
        if (attempts >= maxAttempts) {
            fail(req, resp, email, "Too many failed attempts (" + maxAttempts + "). Close the browser and try again later.");
            return;
        }

        User user;
        try {
            user = userDao.findByEmail(email);
        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }

        if (user == null || password == null || !encoder.matches(password, user.getPasswordHash())) {
            session.setAttribute("loginAttempts", attempts + 1);
            int left = maxAttempts - attempts - 1;
            fail(req, resp, email, "Wrong email or password. " + left + " attempt(s) left.");
            return;
        }

        // Prevent session fixation: throw away the old session and start a fresh one
        session.invalidate();
        HttpSession fresh = req.getSession(true);
        fresh.setAttribute("user", user);
        flash(req, "success", "Welcome back, " + user.getName() + "!");
        redirect(req, resp, "/dashboard");
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String email, String error)
            throws ServletException, IOException {
        req.setAttribute("error", error);
        req.setAttribute("email", email);
        render(req, resp, "login"); // RequestDispatcher.forward
    }
}
