package io.angularpay.maturity.ports.inbound;

import io.angularpay.maturity.models.CronUpdateApiModel;
import io.angularpay.maturity.models.SlaInDaysUpdateApiModel;

import java.util.Map;

public interface RestApiPort {
    void updatePaynableCron(CronUpdateApiModel cronUpdateApiModel, Map<String, String> headers);
    void updatePeerFundCron(CronUpdateApiModel cronUpdateApiModel, Map<String, String> headers);
    void updatePaynableSlaInDays(SlaInDaysUpdateApiModel slaInDaysUpdateApiModel, Map<String, String> headers);
    void updatePeerFundSlaInDays(SlaInDaysUpdateApiModel slaInDaysUpdateApiModel, Map<String, String> headers);
    void runPaynableCronNow(Map<String, String> headers);
    void runPeerFundCronNow(Map<String, String> headers);
}
