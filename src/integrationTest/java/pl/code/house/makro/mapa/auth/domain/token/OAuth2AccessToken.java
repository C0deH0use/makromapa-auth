package pl.code.house.makro.mapa.auth.domain.token;

import static javax.persistence.AccessType.FIELD;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_access_token")
@Access(FIELD)
@Getter(PACKAGE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
class OAuth2AccessToken {

  @Id
  @Column(name = "token_id")
  private String tokenId;

  @Column(name = "user_name")
  private String userName;

}
