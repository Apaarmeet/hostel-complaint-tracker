package com.hostel.tracker.model;

/** Complaint life cycle: OPEN -> IN_PROGRESS -> RESOLVED */
public enum Status {
    OPEN("Open"), IN_PROGRESS("In Progress"), RESOLVED("Resolved");

    private final String label;
    Status(String label) { this.label = label; }
    public String getLabel() { return label; }
}
