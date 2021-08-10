package pl.code.house.makro.mapa.auth.domain.receipt;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.decodeBase64;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
class AppleReceiptValidationService {

  private static final String ITUNES_VALIDATION_FAILED_MESSAGE = "Validation against Itunes failed with 21007 StatusCode, checking receipt against SandBox";

  private final AppleReceiptValidationClient validationClient;

  ValidationResponseDto validateReceipt(String encodedReceipt) {
    log.debug("Apple Purchase Receipt:{}", encodedReceipt);

    String receipt = new String(decodeBase64(encodedReceipt), UTF_8);
    ValidationResponseDto validationResponse = Try.of(() -> validationClient.validateAgainstItunes(receipt))
        .onFailure(ItunesReceiptValidationException.class, (exc) -> log.error(ITUNES_VALIDATION_FAILED_MESSAGE, exc))
        .recover(ItunesReceiptValidationException.class, validationClient.validateAgainstSandbox(receipt))
        .get();
    log.debug("Validation succeeded. {}", validationResponse);
    return validationResponse;
  }
}
