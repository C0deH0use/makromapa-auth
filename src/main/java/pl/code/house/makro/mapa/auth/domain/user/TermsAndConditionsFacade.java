package pl.code.house.makro.mapa.auth.domain.user;

import static org.springframework.util.Assert.isTrue;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.code.house.makro.mapa.auth.domain.user.dto.TermsAndConditionsDto;
import pl.code.house.makro.mapa.auth.error.NewTermsAndConditionsNotApprovedException;

@Slf4j
@Service
@RequiredArgsConstructor
class TermsAndConditionsFacade {

  private final UserRepository userRepository;

  private final TermsAndConditionsRepository repository;

  TermsAndConditionsDto fetchLatestTerms() {
    return repository.findFirstByOrderByLastUpdatedAsc().toDto();
  }

  void isLatestTermsApproved(Long termsAndConditionsId) {
    TermsAndConditions latestTnC = repository.findFirstByOrderByLastUpdatedAsc();
    boolean userNotApprovedLatestTnC = latestTnC.getId().equals(termsAndConditionsId);

    if (!userNotApprovedLatestTnC) {
      throw new NewTermsAndConditionsNotApprovedException("New terms and conditions are required for user approval");
    }
  }

  @Transactional
  TermsAndConditionsDto approveTermsForUser(long termId, String userId) {
    TermsAndConditionsDto latestTerms = fetchLatestTerms();
    isTrue(latestTerms.getId() == termId, "");

    log.info("User {} has approved latest Terms and Conditions [{}]", userId, termId);
    UUID uuid = UUID.fromString(userId);
    userRepository.updateUserTermsAndConditionsId(uuid, termId);

    return latestTerms;
  }
}
