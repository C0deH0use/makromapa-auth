package pl.code.house.makro.mapa.auth.domain.mail.dto;

import static pl.code.house.makro.mapa.auth.domain.mail.EmailType.RESET_PASSWORD;

import org.thymeleaf.context.Context;

public class ResetPasswordMessageDetails extends MessageDetails {

  private static final String TEMPLATE = "password-reset-template.html";

  public ResetPasswordMessageDetails(String subject, String receiver, Context context) {
    super(RESET_PASSWORD, subject, receiver, TEMPLATE, context);
  }
}
