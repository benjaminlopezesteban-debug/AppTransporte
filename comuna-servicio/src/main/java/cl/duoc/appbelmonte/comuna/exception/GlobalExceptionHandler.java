package cl.duoc.appbelmonte.comuna.exception;

import cl.duoc.appbelmonte.comuna.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<ErrorResponse> manejarCredencialesInvalidas(CredencialesInvalidasException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.UNAUTHORIZED, excepcion.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(ReglaNegocioException.class)
	public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.CONFLICT, excepcion.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(DatosInvalidosException.class)
	public ResponseEntity<ErrorResponse> manejarDatosInvalidos(DatosInvalidosException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.BAD_REQUEST, excepcion.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(RecursoNoEncontradoException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.NOT_FOUND, excepcion.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(ServicioNoDisponibleException.class)
	public ResponseEntity<ErrorResponse> manejarServicioNoDisponible(ServicioNoDisponibleException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.SERVICE_UNAVAILABLE, excepcion.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> manejarParametroInvalido(MethodArgumentTypeMismatchException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.BAD_REQUEST,
				"El parámetro " + excepcion.getName() + " no tiene un valor válido");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> manejarAccesoDenegado(AccessDeniedException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.FORBIDDEN,
				"No tiene permisos para realizar esta acción");
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException excepcion) {
		String mensaje = excepcion.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse("Datos inválidos");
		ErrorResponse error = ErrorResponse.de(HttpStatus.BAD_REQUEST, mensaje);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> manejarRutaNoEncontrada(NoResourceFoundException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.NOT_FOUND, "El recurso solicitado no existe");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> manejarCuerpoIlegible(HttpMessageNotReadableException excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> manejarErrorGeneral(Exception excepcion) {
		ErrorResponse error = ErrorResponse.de(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
