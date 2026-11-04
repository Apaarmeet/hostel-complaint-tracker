import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Build-time helper: opens every JSP page once (as each role) so Tomcat compiles them
 * during the Docker build. The compiled classes are shipped in the image, so the tiny
 * free-tier server never has to compile JSPs itself.
 * Run with:  java docker/Warmup.java http://localhost:8080
 */
public class Warmup {
    static HttpClient client;
    static String base;
    static int failures = 0;

    public static void main(String[] args) throws Exception {
        base = args[0];
        for (int i = 0; i < 120; i++) {               // wait for the app to start
            try { if (newClient().send(get("/health"), HttpResponse.BodyHandlers.ofString()).statusCode() == 200) break; }
            catch (Exception ignored) { }
            Thread.sleep(1000);
        }
        session(null, null, "/", "/login", "/register", "/session-info");
        session("warden@hostel.edu", "warden123", "/dashboard", "/complaint?id=1", "/reports", "/admin/users",
                "/profile", "/api-console");
        session("aarav@hostel.edu", "student123", "/dashboard", "/complaint?action=new", "/complaint?id=1",
                "/complaint?action=edit&id=11");
        session("ramesh@hostel.edu", "staff123", "/dashboard", "/complaint?id=1");
        if (failures > 0) { System.err.println(failures + " page(s) failed"); System.exit(1); }
        System.out.println("All JSP pages compiled");
    }

    static void session(String email, String password, String... pages) throws Exception {
        client = newClient();
        if (email != null) {
            HttpRequest login = HttpRequest.newBuilder(URI.create(base + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("email=" + email + "&password=" + password)).build();
            client.send(login, HttpResponse.BodyHandlers.discarding());
        }
        for (String p : pages) {
            int code = client.send(get(p), HttpResponse.BodyHandlers.ofString()).statusCode();
            System.out.println(code + "  " + p + (email == null ? "" : "  (" + email + ")"));
            if (code >= 500) failures++;
        }
    }

    static HttpClient newClient() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager())
                .followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(5)).build();
    }

    static HttpRequest get(String path) {
        return HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(60)).GET().build();
    }
}
