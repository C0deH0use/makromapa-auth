package pl.code.house.makro.mapa.auth.domain.user;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static pl.code.house.makro.mapa.auth.domain.user.PremiumFeature.NON;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.user.dto.PointsProductDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoDto;
import pl.code.house.makro.mapa.auth.domain.user.dto.UserInfoUpdatePointsDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointsFacade {

  private final UserRepository userRepository;

  private final PointProductRepository productRepository;

  private final UserPointsService pointsService;

  private final UserAuthoritiesService authoritiesService;

  @Transactional
  public Optional<UserInfoDto> updatePointsFor(UUID userId, UserInfoUpdatePointsDto updatePointsDto) {
    pointsService.handleUpdate(updatePointsDto, userId);

    return userRepository.findById(userId)
        .map(this::toUserInfo);
  }

  public List<PointsProductDto> fetchAllPointsProducts() {
    return productRepository.findAll()
        .stream()
        .map(PointsProduct::toDto)
        .collect(toList());
  }

  private UserInfoDto toUserInfo(BaseUser user) {
    return user.toUserInfo(getUserPremiumFeatures(user));
  }

  private Set<PremiumFeature> getUserPremiumFeatures(BaseUser user) {
    return authoritiesService.getUserAuthorities(user.getId())
        .stream()
        .map(PremiumFeature::fromAuthority)
        .filter(not(NON::equals))
        .collect(toSet());
  }
}