package com.shreejewellers.billing.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="invoice_items")
public class InvoiceItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @JsonIgnore @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="invoice_id") public Invoice invoice;
 @Column(name="product_id",nullable=false) public Long productId;
 @Column(nullable=false,length=60) public String sku;
 @Column(nullable=false,length=200) public String description;
 @Column(nullable=false) public Integer quantity;
 @Column(name="unit_price",nullable=false,precision=16,scale=2) public BigDecimal unitPrice;
 @Column(name="line_total",nullable=false,precision=16,scale=2) public BigDecimal lineTotal;
}
