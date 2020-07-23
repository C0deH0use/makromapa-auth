package pl.code.house.makro.mapa.auth.domain;

import static java.time.ZonedDateTime.now;

import java.time.Clock;
import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

@MappedSuperclass
public class AuditAwareEntity {

  @Column(name = "created", nullable = false, updatable = false)
  protected ZonedDateTime created;

  @Column(name = "last_updated", nullable = false)
  protected ZonedDateTime lastUpdated;

  @PrePersist
  public void setCreatedIfNull() {
    if (created == null) {
      created = now(Clock.systemUTC());
    }

    lastUpdated = now();
  }
}
