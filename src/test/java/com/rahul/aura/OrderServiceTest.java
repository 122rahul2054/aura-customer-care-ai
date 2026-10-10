package com.rahul.aura;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class OrderServiceTest {
 final OrderService service=new OrderService();
 @Test void normalizesSpokenIds(){assertEquals("ORD-101",OrderService.normalize("ord 101"));assertTrue(service.lookup("101").containsKey("order"));}
 @Test void invalidAndMissingAreExplicit(){assertEquals("ORDER_NOT_FOUND",service.lookup("ORD-999").get("error"));assertEquals("MISSING_ORDER_ID",service.lookup("").get("error"));}
 @Test void eligibilityUsesBackendFacts(){assertEquals(false,service.lookup("ORD-101").get("cancellation_eligible"));assertEquals(true,service.lookup("ORD-103").get("cancellation_eligible"));assertEquals("EXPIRED",service.lookup("ORD-102").get("return_window"));assertEquals(false,service.lookup("ORD-103").get("action_performed"));}
}
