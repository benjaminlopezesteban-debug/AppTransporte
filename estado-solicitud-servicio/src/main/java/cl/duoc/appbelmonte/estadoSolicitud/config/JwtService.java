package cl.duoc.appbelmonte.estadoSolicitud.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecretKey clave;

	public JwtService(@Value("${jwt.secret}") String secreto) {
		this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
	}

	public Claims parsear(String token) {
		return Jwts.parser()
				.verifyWith(clave)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
