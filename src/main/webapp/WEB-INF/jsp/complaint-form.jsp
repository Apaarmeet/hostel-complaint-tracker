<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Complaint"); %>
<%@ include file="header.jspf" %>
<%-- Values come from the saved complaint (edit) or from the submitted form (after a validation error) --%>
<c:set var="editing" value="${not empty complaint or not empty formId}"/>
<c:set var="fid" value="${not empty complaint ? complaint.id : formId}"/>
<c:set var="vTitle" value="${not empty param.title ? param.title : complaint.title}"/>
<c:set var="vDesc" value="${not empty param.description ? param.description : complaint.description}"/>
<c:set var="vCat" value="${not empty param.category ? param.category : complaint.category}"/>
<c:set var="vPrio" value="${not empty param.priority ? param.priority : (empty complaint ? 'MEDIUM' : complaint.priority)}"/>

<div class="narrow">
    <a class="back" href="${ctx}${editing ? '/complaint?id='.concat(fid) : '/dashboard'}">← Back</a>
    <div class="card">
        <h2>${editing ? 'Edit complaint #'.concat(fid) : 'Raise a complaint'}</h2>
        <p class="muted">Room <b><c:out value="${me.roomNo}"/></b> is filled in from your profile.</p>
        <c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>

        <form method="post" action="${ctx}/complaint" class="form">
            <input type="hidden" name="action" value="${editing ? 'update' : 'create'}">
            <c:if test="${editing}"><input type="hidden" name="id" value="${fid}"></c:if>

            <label>What's wrong? <span class="muted small">(max ${maxTitle} characters)</span>
                <input name="title" maxlength="${maxTitle}" required placeholder="e.g. Fan not working"
                       value="<c:out value='${vTitle}'/>">
            </label>

            <label>Category</label>
            <div class="chips-select">
                <c:forEach var="cat" items="${applicationScope.categories}">
                    <label class="chip-radio">
                        <input type="radio" name="category" value="<c:out value='${cat}'/>" ${cat == vCat ? 'checked' : ''} required>
                        <span><c:out value="${cat}"/></span>
                    </label>
                </c:forEach>
            </div>

            <label>Priority</label>
            <div class="chips-select">
                <c:forEach var="p" items="${priorities}">
                    <label class="chip-radio prio-${fn:toLowerCase(p)}">
                        <input type="radio" name="priority" value="${p}" ${p == vPrio ? 'checked' : ''}>
                        <span>${p}</span>
                    </label>
                </c:forEach>
            </div>

            <label>Details <span class="muted small">(max ${maxDescription} characters)</span>
                <textarea name="description" rows="5" maxlength="${maxDescription}" required
                          placeholder="Where exactly is the problem? Since when?"><c:out value="${vDesc}"/></textarea>
            </label>

            <button class="btn btn-primary" type="submit">${editing ? 'Save changes' : 'Submit complaint'}</button>
        </form>
    </div>
</div>
<jsp:include page="footer.jsp"/>
