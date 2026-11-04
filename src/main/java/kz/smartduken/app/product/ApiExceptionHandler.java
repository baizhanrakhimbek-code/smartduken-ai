package kz.smartduken.app.product;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.LinkedHashMap;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(ProductNotFoundException.class)
 ResponseEntity<ProblemDetail> missing(ProductNotFoundException ex) { return problem(HttpStatus.NOT_FOUND, ex.getMessage()); }
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
  ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Енгізілген деректерді тексеріңіз");
  var errors = new LinkedHashMap<String, String>();
  ex.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
  detail.setProperty("errors", errors); return ResponseEntity.badRequest().body(detail);
 }
 @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
 ResponseEntity<ProblemDetail> invalid(Exception ex) { return problem(HttpStatus.BAD_REQUEST, "JSON пішімін және өріс түрлерін тексеріңіз"); }
 private ResponseEntity<ProblemDetail> problem(HttpStatus status, String message) {
  return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, message));
 }
}
