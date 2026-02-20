package com.currencyx.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ConvertRequest {

    @NotBlank(message = "Source currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String fromCurrency;

    @NotBlank(message = "Target currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String toCurrency;

    @NotNull(message = "Conversion amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    public ConvertRequest() {
    }

    public ConvertRequest(String fromCurrency, String toCurrency, BigDecimal amount) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
    }
}
