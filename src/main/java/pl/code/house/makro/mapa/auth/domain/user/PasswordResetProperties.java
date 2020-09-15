package pl.code.house.makro.mapa.auth.domain.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@lombok.Value
class PasswordResetProperties {

  long expiresOn;
  String mailSubject;

  PasswordResetProperties(
      @Value("${mails.password.reset.verification.code.expiresOn.hours}") long expiresOn,
      @Value("${mails.password.reset.verification.code.mail.subject}") String mailSubject
  ) {
    this.expiresOn = expiresOn;
    this.mailSubject = mailSubject;
  }
}
