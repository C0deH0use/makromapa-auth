package pl.code.house.makro.mapa.auth.domain;

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP;

import com.icegreen.greenmail.util.ServerSetup;

public class GreenMailSmtpConfig {

  public static final ServerSetup SMTP_SETUP = SMTP;

  static {
    SMTP_SETUP.setServerStartupTimeout(8000);
  }
}
