package pl.code.house.makro.mapa.auth.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "app.provider")
public record ExternalProviders(List<String> clients) {

}
