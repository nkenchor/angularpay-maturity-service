
package io.angularpay.maturity.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CronUpdateApiModel {

    @NotEmpty
    private String cron;
}
