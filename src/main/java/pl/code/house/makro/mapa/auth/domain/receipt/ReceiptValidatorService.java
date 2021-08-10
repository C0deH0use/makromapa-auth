package pl.code.house.makro.mapa.auth.domain.receipt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptValidatorService {

  private final AppleReceiptValidationService appleService;

  ValidationResponseDto validateReceipt(ReceiptValidationDto receiptDto) {
    log.info("Validating Purchase Receipt from Store: {}", receiptDto.getStore());

    if (receiptDto.getStore() == Store.PLAY_STORE) {
      return null;
    }

    return appleService.validateReceipt(receiptDto.getLocalReceipt());
  }
}
