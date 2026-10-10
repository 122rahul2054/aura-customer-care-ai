package com.rahul.aura;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api")
public class ApiController {
 private final CallService calls;
 private final OrderService orders;
 private final AiService ai;
 public ApiController(CallService calls,OrderService orders,AiService ai){
  this.calls=calls;
  this.orders=orders;
  this.ai=ai;
 }
 public record ChatRequest(@NotBlank @Size(max=64) String callId,@NotBlank @Size(max=2000) String message){

 }
 @GetMapping("/health") public Map<String,Object> health(){
  return Map.of("status","ok","aiConfigured",ai.configured());
 }
 @GetMapping("/orders/samples") public List<OrderService.Order> orders(){return orders.samples();}
 @PostMapping("/calls") public Map<String,String> start(){return calls.start();}
 @PostMapping("/chat") public CallService.Turn chat(@Valid @RequestBody ChatRequest request){return calls.chat(request.callId(),request.message());}
 @PostMapping("/calls/{id}/end") public CallService.End end(@PathVariable String id){return calls.end(id);}
}
