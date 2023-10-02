package io.angularpay.maturity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.maturity.configurations.AngularPayConfiguration;
import io.angularpay.maturity.domain.Role;
import io.angularpay.maturity.exceptions.CommandException;
import io.angularpay.maturity.exceptions.ErrorObject;
import io.angularpay.maturity.models.GenericCronUpdateCommandRequest;
import io.angularpay.maturity.scheduler.MaturityCronConfiguration;
import io.angularpay.maturity.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.maturity.exceptions.ErrorCode.INVALID_CRON_EXPRESSION;

@Slf4j
@Service
public class UpdateCronCommand extends AbstractCommand<GenericCronUpdateCommandRequest, AngularPayConfiguration.MaturityCandidates> {

    private final DefaultConstraintValidator validator;
    private final AngularPayConfiguration angularPayConfiguration;
    private final MaturityCronConfiguration maturityCronConfiguration;

    public UpdateCronCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            AngularPayConfiguration angularPayConfiguration,
            MaturityCronConfiguration maturityCronConfiguration) {
        super("UpdateCronCommand", mapper);
        this.validator = validator;
        this.angularPayConfiguration = angularPayConfiguration;
        this.maturityCronConfiguration = maturityCronConfiguration;
    }

    @Override
    protected AngularPayConfiguration.MaturityCandidates handle(GenericCronUpdateCommandRequest request) {
        if (!CronExpression.isValidExpression(request.getCronUpdateApiModel().getCron())) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(INVALID_CRON_EXPRESSION)
                    .message(INVALID_CRON_EXPRESSION.getDefaultMessage())
                    .build();
        }
        AngularPayConfiguration.MaturityCandidates candidates = null;
        for (AngularPayConfiguration.MaturityCandidates x : this.angularPayConfiguration.getMaturityCandidates()) {
            if (x.getName().equalsIgnoreCase(request.getName())) {
                x.setCron(request.getCronUpdateApiModel().getCron());
                this.maturityCronConfiguration.refreshCronJob(x);
                candidates = x;
            }
        }
        return candidates;
    }

    @Override
    protected List<ErrorObject> validate(GenericCronUpdateCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_MATURITY_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
