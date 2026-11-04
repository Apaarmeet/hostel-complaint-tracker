<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Log in"); %>
<%@ include file="header.jspf" %>
<%-- "demo" query parameter pre-fills the form from the landing page --%>
<c:set var="demo" value="${param.demo}"/>
<c:choose>
    <c:when test="${empty demo}"><c:set var="demoPass" value=""/></c:when>
    <c:when test="${fn:startsWith(demo, 'warden')}"><c:set var="demoPass" value="warden123"/></c:when>
    <c:when test="${fn:contains('ramesh suresh anil kavita', fn:substringBefore(demo, '@'))}"><c:set var="demoPass" value="staff123"/></c:when>
    <c:otherwise><c:set var="demoPass" value="student123"/></c:otherwise>
</c:choose>
<div class="auth">
    <div class="card">
        <h2>Log in</h2>
        <p class="muted">Students, warden and staff all log in here.</p>
        <c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>
        <form method="post" action="${ctx}/login" class="form">
            <label>Email
                <input type="email" name="email" required autofocus value="<c:out value='${not empty email ? email : demo}'/>">
            </label>
            <label>Password
                <input type="password" name="password" required value="${demoPass}">
            </label>
            <button class="btn btn-primary btn-block" type="submit">Log in</button>
        </form>
        <p class="muted small">New student? <a href="${ctx}/register">Create an account</a></p>
        <p class="muted small">This form uses <b>POST</b>, so the password never appears in the URL.</p>
    </div>
</div>
<jsp:include page="footer.jsp"/>
