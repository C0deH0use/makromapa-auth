package pl.code.house.makro.mapa.auth.domain.user;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static pl.code.house.makro.mapa.auth.ApiConstraints.BASE_PATH;

import java.security.Principal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.CommunicationDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.NewPasswordRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.NewUserRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = BASE_PATH + "/user", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('ROLE_REGISTER')")
class UserResource {

  private static final String EXTERNAL_TOKEN = "external-token";

  private final UserFacade facade;

  @PostMapping("/registration")
  ResponseEntity<CommunicationDto> registerNewDraft(@AuthenticationPrincipal UsernamePasswordAuthenticationToken principal, NewUserRequest newUserRequest) {
    log.info("Request to register new user `{}` by {}", newUserRequest.getEmail(), principal.getPrincipal());

    String clientId = getClientId(principal);

    if (!clientId.equals(newUserRequest.getClientId())) {
      // double check to make sure that the client ID in the token request is the same as that in the
      // authenticated client
      throw new InvalidClientException("Given client ID does not match authenticated client");
    }

    if (isAnyBlank(newUserRequest.getEmail(), newUserRequest.getPassword())) {
      throw new IllegalArgumentException("Missing username or password parameters in request");
    }

    if (!EXTERNAL_TOKEN.equalsIgnoreCase(newUserRequest.getGrantType())) {
      throw new InvalidGrantException("Implicit grant type not supported from user registration endpoint");
    }

    CommunicationDto draftDto = facade.registerNewUser(newUserRequest);
    return status(CREATED).body(draftDto);
  }

  @PostMapping(path = "/activate/{activation_code}")
  ResponseEntity<UserDto> activateDraft(@AuthenticationPrincipal UsernamePasswordAuthenticationToken principal,
      @PathVariable("activation_code") String activationCode) {
    log.info("Application Client `{}` is requesting to activate user by activation_code: `{}`", principal.getName(), activationCode);
    String clientId = getClientId(principal);

    UserDto userDto = facade.activateDraftBy(activationCode, clientId);

    return ok(userDto);
  }

  @PostMapping("/password/reset")
  ResponseEntity<CommunicationDto> resetUserPassword(@AuthenticationPrincipal UsernamePasswordAuthenticationToken principal,
      @RequestParam("email") String email) {
    log.info("Registered request from {} to reset password for user `{}`", principal.getName(), email);

    CommunicationDto communicationDto = facade.resetPasswordFor(email);
    return status(CREATED).body(communicationDto);
  }

  @PostMapping("/password/change")
  ResponseEntity changeUserPassword(@AuthenticationPrincipal UsernamePasswordAuthenticationToken principal,
      NewPasswordRequest newPasswordRequest) {
    log.info("Registered request from {} to reset password for user `{}`", principal.getName(), newPasswordRequest.getNewPassword());

    facade.changeUserPassword(newPasswordRequest.getEmail(), newPasswordRequest.getCode(), newPasswordRequest.getNewPassword());
    return ok().build();
  }

  private String getClientId(Principal principal) {
    Authentication client = (Authentication) principal;
    if (!client.isAuthenticated()) {
      throw new InsufficientAuthenticationException("The client is not authenticated.");
    }
    String clientId = client.getName();
    if (StringUtils.isBlank(clientId)) {
      throw new InsufficientAuthenticationException("");
    }

    return clientId;
  }
}
