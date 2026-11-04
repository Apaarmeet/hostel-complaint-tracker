package com.hostel.tracker.seed;

import com.hostel.tracker.dao.ComplaintDao;
import com.hostel.tracker.dao.UserDao;
import com.hostel.tracker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fills an empty database with demo accounts and realistic complaints so the dashboard
 * and reports have something to show on first launch. Runs only once (skips if the warden exists).
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final UserDao userDao;
    private final ComplaintDao complaintDao;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Map<String, User> users = new HashMap<>();

    public DataSeeder(UserDao userDao, ComplaintDao complaintDao) {
        this.userDao = userDao;
        this.complaintDao = complaintDao;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userDao.findByEmail("warden@hostel.edu") != null) return;
        log.info("Empty database: inserting demo data");

        user("warden", "Mr. R. K. Sharma", Role.WARDEN, "warden123", "Office", "9810000001", null);
        user("ramesh", "Ramesh Kumar", Role.STAFF, "staff123", null, "9810000011", "Electricity");
        user("suresh", "Suresh Yadav", Role.STAFF, "staff123", null, "9810000012", "Plumbing");
        user("anil", "Anil Verma", Role.STAFF, "staff123", null, "9810000013", "Wi-Fi / Internet");
        user("kavita", "Kavita Devi", Role.STAFF, "staff123", null, "9810000014", "Cleaning & Furniture");
        user("aarav", "Aarav Mehta", Role.STUDENT, "student123", "A-101", "9876500001", null);
        user("priya", "Priya Nair", Role.STUDENT, "student123", "B-204", "9876500002", null);
        user("rohan", "Rohan Singh", Role.STUDENT, "student123", "A-305", "9876500003", null);
        user("sneha", "Sneha Iyer", Role.STUDENT, "student123", "B-112", "9876500004", null);

        // title, description, category, priority, student, age(h), staff, assignedAfter(h), resolvedAfter(h), note
        c("Tube light flickering", "The tube light in my room keeps flickering at night and makes a buzzing sound.",
                "Electricity", Priority.MEDIUM, "aarav", 80, "ramesh", 1, 5, "Replaced the choke and the tube light.");
        c("Bathroom tap leaking", "The tap in the 2nd floor B-wing bathroom leaks all the time. Water is being wasted.",
                "Plumbing", Priority.HIGH, "priya", 120, "suresh", 2, 22, "Replaced the washer and the tap spindle.");
        c("No Wi-Fi signal on 3rd floor", "Wi-Fi does not reach rooms A-301 to A-310. Cannot attend online classes.",
                "Wi-Fi / Internet", Priority.HIGH, "rohan", 100, "anil", 3, 34, "Installed an extra access point near the stairs.");
        c("Study table drawer broken", "The drawer of my study table has come off its rail and does not close.",
                "Furniture", Priority.LOW, "aarav", 200, "kavita", 10, 70, "Drawer rail replaced.");
        c("Corridor not cleaned", "The B-wing corridor has not been swept for two days.",
                "Cleaning", Priority.MEDIUM, "priya", 50, "kavita", 1, 4, "Corridor cleaned, added to the daily roster.");
        c("Water cooler not cooling", "The water cooler on the 2nd floor gives warm water since yesterday.",
                "Water Cooler", Priority.HIGH, "rohan", 70, "ramesh", 2, 12, "Compressor relay replaced, cooling fine now.");
        c("Fan regulator not working", "Fan runs only at full speed, regulator knob does nothing.",
                "Electricity", Priority.MEDIUM, "sneha", 30, "ramesh", 1, 7, "New regulator fitted.");
        c("Dustbins overflowing near mess", "The dustbins outside the mess have not been emptied and smell bad.",
                "Cleaning", Priority.LOW, "rohan", 15, "kavita", 1, 3, "Bins emptied.");
        c("No hot water in shower", "The geyser in the A-wing ground floor bathroom is not heating water.",
                "Plumbing", Priority.HIGH, "aarav", 20, "suresh", 2, -1, null);
        c("Wi-Fi disconnects every 10 minutes", "Connection drops every 10 minutes and I have to log in again.",
                "Wi-Fi / Internet", Priority.MEDIUM, "priya", 10, "anil", 1, -1, null);
        c("Window glass cracked", "The window glass in my room is cracked and cold air comes in at night.",
                "Other", Priority.MEDIUM, "aarav", 48, "kavita", 5, -1, null);
        c("Power socket sparking", "The socket next to my bed sparks when I plug in my charger. Feels dangerous.",
                "Electricity", Priority.HIGH, "rohan", 3, null, 0, -1, null);
        c("Cupboard door hinge loose", "The cupboard door is hanging from one hinge.",
                "Furniture", Priority.LOW, "sneha", 26, null, 0, -1, null);
        c("Washroom drain blocked", "Water is collecting on the floor of the B-wing washroom, drain is blocked.",
                "Plumbing", Priority.HIGH, "sneha", 6, null, 0, -1, null);

        log.info("Demo data ready: 9 users, 14 complaints");
    }

    private void user(String key, String name, Role role, String password, String room, String phone, String speciality)
            throws Exception {
        User u = new User();
        u.setName(name);
        u.setEmail(key + "@hostel.edu");
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role);
        u.setRoomNo(room);
        u.setPhone(phone);
        u.setSpeciality(speciality);
        u.setId(userDao.insert(u));
        users.put(key, u);
    }

    private void c(String title, String desc, String category, Priority priority, String student, int ageHours,
                   String staff, int assignedAfter, int resolvedAfter, String note) {
        LocalDateTime created = LocalDateTime.now().minusHours(ageHours).withSecond(0).withNano(0);
        User s = users.get(student);
        Complaint c = new Complaint();
        c.setTitle(title);
        c.setDescription(desc);
        c.setCategory(category);
        c.setPriority(priority);
        c.setRoomNo(s.getRoomNo());
        c.setStudent(s);
        c.setCreatedAt(created);
        c.setStatus(Status.OPEN);
        c.addUpdate(Status.OPEN, "Complaint raised", s, created);
        if (staff != null) {
            User st = users.get(staff);
            LocalDateTime assigned = created.plusHours(assignedAfter);
            c.setAssignedTo(st);
            c.setAssignedAt(assigned);
            c.setStatus(Status.IN_PROGRESS);
            c.addUpdate(Status.IN_PROGRESS, "Assigned to " + st.getName(), users.get("warden"), assigned);
            if (resolvedAfter > 0) {
                LocalDateTime resolved = created.plusHours(resolvedAfter);
                c.setResolvedAt(resolved);
                c.setResolutionNote(note);
                c.setStatus(Status.RESOLVED);
                c.addUpdate(Status.RESOLVED, note, st, resolved);
            }
        }
        complaintDao.persistSeed(c);
    }
}
