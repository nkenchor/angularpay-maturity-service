package io.angularpay.maturity.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GenericCronRunCommandRequest extends AccessControl {

    @NotEmpty
    private String name;

    GenericCronRunCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
