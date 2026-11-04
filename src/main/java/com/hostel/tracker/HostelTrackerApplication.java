package com.hostel.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point.
 *
 * @ServletComponentScan makes the embedded Tomcat pick up our classic Java EE components:
 * every class annotated with @WebServlet, @WebFilter and @WebListener.
 * Extending SpringBootServletInitializer lets the same WAR also be deployed to an external Tomcat.
 */
@SpringBootApplication
@ServletComponentScan
public class HostelTrackerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(HostelTrackerApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(HostelTrackerApplication.class);
    }
}
