package com.oneneev.plotrix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignalRequest {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    private String city; // "mumbai", "delhi", "bangalore" etc — always lowercase

    private String propertyType = "any"; // "apartment", "villa", "plot"
}
