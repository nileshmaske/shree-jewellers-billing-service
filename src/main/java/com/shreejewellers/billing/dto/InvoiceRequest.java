package com.shreejewellers.billing.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
public record InvoiceRequest(
 @NotBlank @Size(max=160) String customerName,
 @Size(max=25) String customerPhone,
 @NotNull @DecimalMin("0.00") @Digits(integer=14,fraction=2) BigDecimal discount,
 @NotEmpty List<@Valid Item> items
){
 public record Item(
  @NotNull @Positive Long productId,
  @NotBlank @Size(max=60) String sku,
  @NotBlank @Size(max=200) String description,
  @NotNull @Min(1) @Max(10000) Integer quantity,
  @NotNull @DecimalMin("0.00") @Digits(integer=14,fraction=2) BigDecimal unitPrice
 ){}
}
