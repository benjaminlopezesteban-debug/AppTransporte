package cl.duoc.appbelmonte.direccion.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIJO_BEARER = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta,
			FilterChain cadena) throws ServletException, IOException {
		String encabezado = solicitud.getHeader(HttpHeaders.AUTHORIZATION);

		if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
			String token = encabezado.substring(PREFIJO_BEARER.length());
			try {
				Claims claims = jwtService.parsear(token);
				Long usuarioId = Long.valueOf(claims.getSubject());
				String rol = claims.get("rol", String.class);
				SimpleGrantedAuthority autoridad = new SimpleGrantedAuthority("ROLE_" + rol);
				UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
						usuarioId, null, List.of(autoridad));
				autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(solicitud));
				SecurityContextHolder.getContext().setAuthentication(autenticacion);
			} catch (JwtException | IllegalArgumentException excepcion) {
				SecurityContextHolder.clearContext();
			}
		}

		cadena.doFilter(solicitud, respuesta);
	}
}
