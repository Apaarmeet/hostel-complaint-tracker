package com.hostel.tracker.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.hostel.tracker.model.Complaint;
import com.hostel.tracker.model.ComplaintUpdate;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Objects: the exact shape of the JSON / XML the REST API sends and receives.
 * We never expose the Hibernate entities directly (they contain password hashes, lazy collections...).
 */
public final class Dtos {

    private Dtos() { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JacksonXmlRootElement(localName = "complaint")
    public record ComplaintDto(Long id, String title, String description, String category, String priority,
                               String status, String roomNo, String raisedBy, String assignedTo,
                               String createdAt, String resolvedAt, Long resolutionHours,
                               @JacksonXmlElementWrapper(localName = "timeline")
                               @JacksonXmlProperty(localName = "event")
                               List<TimelineDto> timeline) {

        public static ComplaintDto of(Complaint c, boolean withTimeline) {
            return new ComplaintDto(c.getId(), c.getTitle(), c.getDescription(), c.getCategory(),
                    c.getPriority().name(), c.getStatus().name(), c.getRoomNo(), c.getStudent().getName(),
                    c.getAssignedTo() == null ? null : c.getAssignedTo().getName(),
                    String.valueOf(c.getCreatedAt()), c.getResolvedAt() == null ? null : String.valueOf(c.getResolvedAt()),
                    c.getResolutionHours(),
                    withTimeline ? c.getUpdates().stream().map(TimelineDto::of).toList() : null);
        }
    }

    public record TimelineDto(String status, String note, String by, String at) {
        static TimelineDto of(ComplaintUpdate u) {
            return new TimelineDto(u.getStatus().name(), u.getNote(),
                    u.getUpdatedBy() == null ? null : u.getUpdatedBy().getName(), String.valueOf(u.getCreatedAt()));
        }
    }

    @JacksonXmlRootElement(localName = "complaints")
    public record ComplaintList(int count,
                                @JacksonXmlElementWrapper(useWrapping = false)
                                @JacksonXmlProperty(localName = "complaint")
                                List<ComplaintDto> items) { }

    /** Request body for POST and PUT /api/complaints. */
    public record ComplaintRequest(String title, String description, String category, String priority) { }

    /** Request body for PUT /api/complaints/{id}/assign */
    public record AssignRequest(Long staffId, String note) { }

    /** Request body for PUT /api/complaints/{id}/resolve */
    public record ResolveRequest(String note) { }

    @JacksonXmlRootElement(localName = "stats")
    public record Stats(long total, Map<String, Long> byStatus, Map<String, Long> byCategory,
                        Map<String, Double> avgResolutionHoursByCategory) { }

    @JacksonXmlRootElement(localName = "message")
    public record Message(String message) { }
}
