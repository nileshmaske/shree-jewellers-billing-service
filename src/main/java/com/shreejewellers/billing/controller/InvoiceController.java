package com.shreejewellers.billing.controller;
import com.shreejewellers.billing.dto.InvoiceRequest;
import com.shreejewellers.billing.entity.Invoice;
import com.shreejewellers.billing.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/billing/invoices")
public class InvoiceController {
 private final InvoiceService service;
 public InvoiceController(InvoiceService service){this.service=service;}
 @PostMapping public ResponseEntity<Invoice> create(@Valid @RequestBody InvoiceRequest request,Authentication authentication){
  return ResponseEntity.status(HttpStatus.CREATED).body(service.createDraft(request,authentication.getName()));
 }
 @GetMapping("/{id}") public Invoice get(@PathVariable Long id){return service.get(id);}
 @GetMapping public Page<Invoice> list(@PageableDefault(size=20) Pageable pageable){return service.list(pageable);}
 @PostMapping("/{id}/cancel") public Invoice cancel(@PathVariable Long id){return service.cancelDraft(id);}
}
