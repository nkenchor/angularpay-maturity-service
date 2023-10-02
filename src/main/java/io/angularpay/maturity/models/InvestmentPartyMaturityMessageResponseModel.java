package io.angularpay.maturity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InvestmentPartyMaturityMessageResponseModel {
    @JsonProperty("user_reference")
    private String userReference;
    private MaturityMessage message;
}
