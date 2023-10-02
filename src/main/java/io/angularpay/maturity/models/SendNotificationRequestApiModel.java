
package io.angularpay.maturity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendNotificationRequestApiModel {

    private NotificationChannel channel;
    private NotificationType type;
    private String from;
    private String to;
    private String message;
    @JsonProperty("send_at")
    private String sendAt;
}
