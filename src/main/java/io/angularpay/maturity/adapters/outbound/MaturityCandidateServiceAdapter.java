package io.angularpay.maturity.adapters.outbound;

import io.angularpay.maturity.configurations.AngularPayConfiguration;
import io.angularpay.maturity.models.InvestmentPartiesRequest;
import io.angularpay.maturity.models.InvestmentPartyMaturityMessageResponseModel;
import io.angularpay.maturity.models.UnpaidFacilityResponseModel;
import io.angularpay.maturity.ports.outbound.MaturityCandidateServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.angularpay.maturity.helpers.Helper.getBaseUrl;

@Service
@RequiredArgsConstructor
public class MaturityCandidateServiceAdapter implements MaturityCandidateServicePort {

    private final WebClient webClient;
    private final AngularPayConfiguration configuration;

    @Override
    public List<UnpaidFacilityResponseModel> getUnpaidFacilityRequests(String name, Map<String, String> headers) {
        URI url = UriComponentsBuilder.fromUriString(getBaseUrl(this.configuration.getMaturityCandidates(), name))
                .path("/list/maturity/status/unpaid")
                .build().toUri();

        return webClient
                .get()
                .uri(url.toString())
                .header("x-angularpay-username", headers.get("x-angularpay-username"))
                .header("x-angularpay-device-id", headers.get("x-angularpay-device-id"))
                .header("x-angularpay-user-reference", headers.get("x-angularpay-user-reference"))
                .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(new ParameterizedTypeReference<List<UnpaidFacilityResponseModel>>() {
                        });
                    } else {
                        return Mono.just(Collections.emptyList());
                    }
                })
                .block();
    }

    @Override
    public List<InvestmentPartyMaturityMessageResponseModel> getInvestmentPartiesMaturityMessages(InvestmentPartiesRequest parameters, Map<String, String> headers) {
        URI url = UriComponentsBuilder.fromUriString(getBaseUrl(this.configuration.getMaturityCandidates(), parameters.getName()))
                .path("/")
                .path(parameters.getRequestReference())
                .path("/maturity/messages")
                .build().toUri();

        return webClient
                .get()
                .uri(url.toString())
                .header("x-angularpay-username", headers.get("x-angularpay-username"))
                .header("x-angularpay-device-id", headers.get("x-angularpay-device-id"))
                .header("x-angularpay-user-reference", headers.get("x-angularpay-user-reference"))
                .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(new ParameterizedTypeReference<List<InvestmentPartyMaturityMessageResponseModel>>() {
                        });
                    } else {
                        return Mono.just(Collections.emptyList());
                    }
                })
                .block();
    }
}
