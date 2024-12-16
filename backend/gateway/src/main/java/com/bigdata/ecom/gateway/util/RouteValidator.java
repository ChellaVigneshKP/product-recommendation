package com.bigdata.ecom.gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;  // Represents the server HTTP request
import org.springframework.stereotype.Component;  // Indicates that this class is a Spring component

import java.util.List;  // For using List collection
import java.util.function.Predicate;  // For using functional predicates

@Component  // Marks this class as a Spring-managed bean
public class RouteValidator {

    // List of open API endpoints that do not require authentication
    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",        // Endpoint for user signup
            "/eureka",             // Endpoint for service discovery (Eureka)
            "/auth/login",         // Endpoint for user login
            "/auth/me",
            "/auth/logout",
            "/products/**"        // Endpoint for fetching products
    );
    private RouteValidator() {
        // Prevent instantiation
    }
    public static final Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints.stream()
                    .noneMatch(url -> {
                        String path = request.getURI().getPath();
                        return path.startsWith(url.replace("/**", ""));
                    });
    public static Predicate<ServerHttpRequest> getIsSecured() {
        return isSecured;
    }
}

