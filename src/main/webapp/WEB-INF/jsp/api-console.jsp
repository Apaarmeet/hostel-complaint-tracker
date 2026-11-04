<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "REST API"); %>
<%@ include file="header.jspf" %>
<div class="page-head">
    <div>
        <h1>REST API console</h1>
        <p class="muted">Send real GET / POST / PUT / DELETE requests to the web service, logged in as <b><c:out value="${me.name}"/></b> (${me.role}). Add <code>?format=xml</code> to a GET to get XML instead of JSON.</p>
    </div>
</div>

<div class="grid-api">
    <div class="card">
        <h3>Presets</h3>
        <div class="presets">
            <button data-m="GET" data-u="/api/complaints">GET all complaints</button>
            <button data-m="GET" data-u="/api/complaints?status=OPEN">GET open only</button>
            <button data-m="GET" data-u="/api/complaints?format=xml">GET as XML</button>
            <button data-m="GET" data-u="/api/complaints/1">GET complaint #1</button>
            <button data-m="GET" data-u="/api/stats">GET stats</button>
            <c:if test="${me.student}">
                <button data-m="POST" data-u="/api/complaints" data-b='{"title":"Mirror broken in bathroom","description":"The mirror above the wash basin is cracked","category":"Other","priority":"LOW"}'>POST new complaint</button>
                <button data-m="PUT" data-u="/api/complaints/ID" data-b='{"title":"Mirror broken in bathroom","description":"Cracked mirror, sharp edges, please replace soon","category":"Other","priority":"HIGH"}'>PUT edit complaint</button>
                <button data-m="DELETE" data-u="/api/complaints/ID">DELETE complaint</button>
            </c:if>
            <c:if test="${me.warden}">
                <button data-m="PUT" data-u="/api/complaints/ID/assign" data-b='{"staffId":2,"note":"Assigned via REST API"}'>PUT assign to staff #2</button>
                <button data-m="PUT" data-u="/api/complaints/ID/resolve" data-b='{"note":"Resolved via REST API"}'>PUT resolve</button>
                <button data-m="DELETE" data-u="/api/complaints/ID">DELETE complaint</button>
            </c:if>
            <c:if test="${me.staff}">
                <button data-m="PUT" data-u="/api/complaints/ID/resolve" data-b='{"note":"Fixed, verified with student"}'>PUT resolve</button>
            </c:if>
        </div>
        <p class="muted small">Presets with <code>ID</code> use the id of the last complaint you created or fetched.</p>
    </div>

    <div class="card">
        <div class="req">
            <select id="method"><option>GET</option><option>POST</option><option>PUT</option><option>DELETE</option></select>
            <input id="url" value="/api/complaints">
            <button class="btn btn-primary" id="send">Send</button>
        </div>
        <label class="small muted">Request body (JSON)</label>
        <textarea id="body" rows="5" placeholder="{ }"></textarea>
        <div class="resp-head"><span id="status" class="muted">No request sent yet</span><span id="time" class="muted small"></span></div>
        <pre id="out" class="code">Response appears here</pre>
    </div>
</div>

<script>
    const ctx = '${ctx}';
    let lastId = null;
    const $ = id => document.getElementById(id);

    document.querySelectorAll('.presets button').forEach(b => b.addEventListener('click', () => {
        $('method').value = b.dataset.m;
        $('url').value = b.dataset.u.replace('ID', lastId ?? 1);
        $('body').value = b.dataset.b ? JSON.stringify(JSON.parse(b.dataset.b), null, 2) : '';
    }));

    $('send').addEventListener('click', async () => {
        const method = $('method').value, started = performance.now();
        const opts = { method, headers: {} };
        if (method === 'POST' || method === 'PUT') {
            opts.headers['Content-Type'] = 'application/json';
            opts.body = $('body').value || '{}';
        }
        $('status').textContent = 'Sending...';
        try {
            const res = await fetch(ctx + $('url').value, opts);
            const text = await res.text();
            $('status').innerHTML = '<b class="' + (res.ok ? 'ok' : 'bad') + '">' + res.status + ' ' + res.statusText + '</b> · '
                + (res.headers.get('Content-Type') || '');
            $('time').textContent = Math.round(performance.now() - started) + ' ms';
            let pretty = text;
            try { const j = JSON.parse(text); pretty = JSON.stringify(j, null, 2); if (j.id) lastId = j.id; } catch (e) { }
            $('out').textContent = pretty;
        } catch (e) {
            $('status').textContent = 'Network error: ' + e.message;
        }
    });
</script>
<jsp:include page="footer.jsp"/>
