package com.shreejewellers.billing.controller;

import com.shreejewellers.billing.entity.Invoice;
import com.shreejewellers.billing.entity.InvoiceStatus;
import com.shreejewellers.billing.service.InvoiceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/billing/invoices")
public class SmsController {
 private final InvoiceService invoices;
 private final RestClient client = RestClient.create();
 private final boolean enabled;
 private final String authKey, flowId, senderId, countryCode;
 public SmsController(InvoiceService invoices,
  @Value("${app.sms.enabled:false}") boolean enabled,
  @Value("${app.sms.auth-key:}") String authKey,
  @Value("${app.sms.flow-id:}") String flowId,
  @Value("${app.sms.sender-id:}") String senderId,
  @Value("${app.sms.country-code:91}") String countryCode) {
  this.invoices=invoices;this.enabled=enabled;this.authKey=authKey;
  this.flowId=flowId;this.senderId=senderId;this.countryCode=countryCode;
 }
 public record SmsRequest(
  @NotBlank @Pattern(regexp="^[+]?[0-9]{10,15}$") String phone
 ) {}
 @PostMapping("/{id}/send-sms")
 public ResponseEntity<Map<String,String>> send(@PathVariable Long id,@Valid @RequestBody SmsRequest request) {
  if(!enabled||authKey.isBlank()||flowId.isBlank()||senderId.isBlank())
   throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"MSG91 SMS is not configured or enabled");
  Invoice invoice=invoices.get(id);
  if(invoice.status!=InvoiceStatus.DRAFT)
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Only active draft estimates can be sent");
  String mobile=request.phone().replace("+","");
  if(mobile.length()==10)mobile=countryCode+mobile;
  Map<String,Object> payload=Map.of(
   "flow_id",flowId,"sender",senderId,"mobiles",mobile,
   "VAR1",invoice.invoiceNumber,"VAR2",invoice.grandTotal.toPlainString()
  );
  try {
   String response=client.post().uri("https://control.msg91.com/api/v5/flow/")
    .header("authkey",authKey).contentType(MediaType.APPLICATION_JSON)
    .body(payload).retrieve().body(String.class);
   if(response==null||!response.matches("(?s).*\\\"type\\\"\\s*:\\s*\\\"success\\\".*"))
    throw new IllegalStateException("MSG91 did not accept the SMS request");
  } catch(Exception ex) {
   throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"MSG91 SMS request failed",ex);
  }
  return ResponseEntity.ok(Map.of("status","SUBMITTED","channel","SMS"));
 }
}
