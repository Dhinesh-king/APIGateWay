package com.api.app;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouterValidator {

    public static final List<String> openApiEndpoints= Arrays.asList(
            "/user/",
            "/user/login"
    );

    public static final List<String> adminApiEndPoints= Arrays.asList(
            "/insurer/allInsurerDetails",
            "/insurer/getInsurerPackageName",
            "/insurer/initiateClaim"
    );
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
            
    public Predicate<ServerHttpRequest> isAdmin =
                    request -> adminApiEndPoints
                            .stream()
                            .anyMatch(uri -> request.getURI().getPath().contains(uri));

}