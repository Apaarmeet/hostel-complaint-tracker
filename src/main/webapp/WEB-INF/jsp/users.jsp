<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Users"); %>
<%@ include file="header.jspf" %>
<div class="page-head">
    <div>
        <h1>Users</h1>
        <p class="muted">This table is generated with <code>ResultSetMetaData</code>: column names and SQL types are read from the query result at runtime.</p>
    </div>
</div>
<div class="card scroll-x">
    <table class="table">
        <thead>
        <tr>
            <c:forEach var="col" items="${table.columns}" varStatus="i">
                <th>${fn:toUpperCase(col)}<br><span class="muted small">${table.types[i.index]}</span></th>
            </c:forEach>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="row" items="${table.rows}">
            <tr>
                <c:forEach var="cell" items="${row}"><td><c:out value="${cell}"/></td></c:forEach>
                <td>
                    <c:if test="${row[3] != 'WARDEN'}">
                        <form method="post" action="${ctx}/admin/users">
                            <input type="hidden" name="id" value="${row[0]}">
                            <button class="btn btn-link-danger btn-sm" type="submit">Delete</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<p class="muted small">Users who still have complaints cannot be deleted, because the database's foreign keys block it.</p>
<jsp:include page="footer.jsp"/>
