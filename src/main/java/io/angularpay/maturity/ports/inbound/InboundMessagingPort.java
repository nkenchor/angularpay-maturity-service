package io.angularpay.maturity.ports.inbound;

import io.angularpay.maturity.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}
