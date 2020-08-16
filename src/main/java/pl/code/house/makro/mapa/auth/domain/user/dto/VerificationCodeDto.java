package pl.code.house.makro.mapa.auth.domain.user.dto;

import static lombok.AccessLevel.PUBLIC;

import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(access = PUBLIC)
public class ActivationCodeDto {

  UUID id;

  UserDto draftUser;

  Boolean enabled;

  String code;

  ZonedDateTime expiresOn;

}
