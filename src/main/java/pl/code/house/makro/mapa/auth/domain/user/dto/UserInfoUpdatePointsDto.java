package pl.code.house.makro.mapa.auth.domain.user.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.PointsOperationReason;

@Value
public class UserInfoUpdatePointsDto implements Serializable {

  private static final long serialVersionUID = -2055694038701358253L;

  @NotNull(message = "Product is required")
  Long product;

  @NotNull(message = "Operation reason is required")
  PointsOperationReason operation;

}
