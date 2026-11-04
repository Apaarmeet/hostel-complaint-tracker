package com.hostel.tracker.tags;

import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;

/**
 * Custom tag that draws one horizontal bar of a bar chart (pure HTML/CSS, no JavaScript).
 * <hc:bar label="Plumbing" value="12.5" max="40" suffix=" h"/>
 */
public class BarTag extends SimpleTagSupport {

    private String label;
    private double value;
    private double max;
    private String suffix = "";
    private String color = "accent";

    public void setLabel(String label) { this.label = label; }
    public void setValue(double value) { this.value = value; }
    public void setMax(double max) { this.max = max; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public void setColor(String color) { this.color = color; }

    @Override
    public void doTag() throws IOException {
        double pct = max <= 0 ? 0 : Math.min(100, value * 100 / max);
        String shown = value == Math.floor(value) ? String.valueOf((long) value) : String.format("%.1f", value);
        String safeLabel = label == null ? "" : label.replace("&", "&amp;").replace("<", "&lt;");
        getJspContext().getOut().write(
                "<div class=\"bar-row\"><span class=\"bar-label\">" + safeLabel + "</span>"
              + "<div class=\"bar-track\"><div class=\"bar-fill bar-" + color + "\" style=\"width:"
              + String.format("%.1f", pct) + "%\"></div></div>"
              + "<span class=\"bar-value\">" + shown + suffix + "</span></div>");
    }
}
