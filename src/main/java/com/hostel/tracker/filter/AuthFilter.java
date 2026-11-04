package com.hostel.tracker.filter;

import com.hostel.tracker.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Runs BEFORE every request reaches a servlet/controller.
 *  - public pages pass straight through
 *  - not logged in -> redirect to /login (or 401 JSON for the REST API)
 *  - warden-only pages are blocked for other roles
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/login", "/register", "/session-info", "/css/", "/js/", "/favicon", "/error", "/health", "/api/health");
    private static final List<String> WARDEN_ONLY = List.of("/admin/", "/reports");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (path.equals("/") || PUBLIC_PREFIXES.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            if (path.startsWith("/api/")) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Please log in first\"}");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        if (WARDEN_ONLY.stream().anyMatch(path::startsWith) && !user.isWarden()) {
            session.setAttribute("flash", "That page is only for the warden.");
            session.setAttribute("flashType", "error");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Pages with personal data must not be cached (Back button after logout shows nothing)
        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }
}
