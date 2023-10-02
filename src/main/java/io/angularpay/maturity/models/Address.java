
package io.angularpay.maturity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Address {

    @JsonProperty("address_lines")
    private List<String> addressLines;
    private String city;
    private String country;
    private String reference;
    private String state;
}
