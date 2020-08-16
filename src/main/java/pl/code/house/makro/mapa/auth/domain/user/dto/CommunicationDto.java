package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.CommunicationProtocol;

@Value
public class CommunicationDto {

  UUID userId;
  String communicationTarget;
  CommunicationProtocol communicationChannel;
  String code;
  ZonedDateTime expiryDate;
}
