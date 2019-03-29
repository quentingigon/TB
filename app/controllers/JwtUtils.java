package controllers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtils {

	private static final String TOKEN_SECRET = "YoUcAnNevErFinDMe";
	public static final String BEARER = "Bearer ";

	public static String getJWT(){
		Date now = new Date();
		long t = now.getTime();
		Date expirationTime = new Date(t + 1300819380);

		return Jwts.builder()
			.setSubject("Authentification")
			.setIssuedAt(now)
			.setExpiration(expirationTime)
			.signWith(SignatureAlgorithm.HS512, TOKEN_SECRET)
			.compact();
	}

	public static boolean validateJWT(String jwt){
		try {
			Jwts.parser().setSigningKey(TOKEN_SECRET).parseClaimsJws(jwt);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;

		}

	}
}
