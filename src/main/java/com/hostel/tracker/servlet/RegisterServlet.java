package com.hostel.tracker.servlet;

import com.hostel.tracker.model.Role;
import com.hostel.tracker.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

/** Student self-registration (JDBC INSERT). Wardens and staff are created by the system. */
@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, "register");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = trim(req.getParameter("name"));
        String email = trim(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String room = trim(req.getParameter("roomNo")).toUpperCase();
        String phone = trim(req.getParameter("phone"));

        String error = null;
        if (name.length() < 2) error = "Please enter your full name.";
        else if (!EMAIL.matcher(email).matches()) error = "Please enter a valid email address.";
        else if (password.length() < 6) error = "Password must be at least 6 characters.";
        else if (room.isEmpty()) error = "Room number is required.";
        else if (!phone.matches("\\d{10}")) error = "Phone number must be 10 digits.";

        try {
            if (error == null && userDao.findByEmail(email) != null) error = "An account with this email already exists.";
            if (error != null) {
                req.setAttribute("error", error);
                render(req, resp, "register");
                return;
            }
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setPasswordHash(encoder.encode(password));
            u.setRole(Role.STUDENT);
            u.setRoomNo(room);
            u.setPhone(phone);
            userDao.insert(u);
        } catch (SQLException e) {
            throw new ServletException("Could not register", e);
        }
        flash(req, "success", "Account created! Log in with " + email);
        redirect(req, resp, "/login");
    }
}
