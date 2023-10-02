package io.angularpay.maturity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.angularpay.maturity.domain.RepaymentStatus;
import lombok.Data;

@Data
public class UnpaidPeerFundResponseModel {
    private String reference;
    @JsonProperty("matures_on")
    private String maturesOn;
    private RepaymentStatus status;
}
