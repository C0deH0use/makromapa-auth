package pl.code.house.makro.mapa.auth.domain.receipt;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static pl.code.house.makro.mapa.auth.domain.receipt.StoreEnvironment.SANDBOX;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import pl.code.house.makro.mapa.auth.error.ReceiptValidationException;
import reactor.core.publisher.Mono;

@Service
class AppleReceiptValidationClient {

  private static final int SANDBOX_RECEIPT_STATUS_CODE = 21_007;
  private final String appStorePassword;
  private final WebClient itunesClient;
  private final WebClient sandboxClient;

  AppleReceiptValidationClient(
      @Value("${app.receipt.validation.apple.itunes}") String itunesUrl,
      @Value("${app.receipt.validation.apple.sandbox}") String sandboxUrl,
      @Value("${app.receipt.validation.apple.password}") String appStorePassword
  ) {
    this.itunesClient = WebClient.builder().baseUrl(itunesUrl).build();
    this.sandboxClient = WebClient.builder().baseUrl(sandboxUrl).build();
    this.appStorePassword = appStorePassword;
  }

  AppleRecieptValidationResponse validateAgainstItunes(String receipt) {
    Map<String, Object> payload = Map.of(
        "receipt-data", receipt,
        "password", appStorePassword,
        "exclude-old-transactions", false
    );

    return itunesClient.post()
        .contentType(APPLICATION_JSON)
        .bodyValue(payload)
        .exchangeToMono(this::getResponseHandler)
        .block();
  }

  AppleRecieptValidationResponse validateAgainstSandbox(String receipt) {
    Map<String, Object> payload = Map.of(
        "receipt-data", receipt,
        "password", appStorePassword,
        "exclude-old-transactions", false
    );

    return sandboxClient.post()
        .contentType(APPLICATION_JSON)
        .bodyValue(payload)
        .exchangeToMono(this::getResponseHandler)
        .block();
  }

  private Mono<AppleRecieptValidationResponse> getResponseHandler(ClientResponse response) {
    if (!response.statusCode().is2xxSuccessful()) {
      return Mono.error(new ReceiptValidationException("Apple Receipt validation failed. StatusCode: " + response.statusCode()));
    }
    return response.bodyToMono(AppleRecieptValidationResponse.class)
        .map(this::validateResponse);
  }

  private AppleRecieptValidationResponse validateResponse(AppleRecieptValidationResponse response) {
    if ((response.getEnvironment() == null || SANDBOX != response.getEnvironment())
        && response.getStatus() == SANDBOX_RECEIPT_STATUS_CODE) {
      throw new ItunesReceiptValidationException(response.getStatus());
    }
    if (response.getStatus() != 0) {
      throw new ReceiptValidationException("Apple Receipt validation failed. StatusCode: " + response.getStatus());
    }
    return response;
  }
}
