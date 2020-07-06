package pl.code.house.makro.mapa.auth.configuration;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
class JsonConfiguration {

  @Bean
  @Primary
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper defaultObjectMapper) {
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
    jsonConverter.setObjectMapper(defaultObjectMapper);
    return jsonConverter;
  }

  @Bean
  @Primary
  public ObjectMapper defaultObjectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();

    final SimpleModule customsModule = new SimpleModule();
    objectMapper.registerModule(customsModule);

    final JavaTimeModule javaTimeModule = new JavaTimeModule();
    objectMapper.registerModule(javaTimeModule);
    objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.setDateFormat(new StdDateFormat());

    return objectMapper;
  }

  @Bean
  Clock systemClock() {
    return Clock.system(ZoneId.of("Europe/Warsaw"));
  }
}
