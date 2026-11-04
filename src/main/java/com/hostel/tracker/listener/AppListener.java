package com.hostel.tracker.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Application-wide setup.
 *  - ServletContextListener: when the app starts, read the "categories" context parameter
 *    (shared by ALL servlets, unlike ServletConfig which belongs to ONE servlet) and keep it
 *    in application scope.
 *  - HttpSessionListener: counts live sessions, shown in the footer via the JSP "application" object.
 */
@WebListener
public class AppListener implements ServletContextListener, HttpSessionListener {

    private static final String DEFAULT_CATEGORIES =
            "Electricity,Plumbing,Wi-Fi / Internet,Furniture,Cleaning,Water Cooler,Other";
    private final AtomicInteger activeSessions = new AtomicInteger();

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext app = event.getServletContext();
        String raw = app.getInitParameter("categories");
        if (raw == null || raw.isBlank()) raw = DEFAULT_CATEGORIES;
        List<String> categories = Arrays.stream(raw.split(",")).map(String::trim).toList();
        app.setAttribute("categories", categories);
        String hostel = app.getInitParameter("hostelName");
        app.setAttribute("hostelName", hostel == null ? "Hostel" : hostel);
        app.setAttribute("activeSessions", activeSessions);
        app.log("Loaded " + categories.size() + " complaint categories");
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions.updateAndGet(n -> Math.max(0, n - 1));
    }
}
