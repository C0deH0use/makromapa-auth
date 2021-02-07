package pl.code.house.makro.mapa.auth.domain.mail;

import static com.icegreen.greenmail.configuration.GreenMailConfiguration.aConfig;
import static javax.mail.Message.RecipientType.TO;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.code.house.makro.mapa.auth.domain.GreenMailSmtpConfig.SMTP_SETUP;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;
import pl.code.house.makro.mapa.auth.domain.mail.dto.RegistrationMessageDetails;
import pl.code.house.makro.mapa.auth.domain.mail.dto.ResetPasswordMessageDetails;

@SpringBootTest
class EmailServiceHttpTest {

  public static final String EXPECTED_GREETINGS = "Witamy w MakroMapie";
  public static final String EXPECTED_RESET_PASSWORD = "Pro=C5=9Bba o zmian=C4=99 has=C5=82a";
  public static final String EXPECTED_SUBJECT = "REGISTRATION_SUBJECT";
  public static final String EXPECTED_RESET_PASSWORD_SUBJECT = "RESET_PASSWORD_SUBJECT";
  public static final String ACTIVATION_CODE = randomNumeric(6);
  public static final String EXPIRY_DATE = "2020-09-15 08:03:48";
  public static final String EXPECTED_RECEIVER = "Marek00Malik@gmail.com";

  @RegisterExtension
  static GreenMailExtension mailBean = new GreenMailExtension(SMTP_SETUP)
      .withConfiguration(aConfig()
          .withUser("user_greenMain", "secret_password")
      );


  @Autowired
  private EmailService sut;

  @Test
  @DisplayName("should send proper registration email")
  void shouldSendProperRegistrationEmail() throws MessagingException {
    //given
    Context ctx = new Context();
    ctx.setVariable("verification_code", ACTIVATION_CODE);
    ctx.setVariable("expiry_date", EXPIRY_DATE);
    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(EXPECTED_SUBJECT, EXPECTED_RECEIVER, ctx);

    //when
    sut.sendHtmlMail(messageDetails);

    //then
    MimeMessage[] receivedMessages = mailBean.getReceivedMessages();
    assertThat(receivedMessages).hasSize(1);
    assertThat(receivedMessages[0].getSubject()).isEqualTo(EXPECTED_SUBJECT);

    Address[] recipients = receivedMessages[0].getRecipients(TO);
    assertThat(recipients).hasSize(1);
    assertThat(recipients[0].toString()).isEqualTo(EXPECTED_RECEIVER);

    String content = GreenMailUtil.getBody(receivedMessages[0]);
    System.out.println(content);
    assertThat(content).contains(EXPECTED_GREETINGS);
    assertThat(content).contains(ACTIVATION_CODE);
  }

  @Test
  @DisplayName("should send proper reset password email")
  void shouldSendProperResetPasswordEmail() throws MessagingException {
    //given
    Context ctx = new Context();
    ctx.setVariable("verification_code", ACTIVATION_CODE);
    ctx.setVariable("expiry_date", EXPIRY_DATE);
    ctx.setVariable("user_name", EXPECTED_RECEIVER);
    ResetPasswordMessageDetails messageDetails = new ResetPasswordMessageDetails(EXPECTED_RESET_PASSWORD_SUBJECT, EXPECTED_RECEIVER, ctx);

    //when
    sut.sendHtmlMail(messageDetails);

    //then
    MimeMessage[] receivedMessages = mailBean.getReceivedMessages();
    assertThat(receivedMessages).hasSize(1);
    assertThat(receivedMessages[0].getSubject()).isEqualTo(EXPECTED_RESET_PASSWORD_SUBJECT);

    Address[] recipients = receivedMessages[0].getRecipients(TO);
    assertThat(recipients).hasSize(1);
    assertThat(recipients[0].toString()).isEqualTo(EXPECTED_RECEIVER);

    String content = GreenMailUtil.getBody(receivedMessages[0]);
    System.out.println(content);
    assertThat(content).contains(EXPECTED_RESET_PASSWORD);
    assertThat(content).contains(EXPECTED_RECEIVER);
    assertThat(content).contains(ACTIVATION_CODE);
  }

  @Test
  @DisplayName("throw if message does not have a valid subject")
  void throwIfMessageDoesNotHaveAValidSubject() {
    //given
    Context ctx = new Context();
    ctx.setVariable("verification_code", ACTIVATION_CODE);
    ctx.setVariable("expiry_date", EXPIRY_DATE);
    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(null, EXPECTED_RECEIVER, ctx);

    //when & then
    assertThatThrownBy(() -> sut.sendHtmlMail(messageDetails))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("should have subject")
    ;
  }

  @Test
  @DisplayName("throw if message does not have valid receiver value")
  void throwIfMessageDoesNotHaveValidReceiverValue() {
    //given
    Context ctx = new Context();
    ctx.setVariable("verification_code", ACTIVATION_CODE);
    ctx.setVariable("expiry_date", EXPIRY_DATE);

    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(EXPECTED_SUBJECT, null, ctx);

    //when & then
    assertThatThrownBy(() -> sut.sendHtmlMail(messageDetails))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("should be addressed to someone")
    ;
  }

  @Test
  @DisplayName("throw if message does not have verification_code")
  void throwIfMessageDoesNotHaveVerificationCode() {
    //given
    Context ctx = new Context();
    ctx.setVariable("expiry_date", EXPIRY_DATE);

    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(EXPECTED_SUBJECT, EXPECTED_RECEIVER, ctx);

    //when & then
    assertThatThrownBy(() -> sut.sendHtmlMail(messageDetails))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("should contain the `verification_code`")
    ;
  }

  @Test
  @DisplayName("throw if message does not have expiry_date")
  void throwIfMessageDoesNotHaveExpiryDate() {
    //given
    Context ctx = new Context();
    ctx.setVariable("verification_code", EXPIRY_DATE);

    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(EXPECTED_SUBJECT, EXPECTED_RECEIVER, ctx);

    //when & then
    assertThatThrownBy(() -> sut.sendHtmlMail(messageDetails))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("should contain the `expiry_date`")
    ;
  }
}