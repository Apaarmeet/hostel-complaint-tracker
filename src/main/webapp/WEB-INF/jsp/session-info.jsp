<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Session info"); %>
<%@ include file="header.jspf" %>
<%@ page import="java.text.SimpleDateFormat, java.util.Date" %>
<%-- Experiment 2. This page deliberately uses all three JSP scripting elements --%>

<%-- 1) DECLARATION: defines a method (or field) of the generated servlet class --%>
<%!
    private String format(Date d) {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm:ss a").format(d);
    }
%>

<%-- jsp:useBean ACTION: creates a java.util.Date bean in page scope --%>
<jsp:useBean id="now" class="java.util.Date" scope="page"/>

<div class="narrow">
    <div class="card">
        <h2>Your HTTP session</h2>
        <p class="muted">A new session is created if the request does not already have one (<code>request.getSession(true)</code>). Reload the page and watch the values change.</p>
        <dl class="facts facts-wide">
            <%-- 2) EXPRESSION elements print a value; "session" is a JSP implicit object --%>
            <div><dt>Session ID</dt><dd><code><%= session.getId() %></code></dd></div>
            <div><dt>New session?</dt><dd>${isNew ? 'Yes, created just now' : 'No, existing session'}</dd></div>
            <div><dt>Created at</dt><dd><%= format((Date) request.getAttribute("created")) %></dd></div>
            <div><dt>Last accessed</dt><dd><%= format((Date) request.getAttribute("lastAccessed")) %></dd></div>
            <div><dt>Visits this session</dt><dd>${visits}</dd></div>
            <div><dt>Timeout</dt><dd>${maxInactive / 60} minutes of inactivity</dd></div>
            <div><dt>Server time</dt><dd><%= format(now) %></dd></div>
            <%-- 3) SCRIPTLET: plain Java code, using the implicit objects application and request --%>
            <%
                Object live = application.getAttribute("activeSessions");
                String browser = request.getHeader("User-Agent");
                if (browser != null && browser.length() > 60) browser = browser.substring(0, 60) + "...";
            %>
            <div><dt>Live sessions on server</dt><dd><%= live %></dd></div>
            <div><dt>Your browser</dt><dd class="small"><%= browser == null ? "-" : browser.replace("<", "&lt;") %></dd></div>
        </dl>
        <a class="btn btn-ghost" href="${ctx}/session-info">Reload</a>
    </div>
</div>
<jsp:include page="footer.jsp"/>
