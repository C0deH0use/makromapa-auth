package pl.code.house.makro.mapa.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("pl.code.house.makro.mapa.auth")
public class MakroMapaAuthApplication {

  public static void main(String[] args) {
    SpringApplication.run(MakroMapaAuthApplication.class, args);
  }

}
