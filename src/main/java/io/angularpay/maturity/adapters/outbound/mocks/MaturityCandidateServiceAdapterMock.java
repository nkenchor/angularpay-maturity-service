package io.angularpay.maturity.adapters.outbound.mocks;

import io.angularpay.maturity.models.InvestmentPartiesRequest;
import io.angularpay.maturity.models.InvestmentPartyMaturityMessageResponseModel;
import io.angularpay.maturity.models.MaturityMessage;
import io.angularpay.maturity.models.UnpaidFacilityResponseModel;
import io.angularpay.maturity.ports.outbound.MaturityCandidateServicePort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.angularpay.maturity.domain.RepaymentStatus.UNPAID;

@Service
public class MaturityCandidateServiceAdapterMock implements MaturityCandidateServicePort {

    @Override
    public List<UnpaidFacilityResponseModel> getUnpaidFacilityRequests(String name, Map<String, String> headers) {
        UnpaidFacilityResponseModel response = new UnpaidFacilityResponseModel();
        response.setReference(UUID.randomUUID().toString());
        response.setMaturesOn("2021-08-26T07:40:00Z");
        response.setStatus(UNPAID);
        return Collections.singletonList(response);
    }

    @Override
    public List<InvestmentPartyMaturityMessageResponseModel> getInvestmentPartiesMaturityMessages(InvestmentPartiesRequest investmentPartiesRequest, Map<String, String> headers) {
        MaturityMessage message = new MaturityMessage();
        message.setSms("sms message sample");
        message.setEmail("email message sample");

        InvestmentPartyMaturityMessageResponseModel response = new InvestmentPartyMaturityMessageResponseModel();
        response.setUserReference(UUID.randomUUID().toString());
        response.setMessage(message);
        return Collections.singletonList(response);
    }
}
