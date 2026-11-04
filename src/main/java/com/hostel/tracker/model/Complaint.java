package com.hostel.tracker.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** A maintenance complaint, persisted with Hibernate. */
@Entity
@Table(name = "complaints")
public class Complaint {

    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String category;
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "room_no")
    private String roomNo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "resolution_note")
    private String resolutionNote;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /** Status history shown as a timeline. Saved/deleted together with the complaint (cascade). */
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ComplaintUpdate> updates = new ArrayList<>();

    public Complaint() { }

    /** Adds an entry to the timeline. */
    public void addUpdate(Status status, String note, User by, LocalDateTime at) {
        ComplaintUpdate u = new ComplaintUpdate();
        u.setComplaint(this);
        u.setStatus(status);
        u.setNote(note);
        u.setUpdatedBy(by);
        u.setCreatedAt(at);
        updates.add(u);
    }

    /** Hours taken from complaint to resolution (null while unresolved). */
    public Long getResolutionHours() {
        if (resolvedAt == null || createdAt == null) return null;
        return Duration.between(createdAt, resolvedAt).toHours();
    }

    /** Human friendly age, e.g. "3 h ago" / "2 d ago". */
    public String getAge() {
        Duration d = Duration.between(createdAt, LocalDateTime.now());
        if (d.toMinutes() < 60) return Math.max(1, d.toMinutes()) + " min ago";
        if (d.toHours() < 48) return d.toHours() + " h ago";
        return d.toDays() + " d ago";
    }

    public String getCreatedAtText()  { return createdAt == null ? "" : createdAt.format(FMT); }
    public String getAssignedAtText() { return assignedAt == null ? "" : assignedAt.format(FMT); }
    public String getResolvedAtText() { return resolvedAt == null ? "" : resolvedAt.format(FMT); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public List<ComplaintUpdate> getUpdates() { return updates; }
    public void setUpdates(List<ComplaintUpdate> updates) { this.updates = updates; }
}
