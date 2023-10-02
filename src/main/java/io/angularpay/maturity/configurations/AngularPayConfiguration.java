package io.angularpay.maturity.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("angularpay")
@Data
public class AngularPayConfiguration {

    private int codecSizeInMB;
    private String notificationUrl;
    private String userconfigUrl;
    private List<MaturityCandidates> maturityCandidates;
    private Redis redis;

    @Data
    public static class Redis {
        private String host;
        private int port;
        private int timeout;
    }

    @Data
    public static class MaturityCandidates {
        private String name;
        private String url;
        private String cron;
        private int slaInDays;
    }
}
