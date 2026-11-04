<%@ page contentType="text/html;charset=UTF-8" %>
</main>
<footer class="footer">
    <span>HostelCare · Hostel Complaint &amp; Maintenance Tracker · Advanced Internet Programming mini project</span>
    <span>Live sessions: <b>${applicationScope.activeSessions}</b> ·
        <a href="${pageContext.request.contextPath}/session-info">Session info</a> ·
        <a href="${pageContext.request.contextPath}/api/complaints">JSON</a> /
        <a href="${pageContext.request.contextPath}/api/complaints?format=xml">XML</a></span>
</footer>
</body>
</html>
