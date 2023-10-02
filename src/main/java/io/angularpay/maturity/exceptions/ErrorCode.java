package io.angularpay.maturity.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_MESSAGE_ERROR("The message format read from the given topic is invalid"),
    INVALID_CRON_EXPRESSION("The CRON expression provided is invalid"),
    INVALID_SLA_DAYS_ERROR("The SLA days provided is invalid"),
    VALIDATION_ERROR("The request has validation errors"),
    NOTIFICATION_SERVICE_ERROR("Unable to send notification. Please check notification-service logs for details."),
    USERCONFIG_SERVICE_ERROR("Unable to send notification. Please check userconfig-service logs for details."),
    SERVICE_CONFIGURATION_ERROR("Invalid service configuration. The service is misconfigured. Please provide correct configuration values."),
    MATURITY_CANDIDATE_MESSAGE_ERROR("The maturity candidate service returned an invalid message response. "),
    REQUEST_NOT_FOUND("The requested resource was NOT found"),
    GENERIC_ERROR("Generic error occurred. See stacktrace for details"),
    AUTHORIZATION_ERROR("You do NOT have adequate permission to access this resource"),
    NO_PRINCIPAL("Principal identifier NOT provided", 500);

    private final String defaultMessage;
    private final int defaultHttpStatus;

    ErrorCode(String defaultMessage) {
        this(defaultMessage, 400);
    }

    ErrorCode(String defaultMessage, int defaultHttpStatus) {
        this.defaultMessage = defaultMessage;
        this.defaultHttpStatus = defaultHttpStatus;
    }
}
