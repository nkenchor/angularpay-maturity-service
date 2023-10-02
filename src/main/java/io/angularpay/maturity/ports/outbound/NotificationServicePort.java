package io.angularpay.maturity.ports.outbound;

import io.angularpay.maturity.models.SendNotificationRequestApiModel;
import io.angularpay.maturity.models.SendNotificationResponseApiModel;

import java.util.Map;
import java.util.Optional;

public interface NotificationServicePort {
    Optional<SendNotificationResponseApiModel> sendNotification(SendNotificationRequestApiModel requestApiModel, Map<String, String> headers);
}
