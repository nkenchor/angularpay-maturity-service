package io.angularpay.maturity.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GenericSlaInDaysUpdateCommandRequest extends AccessControl {

    @NotEmpty
    private String name;

    @NotNull
    @Valid
    private SlaInDaysUpdateApiModel slaInDaysUpdateApiModel;

    GenericSlaInDaysUpdateCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
