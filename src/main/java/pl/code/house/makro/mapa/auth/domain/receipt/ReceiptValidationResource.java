package pl.code.house.makro.mapa.auth.domain.receipt;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/oauth/receipt", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class ReceiptValidationResource {

  private final ReceiptValidatorService receiptValidator;

  @PostMapping
  @ResponseStatus(OK)
  ValidationResponseDto validateReceiptPurchase(Authentication principal, @Valid @RequestBody ReceiptValidationDto receiptDto) {
    log.info("Request to validate User {} receipt purchase in {}", principal.getName(), receiptDto.getStore());

    return receiptValidator.validateReceipt(receiptDto);
  }
}
