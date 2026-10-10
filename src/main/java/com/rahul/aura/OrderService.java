package com.rahul.aura;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class OrderService {
 public record Order(String order_id,String customer,String product,int value,String status,String courier,String tracking,String notes,Integer days_since_delivery) {}
 private final Map<String,Order> orders=new LinkedHashMap<>();
 public OrderService(){
  orders.put("ORD-101",new Order("ORD-101","Priya Sharma","Vitamin C Serum (30ml)",699,"Out for Delivery","BlueDart","BD-982103","Expected by 6 PM today",null));
  orders.put("ORD-102",new Order("ORD-102","Rahul Verma","Hydrating Sunscreen SPF 50",499,"Delivered","Delhivery","DL-441029","Delivered 14 days ago",14));
  orders.put("ORD-103",new Order("ORD-103","Ananya Patel","Green Tea Face Wash + Toner",850,"Processing","","","Ordered 3 hours ago. Eligible for cancellation",null));
 }
 public List<Order> samples(){ return List.copyOf(orders.values()); }
 public static String normalize(String id){
  String s=id==null?"":id.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]","");
  if(s.matches("(?:ORD)?[0-9]{3}")) return "ORD-"+s.replace("ORD","");
  return s;
 }
 public Map<String,Object> lookup(String id){
  String key=normalize(id); Order o=orders.get(key);
  if(o==null) return Map.of("error",key.isBlank()?"MISSING_ORDER_ID":"ORDER_NOT_FOUND","order_id",key,"instruction","Ask the customer to repeat or verify the ID. Do not invent details.");
  return Map.of("order",o,"cancellation_eligible",o.status().equals("Processing"),
    "return_window",o.days_since_delivery()==null?"NOT_DELIVERED":o.days_since_delivery()>7?"EXPIRED":"WITHIN_WINDOW",
    "return_conditions","Must also be unopened, unused and in original packaging.",
    "action_performed",false,"instruction","Lookup only. Do not claim a cancellation, refund or replacement has been performed.");
 }
}
