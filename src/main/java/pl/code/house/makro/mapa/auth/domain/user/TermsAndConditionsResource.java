package pl.code.house.makro.mapa.auth.domain.user;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.TermsAndConditionsDto;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = BASE_PATH + "/terms-and-conditions", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class TermsAndConditionsResource {

  private final TermsAndConditionsFacade facade;

  @GetMapping
  ResponseEntity<TermsAndConditionsDto> fetchLatest() {
    log.info("Requesting latest Terms and Conditions record");
    return ok(facade.fetchLatestTerms());
  }

  @PostMapping("/{termsId}/approve")
  ResponseEntity<TermsAndConditionsDto> fetchLatest(@PathVariable Long termsId, @AuthenticationPrincipal Authentication principal) {
    log.info("User `{}` is requesting to approve Terms and Conditions - `{}`", principal, termsId);
    return ok(facade.approveTermsForUser(termsId, principal.getName()));
  }
}
