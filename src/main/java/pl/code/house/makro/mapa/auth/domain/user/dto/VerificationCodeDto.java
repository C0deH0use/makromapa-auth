package pl.code.house.makro.mapa.auth.domain.user.dto;

import static lombok.AccessLevel.PUBLIC;

import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.CodeType;

@Value
@Builder(access = PUBLIC)
public class VerificationCodeDto {

  UUID id;

  UserDto user;

  Boolean enabled;

  String code;

  CodeType codeType;

  ZonedDateTime expiresOn;

}
