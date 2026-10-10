package com.rahul.aura;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
class CallServiceTest {
 final ObjectMapper json=new ObjectMapper();
 @Test void executesToolPreservesSignaturesAndSummarizes() throws Exception {
  AiService ai=mock(AiService.class);when(ai.configured()).thenReturn(true);
  when(ai.generate(any(),eq(true),isNull())).thenReturn(
   json.readTree("{\"role\":\"model\",\"parts\":[{\"functionCall\":{\"name\":\"get_order_details\",\"id\":\"f1\",\"args\":{\"order_id\":\"ORD-101\"}},\"thoughtSignature\":\"opaque-signature\"}]}"),
   json.readTree("{\"role\":\"model\",\"parts\":[{\"text\":\"Your order is out for delivery, expected by 6 PM today.\"}]}"));
  when(ai.generate(any(),eq(false),any())).thenReturn(json.readTree("{\"role\":\"model\",\"parts\":[{\"text\":\"{\\\"customer_intent\\\":\\\"ORDER_TRACKING\\\",\\\"order_id\\\":\\\"ORD-101\\\",\\\"resolution_status\\\":\\\"RESOLVED\\\",\\\"call_summary\\\":\\\"Tracking provided.\\\"}\"}]}"));
  CallService calls=new CallService(json,ai,new OrderService());String id=calls.start().get("callId");
  var turn=calls.chat(id,"Where is ORD-101?");assertEquals(1,turn.tool_calls().size());assertEquals(3,turn.transcript().size());
  ArgumentCaptor<ArrayNode> history=ArgumentCaptor.forClass(ArrayNode.class);verify(ai,times(2)).generate(history.capture(),eq(true),isNull());
  assertTrue(history.getValue().toString().contains("opaque-signature"));assertTrue(history.getValue().toString().contains("BD-982103"));assertTrue(history.getValue().toString().contains("functionResponse"));
  var end=calls.end(id);assertEquals("ORD-101",end.summary().order_id());assertSame(end,calls.end(id));assertThrows(ResponseStatusException.class,()->calls.chat(id,"hello"));
 }
 @Test void summaryFailureIsNotFakeSuccess(){AiService ai=mock(AiService.class);when(ai.configured()).thenReturn(true);when(ai.generate(any(),eq(true),isNull())).thenThrow(new IllegalStateException("offline"));CallService c=new CallService(json,ai,new OrderService());String id=c.start().get("callId");assertThrows(IllegalStateException.class,()->c.chat(id,"hello"));assertEquals("UNRESOLVED",c.end(id).summary().resolution_status());}
 @Test void separatesSessionsAndHandlesEmptyCall(){AiService ai=mock(AiService.class);when(ai.configured()).thenReturn(true);CallService c=new CallService(json,ai,new OrderService());String a=c.start().get("callId"),b=c.start().get("callId");assertNotEquals(a,b);assertEquals(1,c.end(b).transcript().size());assertEquals("UNRESOLVED",c.end(a).summary().resolution_status());}
 @Test void requiresConfiguration(){AiService ai=mock(AiService.class);assertThrows(ResponseStatusException.class,()->new CallService(json,ai,new OrderService()).start());}
}
