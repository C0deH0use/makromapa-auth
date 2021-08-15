package pl.code.house.makro.mapa.auth.domain.receipt;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class StoreDeserialization extends JsonDeserializer<StoreEnvironment> {

  @Override
  public StoreEnvironment deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return StoreEnvironment.parseFrom(jsonParser.getValueAsString());
  }
}
