package pl.code.house.makro.mapa.auth.domain.mail.dto;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.thymeleaf.context.Context;
import pl.code.house.makro.mapa.auth.domain.mail.EmailType;

@Value
@NonFinal
public abstract class MessageDetails {

  EmailType type;
  String subject;
  String receiver;
  String template;
  Context context;
}
