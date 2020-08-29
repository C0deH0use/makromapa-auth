package pl.code.house.makro.mapa.auth.domain.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@lombok.Value
class UserActivationProperties {

  long expiresOn;
  String mailSubject;

  UserActivationProperties(
      @Value("${mails.registration.verification.code.expiresOn.hours}") long expiresOn,
      @Value("${mails.registration.verification.code.mail.subject}") String mailSubject
  ) {
    this.expiresOn = expiresOn;
    this.mailSubject = mailSubject;
  }
}
