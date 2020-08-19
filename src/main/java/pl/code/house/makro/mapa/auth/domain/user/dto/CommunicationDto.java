package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.util.UUID;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.CodeType;
import pl.code.house.makro.mapa.auth.domain.user.CommunicationProtocol;

@Value
public class CommunicationDto {

  UUID userId;
  CodeType codeType;
  String communicationTarget;
  CommunicationProtocol communicationChannel;
}
