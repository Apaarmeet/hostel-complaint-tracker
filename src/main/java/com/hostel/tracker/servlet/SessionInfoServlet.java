package com.hostel.tracker.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Date;

/**
 * Experiment 2: HttpSession creation time and last-accessed time.
 * request.getSession(true) creates a new session if the request does not already have one.
 */
@WebServlet("/session-info")
public class SessionInfoServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);

        Integer visits = (Integer) session.getAttribute("visitCount");
        visits = visits == null ? 1 : visits + 1;
        session.setAttribute("visitCount", visits);

        req.setAttribute("isNew", session.isNew());
        req.setAttribute("sessionId", session.getId());
        req.setAttribute("created", new Date(session.getCreationTime()));
        req.setAttribute("lastAccessed", new Date(session.getLastAccessedTime()));
        req.setAttribute("maxInactive", session.getMaxInactiveInterval());
        req.setAttribute("visits", visits);
        render(req, resp, "session-info");
    }
}
