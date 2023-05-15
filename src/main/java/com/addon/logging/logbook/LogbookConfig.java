package com.addon.logging.logbook;

import static org.zalando.logbook.BodyFilters.defaultValue;
import static org.zalando.logbook.json.JsonPathBodyFilters.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.Conditions;
import org.zalando.logbook.DefaultHttpLogWriter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HeaderFilters;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.QueryFilters;

@Component
@RequiredArgsConstructor
public class LogbookConfig {
  private static final String obfuscated = "OBFUSCATED";
  private final LogbookProperties logbookProperties;

  private final ObjectMapper objectMapper;

  private final Tracer tracer;

  @Bean
  public Logbook logBook() {

    return Logbook.builder()
        .headerFilter(getHeaderFilter())
        .queryFilter(getQueryFilter())
        .correlationId(request -> Objects.requireNonNull(tracer.currentSpan()).context().traceId())
        .bodyFilter(getBodyFilter())
        .sink(
            new DefaultSink(
                new CustomJsonHttpLogFormatter(objectMapper), new DefaultHttpLogWriter()))
        .condition(getCondition())
        .build();
  }

  private BodyFilter getBodyFilter() {

    if (logbookProperties.getExclude().getBody().isEmpty()) return defaultValue();

    List<BodyFilter> bodyFilterList = new ArrayList<>();
    for (String value : logbookProperties.getExclude().getBody()) {
      bodyFilterList.add(jsonPath("$." + value).delete());
    }
    return bodyFilterList.stream().reduce(BodyFilter.none(), BodyFilter::merge);
  }

  private Predicate<HttpRequest> getCondition() {
    return Conditions.exclude(
        logbookProperties.getExclude().getUrls().stream()
            .map(Conditions::requestTo)
            .collect(Collectors.toList()));
  }

  private QueryFilter getQueryFilter() {
    return logbookProperties.getObfuscate().getParameters().isEmpty()
        ? QueryFilters.defaultValue()
        : QueryFilters.replaceQuery(
            new HashSet<>(logbookProperties.getObfuscate().getParameters())::contains, obfuscated);
  }

  private HeaderFilter getHeaderFilter() {
    return logbookProperties.getObfuscate().getHeaders().isEmpty()
        ? HeaderFilters.defaultValue()
        : HeaderFilters.replaceHeaders(
            logbookProperties.getObfuscate().getHeaders()::contains, obfuscated);
  }
}
