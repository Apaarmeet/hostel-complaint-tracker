<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Welcome"); %>
<%@ include file="header.jspf" %>
<section class="hero">
    <div>
        <span class="eyebrow">Hostel maintenance, sorted</span>
        <h1>Report a problem in 30 seconds.<br>Watch it get <em>fixed</em>.</h1>
        <p class="lead">Students raise complaints, the warden assigns the right staff member, and everyone can see where each complaint stands: <b>Open → In Progress → Resolved</b>.</p>
        <div class="hero-cta">
            <a class="btn btn-primary" href="${ctx}/login">Log in</a>
            <a class="btn btn-ghost" href="${ctx}/register">I'm a new student</a>
        </div>
    </div>
    <div class="hero-card">
        <div class="tl-demo">
            <div class="tl-step done"><span></span><div><b>Open</b><small>Tap leaking in B-204 · Priya</small></div></div>
            <div class="tl-step done"><span></span><div><b>In Progress</b><small>Assigned to Suresh (Plumbing)</small></div></div>
            <div class="tl-step now"><span></span><div><b>Resolved</b><small>Washer replaced · 22 h</small></div></div>
        </div>
    </div>
</section>

<section class="grid3">
    <div class="card feature"><h3>🎓 Students</h3><p>Raise, edit or withdraw complaints, follow the live timeline, and re-open the complaint if the fix doesn't work.</p></div>
    <div class="card feature"><h3>🧑‍💼 Warden</h3><p>See every complaint, filter by status or category, assign staff, and track average fix time.</p></div>
    <div class="card feature"><h3>🔧 Staff</h3><p>See only your assigned jobs and mark them resolved with a note on what you did.</p></div>
</section>

<section class="card">
    <h3>Demo accounts</h3>
    <p class="muted">Click a row to log in as that person.</p>
    <table class="table">
        <thead><tr><th>Role</th><th>Email</th><th>Password</th></tr></thead>
        <tbody>
        <tr class="clickable" onclick="location='${ctx}/login?demo=warden@hostel.edu'"><td><span class="role role-warden">WARDEN</span></td><td>warden@hostel.edu</td><td>warden123</td></tr>
        <tr class="clickable" onclick="location='${ctx}/login?demo=ramesh@hostel.edu'"><td><span class="role role-staff">STAFF</span></td><td>ramesh@hostel.edu (Electricity)</td><td>staff123</td></tr>
        <tr class="clickable" onclick="location='${ctx}/login?demo=aarav@hostel.edu'"><td><span class="role role-student">STUDENT</span></td><td>aarav@hostel.edu</td><td>student123</td></tr>
        </tbody>
    </table>
</section>
<jsp:include page="footer.jsp"/>
