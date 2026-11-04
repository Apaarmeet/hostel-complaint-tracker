<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Reports"); %>
<%@ include file="header.jspf" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<div class="page-head">
    <div>
        <h1>Reports</h1>
        <p class="muted">How fast is each kind of problem fixed? Built with a Spring MVC controller (Controller → Model → View).</p>
    </div>
    <a class="btn btn-ghost" href="${ctx}/api/stats">Raw numbers (JSON)</a>
</div>

<div class="stats">
    <div class="stat stat-all"><b>${total}</b><span>Total complaints</span></div>
    <div class="stat stat-open"><b>${counts['OPEN']}</b><span>Waiting</span></div>
    <div class="stat stat-progress"><b>${counts['IN_PROGRESS']}</b><span>Being fixed</span></div>
    <div class="stat stat-resolved"><b><fmt:formatNumber value="${overallAvg}" maxFractionDigits="1"/> h</b><span>Avg. time to fix</span></div>
</div>

<div class="grid2">
    <div class="card">
        <h3>Average resolution time by category</h3>
        <p class="muted small">Hours from complaint raised to resolved (resolved complaints only)</p>
        <c:forEach var="r" items="${categories}">
            <hc:bar label="${r.name}" value="${r.avgHours}" max="${maxAvg}" suffix=" h" color="accent"/>
        </c:forEach>
    </div>
    <div class="card">
        <h3>Complaints by category</h3>
        <p class="muted small">Total complaints raised in each category</p>
        <c:forEach var="r" items="${categories}">
            <hc:bar label="${r.name}" value="${r.total}" max="${maxTotal}" color="blue"/>
        </c:forEach>
    </div>
</div>

<div class="grid2">
    <div class="card">
        <h3>Staff performance</h3>
        <table class="table">
            <thead><tr><th>Staff</th><th>Assigned</th><th>Resolved</th><th>Avg. hours</th></tr></thead>
            <tbody>
            <c:forEach var="s" items="${staffRows}">
                <tr><td><c:out value="${s.name}"/></td><td>${s.total}</td>
                    <td>${s.resolved} <span class="muted small">(${s.resolvedPercent}%)</span></td>
                    <td><fmt:formatNumber value="${s.avgHours}" maxFractionDigits="1"/></td></tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
    <div class="card">
        <h3>Waiting the longest</h3>
        <c:if test="${empty oldestPending}"><p class="muted">Nothing pending 🎉</p></c:if>
        <ul class="plain">
            <c:forEach var="c" items="${oldestPending}">
                <li><a href="${ctx}/complaint?id=${c.id}">#${c.id} <c:out value="${c.title}"/></a>
                    <span class="muted small">· ${c.age}</span> <hc:badge value="${c.status}"/></li>
            </c:forEach>
        </ul>
    </div>
</div>
<jsp:include page="footer.jsp"/>
