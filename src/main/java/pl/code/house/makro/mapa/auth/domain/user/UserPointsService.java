package pl.code.house.makro.mapa.auth.domain.user;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoUpdatePointsDto;

@Slf4j
@Service
@RequiredArgsConstructor
class UserPointsService {

  private final List<PointsOperationHandler> pointsHandlers;

  void handleUpdate(UserInfoUpdatePointsDto updatePointsDto, UUID userId) {
    PointsOperationDto operationDto = PointsOperationDto.from(userId, updatePointsDto);

    pointsHandlers.stream()
        .filter(handler -> handler.isAcceptable(operationDto))
        .findFirst()
        .ifPresent(handler -> handler.handle(operationDto));
  }
}
