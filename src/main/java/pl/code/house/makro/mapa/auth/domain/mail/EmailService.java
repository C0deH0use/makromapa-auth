package pl.code.house.makro.mapa.auth.domain.mail;

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
}
