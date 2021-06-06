package pl.code.house.makro.mapa.auth.domain.user;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "app")
record DefaultUserAvatars(List<String> avatars) {

}
