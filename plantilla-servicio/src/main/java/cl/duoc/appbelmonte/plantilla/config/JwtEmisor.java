package cl.duoc.appbelmonte.plantilla.config;

import cl.duoc.appbelmonte.plantilla.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtEmisor {

	private final SecretKey clave;
	private final long expiracionMs;

	public JwtEmisor(@Value("${jwt.secret}") String secreto,
			@Value("${jwt.expiracion-ms:86400000}") long expiracionMs) {
		this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
		this.expiracionMs = expiracionMs;
	}

	public String generarToken(Usuario usuario) {
		Instant ahora = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(usuario.getId()))
				.claim("rol", usuario.getRol().name())
				.claim("nombre", usuario.getNombre())
				.issuedAt(Date.from(ahora))
				.expiration(Date.from(ahora.plusMillis(expiracionMs)))
				.signWith(clave)
				.compact();
	}
}
