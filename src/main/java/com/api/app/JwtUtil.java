package com.api.app;

import java.util.Base64;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	Logger logger = LoggerFactory.getLogger(JwtUtil.class);

	public Claims getAllClaimsFromToken(String token)
			throws SignatureException, MalformedJwtException, ClaimJwtException {
		Claims claims = Jwts.parser().setSigningKey(Base64.getEncoder().encode(secret.getBytes())).parseClaimsJws(token)
				.getBody();
		return claims;
	}

	private boolean isTokenExpired(String token)
			throws ExpiredJwtException, SignatureException, MalformedJwtException, ClaimJwtException {
		return this.getAllClaimsFromToken(token).getExpiration().before(new Date());
	}

	public boolean isInvalid(String token) {
		try {
			boolean checkExpired = this.isTokenExpired(token);
			return checkExpired;
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
		return true;
	}

}