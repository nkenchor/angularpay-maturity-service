package io.angularpay.maturity.scheduler;

import io.angularpay.maturity.configurations.AngularPayConfiguration;
import io.angularpay.maturity.domain.Role;
import io.angularpay.maturity.domain.commands.RunCronCommand;
import io.angularpay.maturity.models.AuthenticatedUser;
import io.angularpay.maturity.models.GenericCronRunCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static io.angularpay.maturity.helpers.Helper.addToMappedDiagnosticContext;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class MaturityCronConfiguration implements SchedulingConfigurer {

    private final AngularPayConfiguration angularPayConfiguration;
    private final RunCronCommand runCronCommand;
    private TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> cronScheduledFuture = new HashMap<>();

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(angularPayConfiguration.getMaturityCandidates().size());
        threadPoolTaskScheduler.initialize();
        angularPayConfiguration.getMaturityCandidates().forEach(x -> {
            cronJob(x, threadPoolTaskScheduler);
        });
        this.taskScheduler = threadPoolTaskScheduler;
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }

    private void cronJob(AngularPayConfiguration.MaturityCandidates maturityCandidates, TaskScheduler scheduler) {
        ScheduledFuture<?> job = scheduler.schedule(() -> {
            AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                    .username("service_account")
                    .userReference("service_account")
                    .deviceId("service_account")
                    .correlationId(UUID.randomUUID().toString())
                    .roles(Collections.singletonList(Role.ROLE_MATURITY_ADMIN))
                    .build();

            GenericCronRunCommandRequest genericCronRunCommandRequest = GenericCronRunCommandRequest.builder()
                    .name(maturityCandidates.getName())
                    .authenticatedUser(authenticatedUser)
                    .build();

            addToMappedDiagnosticContext("x-angularpay-correlation-id", authenticatedUser.getCorrelationId());
            addToMappedDiagnosticContext("x-angularpay-username", authenticatedUser.getUsername());
            addToMappedDiagnosticContext("x-angularpay-user-reference", authenticatedUser.getUserReference());
            log.info("Maturity CRON started");

            runCronCommand.execute(genericCronRunCommandRequest);

            log.info("Maturity CRON completed");
            MDC.clear();
        }, triggerContext -> {
            return new CronTrigger(maturityCandidates.getCron()).nextExecutionTime(triggerContext);
        });
        cronScheduledFuture.put(maturityCandidates.getName(), job);
    }

    public void refreshCronJob(AngularPayConfiguration.MaturityCandidates maturityCandidates) {
        if (cronScheduledFuture.get(maturityCandidates.getName()) != null) {
            cronScheduledFuture.get(maturityCandidates.getName()).cancel(true);
            cronJob(maturityCandidates, taskScheduler);
        }
    }
}
