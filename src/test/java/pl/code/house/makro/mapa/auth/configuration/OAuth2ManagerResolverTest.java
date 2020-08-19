package pl.code.house.makro.mapa.auth.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.BEARER_TOKEN;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_NEW_USER;
import static pl.code.house.makro.mapa.auth.domain.user.TestUser.GOOGLE_PREMIUM_USER;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@ExtendWith(MockitoExtension.class)
class OAuth2ManagerResolverTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private AuthenticationManager opaqueAuthenticationManager;

  @Mock
  private AuthenticationManagerResolver<HttpServletRequest> jwtResolver;

  @InjectMocks
  private OAuth2ManagerResolver sut;

  @Test
  @DisplayName("should return jwtResolver if token is JWT based")
  void shouldReturnJwtResolverIfTokenIsJwtBased() {
    //given
    given(request.getHeader(AUTHORIZATION)).willReturn(GOOGLE_NEW_USER.getAuthenticationHeader().getValue());

    //when
    AuthenticationManager manager = sut.resolve(request);

    //then
    then(jwtResolver).should(times(1)).resolve(any(HttpServletRequest.class));
    assertThat(manager).isNotEqualTo(opaqueAuthenticationManager);
  }

  @Test
  @DisplayName("should return Opaque Auth Manager if token is not JWT")
  void shouldReturnOpaqueAuthManagerIfTokenIsNotJwt() {
    //given
    given(request.getHeader(AUTHORIZATION)).willReturn(BEARER_TOKEN + GOOGLE_PREMIUM_USER.getAccessCode());

    //when
    AuthenticationManager manager = sut.resolve(request);

    //then
    then(jwtResolver).should(never()).resolve(any(HttpServletRequest.class));
    assertThat(manager).isEqualTo(opaqueAuthenticationManager);
  }

  @Test
  @DisplayName("throw if token is without bearer keyword")
  void throwIfTokenIsWithoutBearerKeyword() {
    //given
    given(request.getHeader(AUTHORIZATION)).willReturn(GOOGLE_PREMIUM_USER.getAccessCode());

    //when
    assertThatThrownBy(() -> sut.resolve(request))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessageContaining("Token value cannot be recognized");
    then(jwtResolver).should(never()).resolve(any(HttpServletRequest.class));
  }
}