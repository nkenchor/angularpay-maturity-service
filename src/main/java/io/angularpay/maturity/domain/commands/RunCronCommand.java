package io.angularpay.maturity.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.maturity.adapters.outbound.MaturityCandidateServiceAdapter;
import io.angularpay.maturity.adapters.outbound.NotificationServiceAdapter;
import io.angularpay.maturity.adapters.outbound.UserConfigurationServiceAdapter;
import io.angularpay.maturity.configurations.AngularPayConfiguration;
import io.angularpay.maturity.domain.Role;
import io.angularpay.maturity.exceptions.CommandException;
import io.angularpay.maturity.exceptions.ErrorObject;
import io.angularpay.maturity.models.*;
import io.angularpay.maturity.ports.outbound.MaturityCandidateServicePort;
import io.angularpay.maturity.ports.outbound.NotificationServicePort;
import io.angularpay.maturity.ports.outbound.UserConfigurationServicePort;
import io.angularpay.maturity.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.angularpay.maturity.common.Constants.MATURITY_NOTIFICATION_SENDER;
import static io.angularpay.maturity.exceptions.ErrorCode.*;
import static io.angularpay.maturity.helpers.Helper.daysUntilMaturity;
import static io.angularpay.maturity.helpers.Helper.writeAsStringOrDefault;
import static io.angularpay.maturity.models.NotificationChannel.EMAIL;
import static io.angularpay.maturity.models.NotificationChannel.SMS;
import static io.angularpay.maturity.models.NotificationType.INSTANT;

@Slf4j
@Service
public class RunCronCommand extends AbstractCommand<GenericCronRunCommandRequest, Void> {

    private final DefaultConstraintValidator validator;
    private final MaturityCandidateServicePort maturityCandidateServicePort;
    private final AngularPayConfiguration angularPayConfiguration;
    private final NotificationServicePort notificationServiceAdapter;
    private final UserConfigurationServicePort userConfigurationServicePort;

    public RunCronCommand(
            ObjectMapper mapper,
            DefaultConstraintValidator validator,
            MaturityCandidateServiceAdapter maturityCandidateServiceAdapter,
            AngularPayConfiguration angularPayConfiguration,
            NotificationServiceAdapter notificationServiceAdapter,
            UserConfigurationServiceAdapter userConfigurationServiceAdapter) {
        super("RunCronCommand", mapper);
        this.validator = validator;
        this.maturityCandidateServicePort = maturityCandidateServiceAdapter;
        this.angularPayConfiguration = angularPayConfiguration;
        this.notificationServiceAdapter = notificationServiceAdapter;
        this.userConfigurationServicePort = userConfigurationServiceAdapter;
    }

    @Override
    protected Void handle(GenericCronRunCommandRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-angularpay-username", request.getAuthenticatedUser().getUsername());
        headers.put("x-angularpay-user-reference", request.getAuthenticatedUser().getUserReference());
        headers.put("x-angularpay-device-id", request.getAuthenticatedUser().getDeviceId());
        headers.put("x-angularpay-correlation-id", request.getAuthenticatedUser().getCorrelationId());

        List<UnpaidFacilityResponseModel> unpaidFacilityResponseModels = this.maturityCandidateServicePort.getUnpaidFacilityRequests(request.getName(), headers);

        if (CollectionUtils.isEmpty(unpaidFacilityResponseModels)) return null;

        List<UnpaidFacilityResponseModel> filteredUnpaidFacilityResponseModel = unpaidFacilityResponseModels.stream()
                .filter(x -> {
                    long difference = daysUntilMaturity(x.getMaturesOn());
                    return difference <= getSlaDays(request.getName());
                })
                .collect(Collectors.toList());
        log.info("maturing {} facilities: {}", request.getName(), writeAsStringOrDefault(mapper, filteredUnpaidFacilityResponseModel));

        filteredUnpaidFacilityResponseModel.stream().parallel()
                .forEach(unpaidFacility -> {

                    InvestmentPartiesRequest parameters = InvestmentPartiesRequest.builder()
                            .name(request.getName())
                            .requestReference(unpaidFacility.getReference())
                            .build();
                    List<InvestmentPartyMaturityMessageResponseModel> responseModelList = this.maturityCandidateServicePort.getInvestmentPartiesMaturityMessages(parameters, headers);

                    log.info("notification messages for maturing {} facilities: {}", request.getName(), writeAsStringOrDefault(mapper, responseModelList));

                    if (CollectionUtils.isEmpty(responseModelList)) {
                        throw CommandException.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .errorCode(MATURITY_CANDIDATE_MESSAGE_ERROR)
                                .message(MATURITY_CANDIDATE_MESSAGE_ERROR.getDefaultMessage())
                                .build();
                    }

                    responseModelList.stream().parallel().forEach(responseModel -> {
                        UserProfileResponseModel userProfile = userConfigurationServicePort.getUserProfile(responseModel.getUserReference(), headers)
                                .orElseThrow(() -> CommandException.builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .errorCode(USERCONFIG_SERVICE_ERROR)
                                        .message(USERCONFIG_SERVICE_ERROR.getDefaultMessage())
                                        .build()
                                );
                        Optional<SendNotificationResponseApiModel> smsNotification = this.notificationServiceAdapter.sendNotification(SendNotificationRequestApiModel.builder()
                                .channel(SMS)
                                .type(INSTANT)
                                .from(MATURITY_NOTIFICATION_SENDER)
                                .to(userProfile.getPhone())
                                .message(responseModel.getMessage().getSms())
                                .build(),
                                headers
                        );

                        Optional<SendNotificationResponseApiModel> emailNotification = this.notificationServiceAdapter.sendNotification(SendNotificationRequestApiModel.builder()
                                .channel(EMAIL)
                                .type(INSTANT)
                                .from(MATURITY_NOTIFICATION_SENDER)
                                .to(userProfile.getEmail())
                                .message(responseModel.getMessage().getSms())
                                .build(),
                                headers
                        );

                        if (smsNotification.isEmpty() && emailNotification.isEmpty()) {
                            throw CommandException.builder()
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .errorCode(NOTIFICATION_SERVICE_ERROR)
                                    .message(NOTIFICATION_SERVICE_ERROR.getDefaultMessage())
                                    .build();
                        }
                    });
                });
        return null;
    }

    @Override
    protected List<ErrorObject> validate(GenericCronRunCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_MATURITY_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }

    private int getSlaDays(String name) {
        return this.angularPayConfiguration.getMaturityCandidates().stream()
                .filter(x -> x.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> CommandException.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .errorCode(SERVICE_CONFIGURATION_ERROR)
                        .message(SERVICE_CONFIGURATION_ERROR.getDefaultMessage())
                        .build()
                )
                .getSlaInDays();
    }
}
