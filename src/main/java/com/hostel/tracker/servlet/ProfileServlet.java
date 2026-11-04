package com.hostel.tracker.servlet;

import com.hostel.tracker.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** View and update your own profile (JDBC UPDATE). */
@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, "profile");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        String name = trim(req.getParameter("name"));
        String phone = trim(req.getParameter("phone"));
        String room = me.isStudent() ? trim(req.getParameter("roomNo")).toUpperCase() : me.getRoomNo();
        if (name.length() < 2 || !phone.matches("\\d{10}") || (me.isStudent() && room.isEmpty())) {
            req.setAttribute("error", "Name, a 10-digit phone number and room number are required.");
            render(req, resp, "profile");
            return;
        }
        try {
            userDao.updateProfile(me.getId(), name, phone, room);
            req.getSession().setAttribute("user", userDao.findById(me.getId())); // refresh session copy
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        flash(req, "success", "Profile updated.");
        redirect(req, resp, "/profile");
    }
}
