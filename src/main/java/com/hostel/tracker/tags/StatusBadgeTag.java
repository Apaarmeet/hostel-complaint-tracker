package com.hostel.tracker.tags;

import com.hostel.tracker.model.Status;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;

/**
 * Custom tag: <hc:badge value="${c.status}"/>  or  <hc:badge value="${c.priority}"/>
 * Prints a coloured pill, e.g. <span class="badge badge-in_progress">In Progress</span>
 */
public class StatusBadgeTag extends SimpleTagSupport {

    private Object value;

    public void setValue(Object value) { this.value = value; }

    @Override
    public void doTag() throws IOException {
        if (value == null) return;
        String key = value.toString();
        String label = value instanceof Status s ? s.getLabel()
                : key.substring(0, 1) + key.substring(1).toLowerCase().replace('_', ' ');
        getJspContext().getOut().write(
                "<span class=\"badge badge-" + key.toLowerCase() + "\">" + label + "</span>");
    }
}
