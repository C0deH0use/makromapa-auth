package pl.code.house.makro.mapa.auth.domain.user;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.code.house.makro.mapa.auth.ApiConstraints.USER_MANAGEMENT_PATH;
import static pl.code.house.makro.mapa.auth.domain.user.UserFacade.maskEmail;

import java.security.Principal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.ActivateUserRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.CommunicationDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.NewPasswordRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.NewUserRequest;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserDto;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = USER_MANAGEMENT_PATH, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@PreAuthorize("hasAuthority('ROLE_REGISTER')")
class UserResource {

  private static final String EXTERNAL_TOKEN = "external-token";
  private static final String USER_ACTIVATION_LOG = "Client Application `{}` is requesting to activate user {} with activation_code: `{}`";

  private final UserFacade facade;

  @ResponseStatus(CREATED)
  @PostMapping("/registration")
  CommunicationDto registerNewDraft(UsernamePasswordAuthenticationToken principal, NewUserRequest newUserRequest) {
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

    return facade.registerNewUser(newUserRequest);
  }

  @ResponseStatus(OK)
  @PostMapping(path = "/activate")
  UserDto activateDraft(UsernamePasswordAuthenticationToken principal,
      ActivateUserRequest activateUserRequest) {
    log.info(USER_ACTIVATION_LOG, principal.getName(), maskEmail(activateUserRequest.getEmail()), activateUserRequest.getVerificationCode());
    String clientId = getClientId(principal);

    return facade.activateDraftBy(activateUserRequest, clientId);
  }

  @ResponseStatus(OK)
  @PostMapping("/password/reset")
  CommunicationDto resetUserPassword(UsernamePasswordAuthenticationToken principal,
      @RequestParam("email") String email) {
    log.info("Registered request from {} to reset password for user `{}`", principal.getName(), email);

    return facade.resetPasswordFor(email);
  }

  @ResponseStatus(OK)
  @PostMapping("/password/change")
  void changeUserPassword(UsernamePasswordAuthenticationToken principal,
      NewPasswordRequest newPasswordRequest) {
    log.info("Registered request from {} to reset password for user `{}`", principal.getName(), newPasswordRequest.getNewPassword());

    facade.changeUserPassword(newPasswordRequest.getEmail(), newPasswordRequest.getCode(), newPasswordRequest.getNewPassword());
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
