package pl.code.house.makro.mapa.auth.domain.user;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.code.house.makro.mapa.auth.ApiConstraints.OAUTH_USER_PATH;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.TermsAndConditionsDto;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = OAUTH_USER_PATH + "/terms-and-conditions", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
class TermsAndConditionsResource {

  private final TermsAndConditionsFacade facade;

  @GetMapping
  @ResponseStatus(OK)
  TermsAndConditionsDto fetchLatest() {
    log.info("Requesting latest Terms and Conditions record");
    return facade.fetchLatestTerms();
  }

  @ResponseStatus(OK)
  @PostMapping("/{termsId}/approve")
  TermsAndConditionsDto fetchLatest(Authentication principal, @PathVariable Long termsId) {
    log.info("User `{}` is requesting to approve Terms and Conditions - `{}`", principal, termsId);
    return facade.approveTermsForUser(termsId, principal.getName());
  }
}
