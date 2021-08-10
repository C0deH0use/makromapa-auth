package pl.code.house.makro.mapa.auth.domain.receipt;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
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

  ValidationResponseDto validateAgainstItunes(String receipt) {
    Map<String, Object> payload = Map.of(
        "receipt-data", receipt,
        "password", appStorePassword,
        "exclude-old-transactions", false
    );

    return itunesClient.post()
        .contentType(APPLICATION_JSON)
        .bodyValue(payload)
        .exchangeToMono(getResponseHandler(Environment.PRODUCTION)).block();
  }

  ValidationResponseDto validateAgainstSandbox(String receipt) {
    Map<String, Object> payload = Map.of(
        "receipt-data", receipt,
        "password", appStorePassword,
        "exclude-old-transactions", false
    );

    return sandboxClient.post()
        .contentType(APPLICATION_JSON)
        .bodyValue(payload)
        .exchangeToMono(getResponseHandler(Environment.SANDBOX)).block();
  }

  @NotNull
  private Function<ClientResponse, Mono<ValidationResponseDto>> getResponseHandler(Environment environment) {
    return response -> {
      if (response.statusCode() == OK) {
        return response.bodyToMono(ValidationResponseDto.class);
      }

      if (environment == Environment.PRODUCTION && response.statusCode().value() == SANDBOX_RECEIPT_STATUS_CODE) {
        Mono.error(new ItunesReceiptValidationException(response.statusCode()));
      }

      return Mono.error(new ReceiptValidationException("Apple Receipt validation failed. StatusCode: " + response.statusCode()));
    };
  }


  private enum Environment {
    PRODUCTION,
    SANDBOX
  }

}
