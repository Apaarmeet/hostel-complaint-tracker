<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Dashboard"); %>
<%@ include file="header.jspf" %>
<div class="page-head">
    <div>
        <h1>
            <c:choose>
                <c:when test="${me.student}">My complaints</c:when>
                <c:when test="${me.staff}">My assigned jobs</c:when>
                <c:otherwise>All complaints</c:otherwise>
            </c:choose>
        </h1>
        <p class="muted">
            <c:choose>
                <c:when test="${me.student}">Room <c:out value="${me.roomNo}"/> · track everything you have reported.</c:when>
                <c:when test="${me.staff}">Speciality: <c:out value="${me.speciality}"/></c:when>
                <c:otherwise>${studentCount} students · ${staffCount} maintenance staff</c:otherwise>
            </c:choose>
        </p>
    </div>
    <c:if test="${me.student}"><a class="btn btn-primary" href="${ctx}/complaint?action=new">+ Raise complaint</a></c:if>
    <c:if test="${me.warden}"><a class="btn btn-ghost" href="${ctx}/reports">View reports →</a></c:if>
</div>

<div class="stats">
    <a class="stat stat-open" href="${ctx}/dashboard?status=OPEN"><b>${counts['OPEN']}</b><span>Open</span></a>
    <a class="stat stat-progress" href="${ctx}/dashboard?status=IN_PROGRESS"><b>${counts['IN_PROGRESS']}</b><span>In progress</span></a>
    <a class="stat stat-resolved" href="${ctx}/dashboard?status=RESOLVED"><b>${counts['RESOLVED']}</b><span>Resolved</span></a>
    <a class="stat stat-all" href="${ctx}/dashboard"><b>${counts['OPEN'] + counts['IN_PROGRESS'] + counts['RESOLVED']}</b><span>Total</span></a>
</div>

<%-- Filters are sent with GET so the filtered URL can be bookmarked --%>
<form class="filters card" method="get" action="${ctx}/dashboard">
    <label>Status
        <select name="status" onchange="this.form.submit()">
            <option value="">All</option>
            <c:forEach var="s" items="${statuses}">
                <option value="${s}" ${s == selectedStatus ? 'selected' : ''}>${s.label}</option>
            </c:forEach>
        </select>
    </label>
    <label>Category
        <select name="category" onchange="this.form.submit()">
            <option value="">All</option>
            <c:forEach var="cat" items="${applicationScope.categories}">
                <option ${cat == selectedCategory ? 'selected' : ''}><c:out value="${cat}"/></option>
            </c:forEach>
        </select>
    </label>
    <c:if test="${not empty selectedStatus or not empty selectedCategory}">
        <a class="btn btn-ghost btn-sm" href="${ctx}/dashboard">Clear filters</a>
    </c:if>
    <span class="muted small push">${fn:length(complaints)} result(s)</span>
</form>

<c:choose>
    <c:when test="${empty complaints}">
        <div class="empty card">
            <div class="empty-icon">✨</div>
            <h3>Nothing here</h3>
            <p class="muted">
                <c:choose>
                    <c:when test="${me.student}">No complaints yet. Something broken? <a href="${ctx}/complaint?action=new">Raise one</a>.</c:when>
                    <c:otherwise>No complaints match these filters.</c:otherwise>
                </c:choose>
            </p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="list">
            <c:forEach var="c" items="${complaints}">
                <a class="item card prio-${fn:toLowerCase(c.priority)}" href="${ctx}/complaint?id=${c.id}">
                    <div class="item-main">
                        <div class="item-title">#${c.id} · <c:out value="${c.title}"/></div>
                        <div class="item-meta">
                            <span class="chip"><c:out value="${c.category}"/></span>
                            <span>Room <c:out value="${c.roomNo}"/></span>
                            <c:if test="${not me.student}"><span>by <c:out value="${c.student.name}"/></span></c:if>
                            <c:if test="${not empty c.assignedTo and not me.staff}"><span>→ <c:out value="${c.assignedTo.name}"/></span></c:if>
                            <span>${c.age}</span>
                        </div>
                    </div>
                    <div class="item-side">
                        <hc:badge value="${c.priority}"/>
                        <hc:badge value="${c.status}"/>
                    </div>
                </a>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>
<jsp:include page="footer.jsp"/>
