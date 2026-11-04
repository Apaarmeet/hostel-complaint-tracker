<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Register"); %>
<%@ include file="header.jspf" %>
<div class="auth">
    <div class="card">
        <h2>Student registration</h2>
        <c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>
        <form method="post" action="${ctx}/register" class="form">
            <label>Full name <input name="name" required value="<c:out value='${param.name}'/>"></label>
            <label>College email <input type="email" name="email" required value="<c:out value='${param.email}'/>"></label>
            <div class="row2">
                <label>Room no. <input name="roomNo" placeholder="A-101" required value="<c:out value='${param.roomNo}'/>"></label>
                <label>Phone <input name="phone" pattern="\d{10}" placeholder="10 digits" required value="<c:out value='${param.phone}'/>"></label>
            </div>
            <label>Password <input type="password" name="password" minlength="6" required></label>
            <button class="btn btn-primary btn-block" type="submit">Create account</button>
        </form>
        <p class="muted small">Already registered? <a href="${ctx}/login">Log in</a></p>
    </div>
</div>
<jsp:include page="footer.jsp"/>
