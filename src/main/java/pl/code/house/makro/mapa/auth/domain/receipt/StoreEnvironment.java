package pl.code.house.makro.mapa.auth.domain.receipt;

import io.vavr.collection.Stream;

public enum StoreEnvironment {
  SANDBOX,
  PRODUCTION,
  UNKNOWN;

  public static StoreEnvironment parseFrom(String value) {
    return Stream.of(values())
        .find(environment -> environment.name().equalsIgnoreCase(value))
        .getOrElse(UNKNOWN);
  }
}