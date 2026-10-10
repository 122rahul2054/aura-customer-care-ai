package com.rahul.aura;
import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
class AiServiceTest {
 @Test void sendsRealHttpContractWithoutExposingKey() throws Exception {
  var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);var request=new AtomicReference<String>();var auth=new AtomicReference<String>();
  server.createContext("/models/test:generateContent",exchange->{request.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));auth.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));byte[] response="{\"candidates\":[{\"content\":{\"role\":\"model\",\"parts\":[{\"text\":\"Hello\"}]}}]}".getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(200,response.length);exchange.getResponseBody().write(response);exchange.close();});server.start();
  try{var json=new ObjectMapper();var ai=new AiService(json,"test-key","test","http://127.0.0.1:"+server.getAddress().getPort());var history=json.createArrayNode();history.addObject().put("role","user").putArray("parts").addObject().put("text","Hello");assertEquals("Hello",AiService.text(ai.generate(history,true,null)));assertEquals("test-key",auth.get());assertTrue(request.get().contains("get_order_details"));assertFalse(request.get().contains("test-key"));}finally{server.stop(0);}
 }
}
