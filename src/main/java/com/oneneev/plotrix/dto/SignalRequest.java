package com.oneneev.plotrix.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignalRequest {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String propertyType = "any"; // "apartment", "villa", "plot"
}
