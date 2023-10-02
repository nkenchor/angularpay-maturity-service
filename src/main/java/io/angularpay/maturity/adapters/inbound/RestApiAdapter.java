package io.angularpay.maturity.adapters.inbound;

import io.angularpay.maturity.domain.commands.RunCronCommand;
import io.angularpay.maturity.domain.commands.UpdateCronCommand;
import io.angularpay.maturity.domain.commands.UpdateSlaInDaysCommand;
import io.angularpay.maturity.models.*;
import io.angularpay.maturity.ports.inbound.RestApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static io.angularpay.maturity.helpers.Helper.fromHeaders;

@RestController
@RequestMapping("/maturity/cron")
@RequiredArgsConstructor
public class RestApiAdapter implements RestApiPort {

    public static final String PAYNABLE_NAME = "paynable";
    public static final String PEER_FUND_NAME = "peer-fund";

    private final UpdateCronCommand updateCronCommand;
    private final RunCronCommand runCronCommand;
    private final UpdateSlaInDaysCommand updateSlaInDaysCommand;

    @PutMapping("/paynable/schedule")
    @Override
    public void updatePaynableCron(
            @RequestBody CronUpdateApiModel cronUpdateApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericCronUpdateCommandRequest genericCronUpdateCommandRequest = GenericCronUpdateCommandRequest.builder()
                .name(PAYNABLE_NAME)
                .cronUpdateApiModel(cronUpdateApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updateCronCommand.execute(genericCronUpdateCommandRequest);
    }

    @PutMapping("/peer-fund/schedule")
    @Override
    public void updatePeerFundCron(
            @RequestBody CronUpdateApiModel cronUpdateApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericCronUpdateCommandRequest genericCronUpdateCommandRequest = GenericCronUpdateCommandRequest.builder()
                .name(PEER_FUND_NAME)
                .cronUpdateApiModel(cronUpdateApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updateCronCommand.execute(genericCronUpdateCommandRequest);
    }

    @PutMapping("/paynable/sla")
    @Override
    public void updatePaynableSlaInDays(
            @RequestBody SlaInDaysUpdateApiModel slaInDaysUpdateApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericSlaInDaysUpdateCommandRequest genericSlaInDaysUpdateCommandRequest = GenericSlaInDaysUpdateCommandRequest.builder()
                .name(PAYNABLE_NAME)
                .slaInDaysUpdateApiModel(slaInDaysUpdateApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updateSlaInDaysCommand.execute(genericSlaInDaysUpdateCommandRequest);
    }

    @PutMapping("/peer-fund/sla")
    @Override
    public void updatePeerFundSlaInDays(
            @RequestBody SlaInDaysUpdateApiModel slaInDaysUpdateApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericSlaInDaysUpdateCommandRequest genericSlaInDaysUpdateCommandRequest = GenericSlaInDaysUpdateCommandRequest.builder()
                .name(PEER_FUND_NAME)
                .slaInDaysUpdateApiModel(slaInDaysUpdateApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updateSlaInDaysCommand.execute(genericSlaInDaysUpdateCommandRequest);
    }

    @PostMapping("/paynable/run")
    @Override
    public void runPaynableCronNow(@RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericCronRunCommandRequest genericCronRunCommandRequest = GenericCronRunCommandRequest.builder()
                .name(PAYNABLE_NAME)
                .authenticatedUser(authenticatedUser)
                .build();
        this.runCronCommand.execute(genericCronRunCommandRequest);
    }

    @PostMapping("/peer-fund/run")
    @Override
    public void runPeerFundCronNow(@RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericCronRunCommandRequest genericCronRunCommandRequest = GenericCronRunCommandRequest.builder()
                .name(PEER_FUND_NAME)
                .authenticatedUser(authenticatedUser)
                .build();
        this.runCronCommand.execute(genericCronRunCommandRequest);
    }
}
