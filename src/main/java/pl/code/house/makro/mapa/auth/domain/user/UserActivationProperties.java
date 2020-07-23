package pl.code.house.makro.mapa.auth.domain.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@lombok.Value
class UserActivationProperties {

  long expiresOn;
  String mailSubject;

  UserActivationProperties(
      @Value("${user.activation.code.expiresOn.hours}") long expiresOn,
      @Value("${user.activation.code.mail.subject}") String mailSubject
  ) {
    this.expiresOn = expiresOn;
    this.mailSubject = mailSubject;
  }
}
