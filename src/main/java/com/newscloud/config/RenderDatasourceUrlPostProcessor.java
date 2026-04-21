package com.newscloud.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Converts a raw postgres:// or postgresql:// URL to the jdbc:postgresql:// form
 * that Spring Boot / Hikari requires, extracting credentials into separate properties.
 *
 * Handles two sources (checked in this order):
 *  1. DATABASE_URL  — auto-injected by Render when a database is linked to the service
 *  2. SPRING_DATASOURCE_URL (spring.datasource.url) — manually set by the operator
 */
public class RenderDatasourceUrlPostProcessor implements EnvironmentPostProcessor {

    // Use JUL because SLF4J/Logback is not yet initialised when this runs
    private static final Logger log = Logger.getLogger(RenderDatasourceUrlPostProcessor.class.getName());

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {

        log.info("RenderDatasourceUrlPostProcessor running");

        String databaseUrl   = environment.getProperty("DATABASE_URL");
        String datasourceUrl = environment.getProperty("spring.datasource.url");

        log.info("DATABASE_URL=" + mask(databaseUrl));
        log.info("spring.datasource.url=" + mask(datasourceUrl));

        Map<String, Object> props = new HashMap<>();

        if (isRawPostgresUrl(databaseUrl)) {
            parseInto(databaseUrl, props);
        } else if (isRawPostgresUrl(datasourceUrl)) {
            parseInto(datasourceUrl, props);
        }

        if (!props.isEmpty()) {
            log.info("Overriding datasource properties: url=" + mask((String) props.get("spring.datasource.url")));
            environment.getPropertySources().addFirst(
                    new MapPropertySource("renderDatasource", props));
        }
    }

    private boolean isRawPostgresUrl(String url) {
        return url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"));
    }

    private void parseInto(String rawUrl, Map<String, Object> props) {
        URI uri = URI.create(rawUrl.replaceFirst("^postgres(ql)?://", "http://"));

        String host = uri.getHost();
        int    port = uri.getPort();
        String path = uri.getPath();

        String jdbcUrl = "jdbc:postgresql://" + host
                + (port > 0 ? ":" + port : "")
                + path
                + "?sslmode=require";

        props.put("spring.datasource.url", jdbcUrl);
        props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");

        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            String[] parts = userInfo.split(":", 2);
            props.put("spring.datasource.username", parts[0]);
            if (parts.length > 1) {
                props.put("spring.datasource.password", parts[1]);
            }
        }
    }

    private String mask(String url) {
        if (url == null) return "null";
        // hide password segment for logging
        return url.replaceAll(":[^:@/]+@", ":***@");
    }
}
