package com.hostel.tracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * On a tiny free-tier server (0.1 CPU) the FIRST visit to each page is slow because the JVM
 * still has to load and prepare a lot of classes. Right after start-up this component quietly
 * visits every page once in a background thread, so real visitors get fast pages.
 */
@Component
public class StartupWarmup {

    private static final Logger log = LoggerFactory.getLogger(StartupWarmup.class);
    private final Environment env;

    public StartupWarmup(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        if ("false".equalsIgnoreCase(env.getProperty("WARMUP"))) return;
        Thread t = new Thread(this::run, "startup-warmup");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private void run() {
        String base = "http://localhost:" + env.getProperty("local.server.port", "8080");
        long start = System.currentTimeMillis();
        try {
            visit(base, null, null, "/", "/login", "/register", "/session-info");
            visit(base, "warden@hostel.edu", "warden123", "/dashboard", "/complaint?id=1", "/reports",
                    "/admin/users", "/profile", "/api-console", "/api/complaints", "/api/complaints/1?format=xml", "/api/stats");
            visit(base, "aarav@hostel.edu", "student123", "/dashboard", "/complaint?action=new", "/complaint?id=1");
            visit(base, "ramesh@hostel.edu", "staff123", "/dashboard");
            log.info("Warm-up finished in {} s", (System.currentTimeMillis() - start) / 1000);
        } catch (Exception e) {
            log.warn("Warm-up stopped: {}", e.toString());
        }
    }

    private void visit(String base, String email, String password, String... pages) throws Exception {
        HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(10)).build();
        if (email != null) {
            client.send(HttpRequest.newBuilder(URI.create(base + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofMinutes(3))
                    .POST(HttpRequest.BodyPublishers.ofString("email=" + email + "&password=" + password)).build(),
                    HttpResponse.BodyHandlers.discarding());
        }
        for (String p : pages) {
            client.send(HttpRequest.newBuilder(URI.create(base + p)).timeout(Duration.ofMinutes(3)).GET().build(),
                    HttpResponse.BodyHandlers.discarding());
        }
        // log out so the warm-up sessions do not stay in memory
        client.send(HttpRequest.newBuilder(URI.create(base + "/logout")).GET().build(), HttpResponse.BodyHandlers.discarding());
    }
}
