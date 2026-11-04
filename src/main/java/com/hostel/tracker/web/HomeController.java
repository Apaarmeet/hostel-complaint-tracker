package com.hostel.tracker.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    /** Landing page; logged-in users go straight to their dashboard. */
    @GetMapping("/")
    public String home(HttpSession session) {
        return session.getAttribute("user") != null ? "redirect:/dashboard" : "index";
    }

    /** Browser page for trying the REST API (GET/POST/PUT/DELETE) without Postman. */
    @GetMapping("/api-console")
    public String apiConsole() {
        return "api-console";
    }

    /** Used by Render's health check. */
    @GetMapping({"/health", "/api/health"})
    @ResponseBody
    public String health() {
        return "OK";
    }
}
