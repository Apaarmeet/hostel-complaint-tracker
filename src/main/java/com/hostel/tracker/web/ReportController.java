package com.hostel.tracker.web;

import com.hostel.tracker.dao.ComplaintDao;
import com.hostel.tracker.model.Complaint;
import com.hostel.tracker.model.Status;
import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

/**
 * Unit 3.1: Spring MVC.
 *   Controller = this class, Model = the data we put in "model", View = /WEB-INF/jsp/reports.jsp
 * The DispatcherServlet receives GET /reports, calls this method, then renders the view it names.
 */
@Controller
public class ReportController {

    private final ComplaintDao complaintDao;
    private final ServletContext servletContext;

    public ReportController(ComplaintDao complaintDao, ServletContext servletContext) {
        this.complaintDao = complaintDao;
        this.servletContext = servletContext;
    }

    @GetMapping("/reports")
    @SuppressWarnings("unchecked")
    public String reports(Model model) {
        List<Complaint> all = complaintDao.findAll();

        // Per category, in the configured order
        Map<String, StatRow> byCategory = new LinkedHashMap<>();
        for (String cat : (List<String>) servletContext.getAttribute("categories")) byCategory.put(cat, new StatRow(cat));
        // Per staff member
        Map<String, StatRow> byStaff = new TreeMap<>();

        long totalHours = 0, resolved = 0;
        for (Complaint c : all) {
            byCategory.computeIfAbsent(c.getCategory(), StatRow::new).add(c);
            if (c.getAssignedTo() != null) byStaff.computeIfAbsent(c.getAssignedTo().getName(), StatRow::new).add(c);
            if (c.getStatus() == Status.RESOLVED) { resolved++; totalHours += c.getResolutionHours(); }
        }

        List<StatRow> categories = new ArrayList<>(byCategory.values());
        double maxAvg = categories.stream().mapToDouble(StatRow::getAvgHours).max().orElse(0);
        long maxTotal = categories.stream().mapToLong(StatRow::getTotal).max().orElse(0);

        List<Complaint> oldestPending = all.stream()
                .filter(c -> c.getStatus() != Status.RESOLVED)
                .sorted(Comparator.comparing(Complaint::getCreatedAt))
                .limit(5).toList();

        model.addAttribute("categories", categories);
        model.addAttribute("staffRows", byStaff.values());
        model.addAttribute("maxAvg", maxAvg);
        model.addAttribute("maxTotal", maxTotal);
        model.addAttribute("total", all.size());
        model.addAttribute("counts", complaintDao.countByStatus(null, null));
        model.addAttribute("overallAvg", resolved == 0 ? 0 : (double) totalHours / resolved);
        model.addAttribute("oldestPending", oldestPending);
        return "reports"; // logical view name -> /WEB-INF/jsp/reports.jsp
    }
}
