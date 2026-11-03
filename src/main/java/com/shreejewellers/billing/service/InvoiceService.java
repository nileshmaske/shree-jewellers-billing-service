package com.shreejewellers.billing.service;
import com.shreejewellers.billing.dto.InvoiceRequest;
import com.shreejewellers.billing.entity.*;
import com.shreejewellers.billing.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.util.UUID;
@Service
public class InvoiceService {
 private final InvoiceRepository repository;
 private final BigDecimal gstRate;
 public InvoiceService(InvoiceRepository repository,@Value("${app.billing.gst-rate}") BigDecimal gstRate){
  if(gstRate.signum()<0||gstRate.compareTo(new BigDecimal("100"))>0) throw new IllegalArgumentException("GST rate must be between 0 and 100");
  this.repository=repository;this.gstRate=gstRate;
 }
 @Transactional
 public Invoice createDraft(InvoiceRequest request,String username){
  Invoice invoice=new Invoice();
  invoice.invoiceNumber="DRAFT-"+UUID.randomUUID().toString().replace("-","").substring(0,20).toUpperCase();
  invoice.customerName=request.customerName().trim();
  invoice.customerPhone=request.customerPhone();
  invoice.status=InvoiceStatus.DRAFT;
  invoice.createdBy=username;
  BigDecimal subtotal=BigDecimal.ZERO;
  for(InvoiceRequest.Item requestItem:request.items()){
   InvoiceItem item=new InvoiceItem();
   item.invoice=invoice;item.productId=requestItem.productId();item.sku=requestItem.sku().trim();
   item.description=requestItem.description().trim();item.quantity=requestItem.quantity();
   item.unitPrice=requestItem.unitPrice().setScale(2,RoundingMode.HALF_UP);
   item.lineTotal=item.unitPrice.multiply(BigDecimal.valueOf(item.quantity())).setScale(2,RoundingMode.HALF_UP);
   subtotal=subtotal.add(item.lineTotal);
   invoice.items.add(item);
  }
  BigDecimal discount=request.discount().setScale(2,RoundingMode.HALF_UP);
  if(discount.compareTo(subtotal)>0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Discount cannot exceed subtotal");
  invoice.subtotal=subtotal.setScale(2,RoundingMode.HALF_UP);
  invoice.discount=discount;
  invoice.taxableAmount=subtotal.subtract(discount).setScale(2,RoundingMode.HALF_UP);
  invoice.gstRate=gstRate;
  invoice.gstAmount=invoice.taxableAmount.multiply(gstRate).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
  invoice.grandTotal=invoice.taxableAmount.add(invoice.gstAmount);
  return repository.save(invoice);
 }
 @Transactional(readOnly=true)
 public Invoice get(Long id){return repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Invoice not found"));}
 @Transactional(readOnly=true)
 public Page<Invoice> list(Pageable pageable){return repository.findAllByOrderByIdDesc(pageable);}
 @Transactional
 public Invoice cancelDraft(Long id){
  Invoice invoice=get(id);
  if(invoice.status!=InvoiceStatus.DRAFT) throw new ResponseStatusException(HttpStatus.CONFLICT,"Only drafts may be cancelled");
  invoice.status=InvoiceStatus.CANCELLED;
  return invoice;
 }
}
