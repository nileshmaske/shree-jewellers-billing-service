package com.shreejewellers.billing.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="invoices")
public class Invoice {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="invoice_number",nullable=false,unique=true,length=40) public String invoiceNumber;
 @Column(name="customer_name",nullable=false,length=160) public String customerName;
 @Column(name="customer_phone",length=25) public String customerPhone;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=25) public InvoiceStatus status;
 @Column(nullable=false,precision=16,scale=2) public BigDecimal subtotal;
 @Column(nullable=false,precision=16,scale=2) public BigDecimal discount;
 @Column(name="taxable_amount",nullable=false,precision=16,scale=2) public BigDecimal taxableAmount;
 @Column(name="gst_rate",nullable=false,precision=5,scale=2) public BigDecimal gstRate;
 @Column(name="gst_amount",nullable=false,precision=16,scale=2) public BigDecimal gstAmount;
 @Column(name="grand_total",nullable=false,precision=16,scale=2) public BigDecimal grandTotal;
 @Column(name="created_by",nullable=false,length=160) public String createdBy;
 @Column(name="created_at",insertable=false,updatable=false) public LocalDateTime createdAt;
 @Column(name="updated_at",insertable=false,updatable=false) public LocalDateTime updatedAt;
 @OneToMany(mappedBy="invoice",cascade=CascadeType.ALL,orphanRemoval=true)
 public List<InvoiceItem> items=new ArrayList<>();
}
