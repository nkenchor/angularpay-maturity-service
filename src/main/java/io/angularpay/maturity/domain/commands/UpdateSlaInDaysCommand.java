package io.angularpay.maturity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.maturity.configurations.AngularPayConfiguration;
import io.angularpay.maturity.domain.Role;
import io.angularpay.maturity.exceptions.CommandException;
import io.angularpay.maturity.exceptions.ErrorObject;
import io.angularpay.maturity.models.GenericSlaInDaysUpdateCommandRequest;
import io.angularpay.maturity.scheduler.MaturityCronConfiguration;
import io.angularpay.maturity.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.maturity.exceptions.ErrorCode.INVALID_SLA_DAYS_ERROR;

@Slf4j
@Service
public class UpdateSlaInDaysCommand extends AbstractCommand<GenericSlaInDaysUpdateCommandRequest, AngularPayConfiguration.MaturityCandidates> {

    private final DefaultConstraintValidator validator;
    private final AngularPayConfiguration angularPayConfiguration;
    private final MaturityCronConfiguration maturityCronConfiguration;

    public UpdateSlaInDaysCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            AngularPayConfiguration angularPayConfiguration,
            MaturityCronConfiguration maturityCronConfiguration) {
        super("UpdateSlaInDaysCommand", mapper);
        this.validator = validator;
        this.angularPayConfiguration = angularPayConfiguration;
        this.maturityCronConfiguration = maturityCronConfiguration;
    }

    @Override
    protected AngularPayConfiguration.MaturityCandidates handle(GenericSlaInDaysUpdateCommandRequest request) {
        if (request.getSlaInDaysUpdateApiModel().getDays() <= 0) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(INVALID_SLA_DAYS_ERROR)
                    .message(INVALID_SLA_DAYS_ERROR.getDefaultMessage())
                    .build();
        }
        AngularPayConfiguration.MaturityCandidates candidates = null;
        for (AngularPayConfiguration.MaturityCandidates x : this.angularPayConfiguration.getMaturityCandidates()) {
            if (x.getName().equalsIgnoreCase(request.getName())) {
                x.setSlaInDays(request.getSlaInDaysUpdateApiModel().getDays());
                this.maturityCronConfiguration.refreshCronJob(x);
                candidates = x;
            }
        }
        return candidates;
    }

    @Override
    protected List<ErrorObject> validate(GenericSlaInDaysUpdateCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_MATURITY_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
