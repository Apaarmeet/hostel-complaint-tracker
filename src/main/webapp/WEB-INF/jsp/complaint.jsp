<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Complaint"); %>
<%@ include file="header.jspf" %>
<c:set var="c" value="${complaint}"/>
<a class="back" href="${ctx}/dashboard">← Dashboard</a>

<div class="detail">
    <div>
        <div class="card">
            <div class="detail-head">
                <div>
                    <div class="muted small">Complaint #${c.id} · raised ${c.age}</div>
                    <h1><c:out value="${c.title}"/></h1>
                </div>
                <div class="item-side"><hc:badge value="${c.priority}"/> <hc:badge value="${c.status}"/></div>
            </div>
            <p class="desc"><c:out value="${c.description}"/></p>
            <dl class="facts">
                <div><dt>Category</dt><dd><c:out value="${c.category}"/></dd></div>
                <div><dt>Room</dt><dd><c:out value="${c.roomNo}"/></dd></div>
                <div><dt>Raised by</dt><dd><c:out value="${c.student.name}"/><c:if test="${not me.student}"> · <c:out value="${c.student.phone}"/></c:if></dd></div>
                <div><dt>Assigned to</dt><dd><c:out value="${empty c.assignedTo ? 'Not assigned yet' : c.assignedTo.name}"/></dd></div>
                <div><dt>Raised on</dt><dd>${c.createdAtText}</dd></div>
                <c:if test="${not empty c.resolvedAt}"><div><dt>Fixed in</dt><dd>${c.resolutionHours} hours</dd></div></c:if>
            </dl>
        </div>

        <%-- ===== Role-specific actions ===== --%>
        <c:if test="${me.student and c.status == 'OPEN'}">
            <div class="card actions">
                <a class="btn btn-ghost" href="${ctx}/complaint?action=edit&id=${c.id}">Edit</a>
                <form method="post" action="${ctx}/complaint" onsubmit="return confirmDelete(this)">
                    <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${c.id}">
                    <button class="btn btn-danger" type="submit">Withdraw complaint</button>
                </form>
            </div>
        </c:if>

        <c:if test="${me.student and c.status == 'RESOLVED'}">
            <form class="card form" method="post" action="${ctx}/complaint">
                <h3>Problem still there?</h3>
                <input type="hidden" name="action" value="reopen"><input type="hidden" name="id" value="${c.id}">
                <label>Reason <input name="reason" required placeholder="e.g. Tap started leaking again"></label>
                <button class="btn btn-ghost" type="submit">Re-open complaint</button>
            </form>
        </c:if>

        <c:if test="${me.warden and c.status != 'RESOLVED'}">
            <form class="card form" method="post" action="${ctx}/complaint">
                <h3>${empty c.assignedTo ? 'Assign to staff' : 'Re-assign'}</h3>
                <input type="hidden" name="action" value="assign"><input type="hidden" name="id" value="${c.id}">
                <div class="row2">
                    <label>Staff member
                        <select name="staffId" required>
                            <c:forEach var="s" items="${staff}">
                                <option value="${s.id}" ${fn:contains(s.speciality, fn:substringBefore(c.category.concat(' '), ' ')) ? 'selected' : ''}>
                                    <c:out value="${s.name}"/> (<c:out value="${s.speciality}"/>)</option>
                            </c:forEach>
                        </select>
                    </label>
                    <label>Note (optional) <input name="note" placeholder="e.g. Urgent, check today"></label>
                </div>
                <button class="btn btn-primary" type="submit">Assign</button>
            </form>
        </c:if>

        <c:if test="${(me.staff or me.warden) and c.status == 'IN_PROGRESS'}">
            <form class="card form" method="post" action="${ctx}/complaint">
                <h3>Mark as resolved</h3>
                <input type="hidden" name="action" value="resolve"><input type="hidden" name="id" value="${c.id}">
                <label>What was done? <input name="note" required placeholder="e.g. Replaced the washer"></label>
                <button class="btn btn-success" type="submit">✓ Resolved</button>
            </form>
        </c:if>

        <c:if test="${me.warden}">
            <form method="post" action="${ctx}/complaint" class="danger-zone" onsubmit="return confirmDelete(this)">
                <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${c.id}">
                <button class="btn btn-link-danger" type="submit">Delete this complaint permanently</button>
            </form>
        </c:if>
    </div>

    <%-- ===== Status timeline ===== --%>
    <aside class="card">
        <h3>Timeline</h3>
        <ol class="timeline">
            <c:forEach var="u" items="${c.updates}" varStatus="loop">
                <li class="tl tl-${fn:toLowerCase(u.status)} ${loop.last ? 'tl-last' : ''}">
                    <div class="tl-dot"></div>
                    <div>
                        <div><hc:badge value="${u.status}"/></div>
                        <div class="tl-note"><c:out value="${u.note}"/></div>
                        <div class="muted small"><c:out value="${u.updatedBy.name}"/> · ${u.createdAtText}</div>
                    </div>
                </li>
            </c:forEach>
        </ol>
        <p class="muted small">Also available as <a href="${ctx}/api/complaints/${c.id}">JSON</a> ·
            <a href="${ctx}/api/complaints/${c.id}?format=xml">XML</a></p>
    </aside>
</div>
<script>
    // Two-click delete instead of a browser confirm() popup
    function confirmDelete(form) {
        const b = form.querySelector('button');
        if (b.dataset.armed) return true;
        b.dataset.armed = '1'; b.textContent = 'Click again to confirm';
        setTimeout(() => { delete b.dataset.armed; b.textContent = 'Delete'; }, 3000);
        return false;
    }
</script>
<jsp:include page="footer.jsp"/>
