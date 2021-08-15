package pl.code.house.makro.mapa.auth.domain.receipt;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
class AppleReceiptValidationService {

  private static final String PROD_VALIDATION_FAILED_MESSAGE =
      "Validation against iTunes failed with 21007 StatusCode, checking receipt against SandBox server";

  private final AppleReceiptValidationClient validationClient;

  boolean validateReceipt(String receipt) {
    log.debug("Apple Purchase Receipt:{}", receipt);

    AppleRecieptValidationResponse validationResponse = Try.of(() -> validationClient.validateAgainstItunes(receipt))
        .onFailure(ItunesReceiptValidationException.class, (exc) -> log.error(PROD_VALIDATION_FAILED_MESSAGE, exc))
        .recover(ItunesReceiptValidationException.class, (exc) -> validationClient.validateAgainstSandbox(receipt))
        .get();
    log.debug("Validation result. {}", validationResponse);
    return validationResponse.getStatus() == 0;
  }
}
