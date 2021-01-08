package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TermsAndConditionsDto implements Serializable {

  private static final long serialVersionUID = -4905669893146398690L;

  Long id;

  String contractPl;

  String contractEn;
}
