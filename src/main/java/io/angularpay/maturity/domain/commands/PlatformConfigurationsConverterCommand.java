package io.angularpay.maturity.domain.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.maturity.domain.Role;
import io.angularpay.maturity.exceptions.CommandException;
import io.angularpay.maturity.models.*;
import io.angularpay.maturity.models.platform.*;
import io.angularpay.maturity.ports.outbound.OutboundMessagingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.angularpay.maturity.adapters.inbound.RestApiAdapter.PAYNABLE_NAME;
import static io.angularpay.maturity.adapters.inbound.RestApiAdapter.PEER_FUND_NAME;
import static io.angularpay.maturity.exceptions.ErrorCode.INVALID_MESSAGE_ERROR;
import static io.angularpay.maturity.models.platform.PlatformConfigurationSource.HASH;

@Slf4j
@Service
public class PlatformConfigurationsConverterCommand {

    private final PlatformConfigurations platformConfigurations;
    private final ObjectMapper mapper;
    private final UpdateCronCommand updateCronCommand;
    private final UpdateSlaInDaysCommand updateSlaInDaysCommand;

    public PlatformConfigurationsConverterCommand(
            PlatformConfigurations platformConfigurations,
            ObjectMapper mapper,
            OutboundMessagingPort outboundMessagingPort,
            UpdateCronCommand updateCronCommand,
            UpdateSlaInDaysCommand updateSlaInDaysCommand) {
        this.platformConfigurations = platformConfigurations;
        this.mapper = mapper;
        this.updateCronCommand = updateCronCommand;
        this.updateSlaInDaysCommand = updateSlaInDaysCommand;

        log.info("reading platform configurations from message hash...");
        Map<String, String> configurations = outboundMessagingPort.getPlatformConfigurations(
                PlatformConfigurationIdentifier.getHashName()
        );
        if (CollectionUtils.isEmpty(configurations)) {
            log.info("platform configurations message hash is empty");
        } else {
            log.info("read {} items from platform configurations message hash", configurations.size());
            configurations.forEach((key, value) -> {
                PlatformConfigurationIdentifier.fromHashField(key).ifPresent(identifier -> {
                    this.execute(value, identifier, HASH);
                });
            });
        }
    }

    public void execute(String message, PlatformConfigurationIdentifier identifier, PlatformConfigurationSource source) {
        log.info("updating global platform configurations {} values from {} -> {}", identifier.name(), source.name(), message);
        try {
            switch (identifier) {
                case PLATFORM_BANKS:
                    List<PlatformBank> platformBanks = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformBanks(platformBanks);
                    break;
                case PLATFORM_COUNTRIES:
                    List<PlatformCountry> platformCountries = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformCountries(platformCountries);
                    break;
                case PLATFORM_COUNTRY_FEATURES:
                    List<PlatformCountryFeature> platformCountryFeatures = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformCountryFeatures(platformCountryFeatures);
                    break;
                case PLATFORM_CURRENCIES:
                    List<PlatformCurrency> platformCurrencies = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformCurrencies(platformCurrencies);
                    break;
                case PLATFORM_MATURITY_CONFIGURATIONS:
                    List<PlatformMaturityConfiguration> platformMaturityConfigurations = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformMaturityConfigurations(platformMaturityConfigurations);
                    this.updateRuntimeMaturityConfiguration(platformMaturityConfigurations);
                    break;
                case PLATFORM_NOTIFICATION_TYPES:
                    List<PlatformNotificationType> platformNotificationTypes = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformNotificationTypes(platformNotificationTypes);
                    break;
                case PLATFORM_OTP_TYPES:
                    List<PlatformOTPType> platformOTPTypes = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformOTPTypes(platformOTPTypes);
                    break;
                case PLATFORM_SERVICES:
                    List<PlatformService> platformServices = mapper.readValue(message, new TypeReference<>() {
                    });
                    this.platformConfigurations.setPlatformServices(platformServices);
                    break;
                case PLATFORM_TTL_CONFIGURATION:
                    PlatformTTLConfiguration platformTTLConfiguration = mapper.readValue(message, PlatformTTLConfiguration.class);
                    this.platformConfigurations.setPlatformTTLConfiguration(platformTTLConfiguration);
                    break;
            }
        } catch (Exception exception) {
            log.error("An error occurred while processing platform configurations {} value from {}", identifier.name(), source.name(), exception);
            throw new RuntimeException(CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(INVALID_MESSAGE_ERROR)
                    .message(exception.getMessage())
                    .cause(exception)
                    .build());
        }
    }

    private void updateRuntimeMaturityConfiguration(List<PlatformMaturityConfiguration> platformMaturityConfigurations) {
        if (!CollectionUtils.isEmpty(platformMaturityConfigurations)) {
            AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                    .username("service_account")
                    .userReference("service_account")
                    .deviceId("service_account")
                    .correlationId(UUID.randomUUID().toString())
                    .roles(Collections.singletonList(Role.ROLE_MATURITY_ADMIN))
                    .build();

            platformMaturityConfigurations.forEach(maturityConfig -> {
                SlaInDaysUpdateApiModel slaInDaysUpdateApiModel = new SlaInDaysUpdateApiModel();
                slaInDaysUpdateApiModel.setDays(maturityConfig.getSlaDays());

                CronUpdateApiModel cronUpdateApiModel = new CronUpdateApiModel();
                cronUpdateApiModel.setCron(maturityConfig.getCron());

                if ("PNB".equalsIgnoreCase(maturityConfig.getServiceCode())) {
                    GenericSlaInDaysUpdateCommandRequest genericSlaInDaysUpdateCommandRequest = GenericSlaInDaysUpdateCommandRequest.builder()
                            .name(PAYNABLE_NAME)
                            .slaInDaysUpdateApiModel(slaInDaysUpdateApiModel)
                            .authenticatedUser(authenticatedUser)
                            .build();
                    this.updateSlaInDaysCommand.execute(genericSlaInDaysUpdateCommandRequest);

                    GenericCronUpdateCommandRequest genericCronUpdateCommandRequest = GenericCronUpdateCommandRequest.builder()
                            .name(PAYNABLE_NAME)
                            .cronUpdateApiModel(cronUpdateApiModel)
                            .authenticatedUser(authenticatedUser)
                            .build();
                    this.updateCronCommand.execute(genericCronUpdateCommandRequest);
                }

                if ("PEF".equalsIgnoreCase(maturityConfig.getServiceCode())) {
                    GenericSlaInDaysUpdateCommandRequest genericSlaInDaysUpdateCommandRequest = GenericSlaInDaysUpdateCommandRequest.builder()
                            .name(PEER_FUND_NAME)
                            .slaInDaysUpdateApiModel(slaInDaysUpdateApiModel)
                            .authenticatedUser(authenticatedUser)
                            .build();
                    this.updateSlaInDaysCommand.execute(genericSlaInDaysUpdateCommandRequest);

                    GenericCronUpdateCommandRequest genericCronUpdateCommandRequest = GenericCronUpdateCommandRequest.builder()
                            .name(PEER_FUND_NAME)
                            .cronUpdateApiModel(cronUpdateApiModel)
                            .authenticatedUser(authenticatedUser)
                            .build();
                    this.updateCronCommand.execute(genericCronUpdateCommandRequest);
                }
            });
        }
    }
}
