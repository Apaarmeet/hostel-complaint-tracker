<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Profile"); %>
<%@ include file="header.jspf" %>
<div class="narrow">
    <div class="card">
        <h2>My profile</h2>
        <p class="muted"><c:out value="${me.email}"/> · <span class="role role-${fn:toLowerCase(me.role)}">${me.role}</span></p>
        <c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>
        <form method="post" action="${ctx}/profile" class="form">
            <label>Name <input name="name" required value="<c:out value='${me.name}'/>"></label>
            <div class="row2">
                <label>Phone <input name="phone" pattern="\d{10}" required value="<c:out value='${me.phone}'/>"></label>
                <c:if test="${me.student}">
                    <label>Room no. <input name="roomNo" required value="<c:out value='${me.roomNo}'/>"></label>
                </c:if>
            </div>
            <button class="btn btn-primary" type="submit">Save</button>
        </form>
    </div>
</div>
<jsp:include page="footer.jsp"/>
