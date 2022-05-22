package com.api.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import reactor.core.publisher.Mono;

@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {

	@Autowired
	private RouterValidator routerValidator;

	@Autowired
	private JwtUtil jwtUtil;

	private static String token;

	Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		ServerHttpRequest request = exchange.getRequest();
		try {
			if (routerValidator.isSecured.test(request)) {

				if (this.isAuthMissing(request)) {
					logger.info("Authorization is Failed.. No Authorization Header is Present!!");
					return this.onError(exchange, "Authorization header is missing in request", HttpStatus.FORBIDDEN);
				}

				token = this.getAuthHeader(request);

				if (jwtUtil.isInvalid(token)) {
					logger.info("Authorization is Failed.. Invalid Jwt Token!!");
					return this.onError(exchange, "Authorization is Failed", HttpStatus.FORBIDDEN);
				}

				this.populateRequestWithHeaders(exchange, token);
			}
			if (routerValidator.isAdmin.test(request)) {
				String auths = (String) jwtUtil.getAllClaimsFromToken(token).get("AUTHORIZATION");
				if (!(auths.contains("ADMIN") || auths.contains("admin"))) {
					logger.info("Authorization is Failed.. User Access is denied for the requested Resource!!");
					return this.onError(exchange, "The user is not allowed to Access this Page!",
							HttpStatus.UNAUTHORIZED);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return this.onError(exchange, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		logger.info("Authorization is Successfull..");
		return chain.filter(exchange);
	}

	/* PRIVATE */

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}

	private String getAuthHeader(ServerHttpRequest request) {
		return request.getHeaders().getOrEmpty("Authorization").get(0);
	}

	private boolean isAuthMissing(ServerHttpRequest request) {
		return !request.getHeaders().containsKey("Authorization");
	}

	private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
		Claims claims = null;

		try {
			claims = jwtUtil.getAllClaimsFromToken(token);
		} catch (SignatureException e) {
			logger.info("Invalid JWT signature.");
			logger.trace("Invalid JWT signature trace: {}", e);
		} catch (MalformedJwtException e) {
			logger.info("Invalid JWT token.");
			logger.trace("Invalid JWT token trace: {}", e);
		} catch (ExpiredJwtException e) {
			logger.info("Expired JWT token.");
			logger.trace("Expired JWT token trace: {}", e);
		} catch (ClaimJwtException e) {
			logger.info("Claiming the JWT token is Failed");
			logger.trace("Claiming JWT token trace: {}", e);
		}
		exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id")))
				.header("role", String.valueOf(claims.get("role"))).build();
	}
}