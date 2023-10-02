package io.angularpay.maturity.ports.outbound;

import io.angularpay.maturity.models.InvestmentPartiesRequest;
import io.angularpay.maturity.models.InvestmentPartyMaturityMessageResponseModel;
import io.angularpay.maturity.models.UnpaidFacilityResponseModel;

import java.util.List;
import java.util.Map;

public interface MaturityCandidateServicePort {
    List<UnpaidFacilityResponseModel> getUnpaidFacilityRequests(String name, Map<String, String> headers);
    List<InvestmentPartyMaturityMessageResponseModel> getInvestmentPartiesMaturityMessages(InvestmentPartiesRequest investmentPartiesRequest, Map<String, String> headers);
}