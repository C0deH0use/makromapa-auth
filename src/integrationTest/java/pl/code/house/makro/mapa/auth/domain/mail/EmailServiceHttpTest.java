package pl.code.house.makro.mapa.auth.domain.mail;

import static javax.mail.Message.RecipientType.TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;
import pl.code.house.makro.mapa.auth.domain.mail.dto.RegistrationMessageDetails;

@SpringBootTest
class EmailServiceHttpTest {

  public static final String EXPECTED_GREETINGS = "Witamy w MakroMapie";
  public static final String EXPECTED_SUBJECT = "REGISTRATION_SUBJECT";
  public static final String ACTIVATION_CODE = "ACTIVATION_CODE";
  public static final String EXPECTED_RECEIVER = "email@test.com";
  @Autowired
  private EmailService sut;

  private GreenMail greenMail;

  @BeforeEach
  void setup() {
    greenMail = new GreenMail();
    greenMail.setUser("user_greenMain", "secret_password");
    greenMail.start();
  }

  @AfterEach
  void stop() {
    greenMail.stop();
  }

  @Test
  @DisplayName("should send proper registration email")
  void shouldSendProperRegistrationEmail() throws MessagingException {
    //given
    Context ctx = new Context();
    ctx.setVariable("name", "TEST_NAME");
    ctx.setVariable("activation_code", ACTIVATION_CODE);
    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(EXPECTED_SUBJECT, EXPECTED_RECEIVER, ctx);

    //when
    sut.sendHtmlMail(messageDetails);

    //then
    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
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
  @DisplayName("throw if message does not have a valid subject")
  void throwIfMessageDoesNotHaveAValidSubject() {
    //given
    Context ctx = new Context();
    ctx.setVariable("name", "TEST_NAME");
    ctx.setVariable("activation_code", ACTIVATION_CODE);
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
    ctx.setVariable("name", "TEST_NAME");
    ctx.setVariable("activation_code", ACTIVATION_CODE);
    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(EXPECTED_SUBJECT, null, ctx);

    //when & then
    assertThatThrownBy(() -> sut.sendHtmlMail(messageDetails))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("should be addressed to someone")
    ;
  }

  @Test
  @DisplayName("throw if message does not have activation_code")
  void throwIfMessageDoesNotHaveActivationCode() {
    //given
    Context ctx = new Context();
    ctx.setVariable("name", "TEST_NAME");
    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(EXPECTED_SUBJECT, null, ctx);

    //when & then
    assertThatThrownBy(() -> sut.sendHtmlMail(messageDetails))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("should be addressed to someone")
    ;
  }
}