# HostelCare: Hostel Complaint & Maintenance Tracker

Advanced Internet Programming mini project. Built with Java Servlets, JSP, JDBC, Hibernate, Spring MVC, Spring Boot and a REST API (JSON + XML).

Students raise maintenance complaints (electricity, plumbing, Wi-Fi...). The **warden** assigns each one to a **staff** member, and the status goes **Open → In Progress → Resolved**, with a full timeline. Reports show the average time to fix each category of problem.

## Demo accounts

| Role    | Email               | Password   |
|---------|---------------------|------------|
| Warden  | warden@hostel.edu   | warden123  |
| Staff   | ramesh@hostel.edu (also suresh, anil, kavita) | staff123 |
| Student | aarav@hostel.edu (also priya, rohan, sneha)   | student123 |

New students can also register themselves.

## Syllabus coverage (where to point the examiner)

| Syllabus topic | Where |
|---|---|
| Client–server, static vs dynamic pages | `static/css` is static; every JSP page is dynamic |
| Servlet life cycle (`init`, `service`, `destroy`) | `servlet/BaseServlet.java` |
| `@WebServlet`, web container (Tomcat) | all classes in `servlet/` |
| `HttpServletRequest` / `HttpServletResponse` | every servlet |
| **GET vs POST** | `/dashboard?status=OPEN` filters use GET (bookmarkable); login, register and complaint actions use POST |
| **RequestDispatcher.forward vs sendRedirect** | `LoginServlet`: failed login is forwarded, successful login is redirected. All POSTs redirect (Post/Redirect/Get) |
| **ServletConfig** (per servlet) | `LoginServlet` `maxAttempts`, `ComplaintServlet` `maxTitleLength` / `maxDescriptionLength` |
| ServletContext init params (whole app) | `application.properties` → `hostelName`, `categories`, read in `listener/AppListener.java` |
| **HttpSession (Experiment 2)** | `/session-info`: creation time, last-accessed time, visit counter |
| Session tracking / login | `user` attribute in session, `filter/AuthFilter.java` |
| JSP scripting elements: declaration, scriptlet, expression | `session-info.jsp` |
| JSP directives | `page`, `include` (`header.jspf`), `taglib` |
| JSP actions | `<jsp:include>` (footer), `<jsp:useBean>` (session-info) |
| **Custom tags** | `tags/StatusBadgeTag.java`, `tags/BarTag.java`, `WEB-INF/hostel.tld` → `<hc:badge>`, `<hc:bar>` |
| JSP implicit objects | `session`, `application`, `request`, `pageContext`, `param` |
| **JDBC**: `PreparedStatement`, `Statement`, `ResultSet` | `dao/UserDao.java` (register, login, profile, delete) |
| `ResultSetMetaData` | `UserDao.runReport()` → `/admin/users` page builds its table from metadata |
| **Hibernate** insert / update / delete / select, HQL | `dao/ComplaintDao.java` (uses a Hibernate `Session`) |
| Entity mapping, relations | `model/Complaint.java` (`@ManyToOne`, `@OneToMany` timeline) |
| **Spring MVC** (Controller, Model, View) | `web/ReportController.java` → `reports.jsp` |
| **Spring Boot** | `HostelTrackerApplication.java` (embedded Tomcat, auto-config) |
| **REST: GET, POST, PUT, DELETE** | `api/ComplaintApiController.java`, try it at `/api-console` |
| **JSON and XML** | `/api/complaints` (JSON), `/api/complaints?format=xml` (XML) |
| Filters & listeners (bonus) | `AuthFilter` (login + role check), `AppListener` (live session counter) |

## REST API

| Method | URL | Who |
|---|---|---|
| GET | `/api/complaints?status=OPEN&category=Plumbing` | everyone (sees only what they are allowed to) |
| GET | `/api/complaints/{id}` | owner / assigned staff / warden |
| POST | `/api/complaints` `{"title","description","category","priority"}` | student |
| PUT | `/api/complaints/{id}` | student (while OPEN) |
| PUT | `/api/complaints/{id}/assign` `{"staffId":2,"note":"..."}` | warden |
| PUT | `/api/complaints/{id}/resolve` `{"note":"..."}` | assigned staff / warden |
| DELETE | `/api/complaints/{id}` | student (own OPEN) / warden |
| GET | `/api/stats` | everyone |

Add `?format=xml` to any GET to get XML.

## Run locally

Needs Java 21 and Maven:

```bash
mvn package -DskipTests
java -jar target/app.war        # http://localhost:8080
```

Or with Docker: `docker build -t hostelcare . && docker run -p 8080:8080 hostelcare`

The default database is H2 (in-memory), and demo data is inserted on every start. To keep data permanently, set the `DATABASE_URL` environment variable to a PostgreSQL URL (e.g. a free Neon database).

## Deployment

- **Java app** → Render (free Docker web service, `render.yaml` + `Dockerfile`)
- **Public URL** → Vercel (`vercel-proxy/vercel.json` passes every request through to Render)

Vercel can't run Java, so it serves as the front door and Render runs the servlet container. On Render's free plan the app sleeps after 15 minutes of inactivity, and the first visit after that takes about a minute while it wakes up. Open the link a minute before your demo.

## Viva quick answers

- **forward vs redirect?** A forward happens inside the server: it's the same request and the URL doesn't change, which is why a failed login can still show what you typed. A redirect tells the browser to make a new request. We redirect after every POST, so refreshing the page can't submit the form twice.
- **GET vs POST?** GET puts the data in the URL, so it can be bookmarked and shouldn't change anything (filters). POST sends the data in the request body, which is right for passwords and for anything that changes data.
- **ServletConfig vs ServletContext?** ServletConfig holds settings for one servlet (`maxAttempts` belongs only to LoginServlet). ServletContext is shared by the whole app (`categories` is used everywhere).
- **Why JDBC for users and Hibernate for complaints?** It shows both approaches. With JDBC you write every SQL statement and map each ResultSet column by hand. With Hibernate you work with Java objects: updating a complaint is just calling setters, and Hibernate saves the changes on commit (dirty checking).
- **How is security handled?** Passwords are hashed with BCrypt. A filter blocks pages you aren't allowed to see, and the session ID is renewed at login to prevent session fixation. JSP output goes through `<c:out>` (prevents XSS), and every query uses a PreparedStatement or HQL parameters (prevents SQL injection).
