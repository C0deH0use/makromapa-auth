package pl.code.house.makro.mapa.auth.domain.mail;

import static org.thymeleaf.templatemode.TemplateMode.HTML;
import static org.thymeleaf.templatemode.TemplateMode.TEXT;

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class SpringMailConfiguration {

  private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";

  @Bean
  public ResourceBundleMessageSource emailMessageSource() {
    final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("mail/");
    return messageSource;
  }

  @Bean
  public TemplateEngine emailTemplateEngine() {
    final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    // Resolver for TEXT emails
    templateEngine.addTemplateResolver(textTemplateResolver());
    // Resolver for HTML emails (except the editable one)
    templateEngine.addTemplateResolver(htmlTemplateResolver());
    // Resolver for HTML editable emails (which will be treated as a String)
    templateEngine.addTemplateResolver(stringTemplateResolver());
    // Message source, internationalization specific to emails
    templateEngine.setTemplateEngineMessageSource(emailMessageSource());
    return templateEngine;
  }

  private ITemplateResolver textTemplateResolver() {
    final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setOrder(1);
    templateResolver.setResolvablePatterns(Collections.singleton("text/*"));
    templateResolver.setPrefix("templates/");
    templateResolver.setSuffix(".txt");
    templateResolver.setTemplateMode(TEXT);
    templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
    templateResolver.setCacheable(false);
    return templateResolver;
  }

  private ITemplateResolver htmlTemplateResolver() {
    final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setOrder(2);
    templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
    templateResolver.setPrefix("templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(HTML);
    templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
    templateResolver.setCacheable(false);
    return templateResolver;
  }

  private ITemplateResolver stringTemplateResolver() {
    final StringTemplateResolver templateResolver = new StringTemplateResolver();
    templateResolver.setOrder(3);
    // No resolvable pattern, will simply process as a String template everything not previously matched
    templateResolver.setTemplateMode(HTML);
    templateResolver.setCacheable(false);
    return templateResolver;
  }

}
