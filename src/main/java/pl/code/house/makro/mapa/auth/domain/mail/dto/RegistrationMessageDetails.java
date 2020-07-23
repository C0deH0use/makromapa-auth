package pl.code.house.makro.mapa.auth.domain.mail.dto;

import static pl.code.house.makro.mapa.auth.domain.mail.EmailType.REGISTRATION;

import org.thymeleaf.context.Context;

public class RegistrationMessageDetails extends MessageDetails {

  private static final String template = "user_registration-template.html";

  public RegistrationMessageDetails(String subject, String receiver, Context context) {
    super(REGISTRATION, subject, receiver, template, context);
  }
}
