package io.angularpay.maturity.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvestmentPartiesRequest {
    private String name;
    private String requestReference;
}
