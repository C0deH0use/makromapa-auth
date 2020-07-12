package pl.code.house.makro.mapa.auth.domain.user;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@ToString
@EqualsAndHashCode
@Getter(PACKAGE)
@Builder(access = PACKAGE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
class UserDetails {

  @Column(name = "name")
  private String name;

  @Column(name = "surname")
  private String surname;

  @Column(name = "email")
  private String email;

  @Column(name = "picture")
  private String picture;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private UserType type;
}
