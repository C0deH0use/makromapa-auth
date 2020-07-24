package pl.code.house.makro.mapa.auth.domain.mail;

import static org.apache.commons.lang3.StringUtils.indexOf;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static pl.code.house.makro.mapa.auth.domain.mail.EmailType.REGISTRATION;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import pl.code.house.makro.mapa.auth.domain.mail.dto.MessageDetails;
import pl.code.house.makro.mapa.auth.error.EmailNotSendException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  private final TemplateEngine templateEngine;

  public void sendHtmlMail(MessageDetails details) {
    hasText(details.getReceiver(), "Email should be addressed to someone");
    hasText(details.getSubject(), "Email should have subject");
    notNull(details.getType(), "Email should be of proper type");
    validateByType(details);
    log.info("Sending {} message titled {} to - {}", details.getType(), details.getSubject(), maskSubject(details.getReceiver()));
    trySendMessage(details);
  }

  private String maskSubject(String receiver) {
    int indexOfAtSign = indexOf(receiver, "@");

    return receiver.substring(0, indexOfAtSign) + "*".repeat(receiver.length() - indexOfAtSign);
  }

  private void trySendMessage(MessageDetails details) {
    try {
      MimeMessage msg = mailSender.createMimeMessage();
      MimeMessageHelper message = new MimeMessageHelper(msg, true, "UTF-8"); // true = multipart
      message.setSubject(details.getSubject());
      message.setTo(details.getReceiver());

      String htmlMessage = templateEngine.process(details.getTemplate(), details.getContext());
      message.setText(htmlMessage, true);

      mailSender.send(msg);
    } catch (MessagingException e) {
      throw new EmailNotSendException("Could not send email - " + details.getType(), e);
    }
  }

  private void validateByType(MessageDetails details) {
    if (REGISTRATION == details.getType()) {
      isTrue(details.getContext().containsVariable("activation_code"), "Registration email message should contain the `activation_code`");
    }
  }
}
