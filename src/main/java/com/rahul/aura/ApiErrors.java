package com.rahul.aura;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<?> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("error",e.getReason()==null?"Request failed":e.getReason()));}
 @ExceptionHandler(IllegalStateException.class) public ResponseEntity<?> provider(IllegalStateException e){return ResponseEntity.status(503).body(Map.of("error",e.getMessage()==null?"AI unavailable":e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validation(){return ResponseEntity.badRequest().body(Map.of("error","A call ID and a message of 1–2000 characters are required."));}
}
