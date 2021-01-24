package pl.code.house.makro.mapa.auth.domain.user;

import static lombok.AccessLevel.PACKAGE;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoUpdatePointsDto;

@Value
@Builder(access = PACKAGE)
class PointsOperationDto {

  UUID userId;
  PointsOperationReason operation;
  Long product;

  static PointsOperationDto from(UUID userId, UserInfoUpdatePointsDto updatePointsDto) {
    return PointsOperationDto.builder()
        .userId(userId)
        .operation(updatePointsDto.getOperation())
        .product(updatePointsDto.getProduct())
        .build();
  }
}
