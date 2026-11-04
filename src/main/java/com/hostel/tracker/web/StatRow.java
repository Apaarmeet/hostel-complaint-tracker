package com.hostel.tracker.web;

/** One row of the reports page (per category or per staff member). JavaBean getters for JSP EL. */
public class StatRow {
    private final String name;
    private long total;
    private long open;
    private long inProgress;
    private long resolved;
    private long totalHours;

    public StatRow(String name) { this.name = name; }

    public void add(com.hostel.tracker.model.Complaint c) {
        total++;
        switch (c.getStatus()) {
            case OPEN -> open++;
            case IN_PROGRESS -> inProgress++;
            case RESOLVED -> { resolved++; totalHours += c.getResolutionHours(); }
        }
    }

    public String getName() { return name; }
    public long getTotal() { return total; }
    public long getOpen() { return open; }
    public long getInProgress() { return inProgress; }
    public long getResolved() { return resolved; }
    /** Average hours from complaint to fix (only resolved complaints count). */
    public double getAvgHours() { return resolved == 0 ? 0 : (double) totalHours / resolved; }
    public long getResolvedPercent() { return total == 0 ? 0 : Math.round(resolved * 100.0 / total); }
}
