package com.api.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

	@Autowired
	AuthenticationFilter filter;

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("authorization-server",
						r -> r.path("/user/**").filters(f -> f.filter(filter)).uri("lb://authorization-server"))
				.route("iptreatment-offering-service",
						r -> r.path("/offerings/**").filters(f -> f.filter(filter))
								.uri("lb://iptreatment-offering-service"))
				.route("iptreatment-service",
						r -> r.path("/treatment/**").filters(f -> f.filter(filter)).uri("lb://iptreatment-service"))
				.route("insurance-claim-service",
						r -> r.path("/insurer/**").filters(f -> f.filter(filter)).uri("lb://insurance-claim-service"))
				.build();
	}

}