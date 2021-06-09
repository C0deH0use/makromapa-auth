package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.UUID.fromString;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static pl.code.house.makro.mapa.auth.ApiConstraints.OAUTH_USER_PATH;

import java.util.UUID;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoUpdatePointsDto;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = OAUTH_USER_PATH, produces = APPLICATION_JSON_VALUE, consumes = ALL_VALUE)
class UserPointsResource {

  private final UserPointsFacade facade;

  @PostMapping("/points")
  ResponseEntity<UserInfoDto> updateUserPoints(Authentication principal, @Valid UserInfoUpdatePointsDto updatePointsDto) {
    UUID userId = fromString(principal.getName());
    log.info("Request to update User {} points, via {} operation:{}", userId, updatePointsDto.getOperation(), updatePointsDto.getProduct());

    return facade.updatePointsFor(userId, updatePointsDto)
        .map(ResponseEntity::ok)
        .orElse(notFound().build());
  }

  @PostMapping("/{userId}/points")
  ResponseEntity<UserInfoDto> updateUserPoints(@PathVariable UUID userId, @Valid UserInfoUpdatePointsDto updatePointsDto) {
    log.info("Request to update User {} points, via {} operation:{}", userId, updatePointsDto.getOperation(), updatePointsDto.getProduct());

    return facade.updatePointsFor(userId, updatePointsDto)
        .map(ResponseEntity::ok)
        .orElse(notFound().build());
  }
}
