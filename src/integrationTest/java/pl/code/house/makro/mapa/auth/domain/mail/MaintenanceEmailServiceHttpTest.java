package pl.code.house.makro.mapa.auth.domain.mail;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;
import pl.code.house.makro.mapa.auth.domain.mail.dto.RegistrationMessageDetails;
import pl.code.house.makro.mapa.auth.domain.mail.dto.ResetPasswordMessageDetails;

@SpringBootTest
@Tag("maintenance")
class MaintenanceEmailServiceHttpTest {

  public static final String EXPECTED_SUBJECT = "REGISTRATION_SUBJECT";
  public static final String EXPECTED_RESET_PASSWORD_SUBJECT = "RESET_PASSWORD_SUBJECT";
  public static final String ACTIVATION_CODE = randomNumeric(6);
  public static final String EXPIRY_DATE = "2020-09-15 08:03:48";
//    public static final String EXPECTED_RECEIVER = "aga.poczta3@autograf.pl";
  public static final String EXPECTED_RECEIVER = "Marek00Malik@gmail.com";

  @Autowired
  private EmailService sut;

  @Test
  @DisplayName("should send proper registration email")
  void shouldSendProperRegistrationEmail() {
    //given
    Context ctx = new Context();
    ctx.setVariable("verification_code", ACTIVATION_CODE);
    ctx.setVariable("expiry_date", EXPIRY_DATE);
    RegistrationMessageDetails messageDetails = new RegistrationMessageDetails(EXPECTED_SUBJECT, EXPECTED_RECEIVER, ctx);

    //when
    sut.sendHtmlMail(messageDetails);
  }

  @Test
  @DisplayName("should send proper reset password email")
  void shouldSendProperResetPasswordEmail() {
    //given
    Context ctx = new Context();
    ctx.setVariable("verification_code", ACTIVATION_CODE);
    ctx.setVariable("expiry_date", EXPIRY_DATE);
    ctx.setVariable("user_name", EXPECTED_RECEIVER);
    ResetPasswordMessageDetails messageDetails = new ResetPasswordMessageDetails(EXPECTED_RESET_PASSWORD_SUBJECT, EXPECTED_RECEIVER, ctx);

    //when
    sut.sendHtmlMail(messageDetails);
  }

}