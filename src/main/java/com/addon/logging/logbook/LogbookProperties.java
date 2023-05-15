package com.addon.logging.logbook;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@ConfigurationProperties(prefix = "logbook")
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogbookProperties {

  final Exclude exclude = new Exclude();
  final Obfuscate obfuscate = new Obfuscate();

  @Getter
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Obfuscate {

    final List<String> headers = new ArrayList<>();
    final List<String> parameters = new ArrayList<>();
    final List<String> paths = new ArrayList<>();
  }

  @Getter
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Exclude {
    final List<String> urls = new ArrayList<>();
    final List<String> body = new ArrayList<>();
  }
}
