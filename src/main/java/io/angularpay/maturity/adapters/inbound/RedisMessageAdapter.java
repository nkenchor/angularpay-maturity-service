package io.angularpay.maturity.adapters.inbound;

import io.angularpay.maturity.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.maturity.models.platform.PlatformConfigurationIdentifier;
import io.angularpay.maturity.ports.inbound.InboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.angularpay.maturity.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}
