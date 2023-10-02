package io.angularpay.maturity.adapters.outbound.mocks;

import io.angularpay.maturity.models.SendNotificationRequestApiModel;
import io.angularpay.maturity.models.SendNotificationResponseApiModel;
import io.angularpay.maturity.ports.outbound.NotificationServicePort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationServiceAdapterMock implements NotificationServicePort {

    @Override
    public Optional<SendNotificationResponseApiModel> sendNotification(SendNotificationRequestApiModel sendNotificationRequestApiModel, Map<String, String> headers) {
        SendNotificationResponseApiModel notificationResponseApiModel = new SendNotificationResponseApiModel();
        notificationResponseApiModel.setReference(UUID.randomUUID().toString());
        return Optional.of(notificationResponseApiModel);
    }
}
