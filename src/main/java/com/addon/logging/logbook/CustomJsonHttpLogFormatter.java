package com.addon.logging.logbook;

import com.addon.logging.LoggingConstants;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.StructuredHttpLogFormatter;

public class CustomJsonHttpLogFormatter implements StructuredHttpLogFormatter {

  public static final String MARKER = "Marker";
  private static final String APP_NAME = "app-name";
  private static final String APP_NAME_VALUE = "BAZAAR";
  private static final String ORIGIN = "origin";
  private static final String TYPE = "api-type";
  private static final String CORRELATION_ID = "correlationId";
  private static final String PROTOCOL = "protocol";
  private static final String REMOTE = "remote";
  private static final String METHOD = "method";
  private static final String URI = "uri";
  private static final String STATUS = "status";
  private static final String DURATION = "duration";
  private static final String HEADERS = "headers";
  private static final String BODY = "body";
  private final ObjectMapper mapper;

  public CustomJsonHttpLogFormatter() {
    this(new ObjectMapper());
  }

  public CustomJsonHttpLogFormatter(final ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Map<String, Object> prepare(final Correlation correlation, final HttpResponse response)
      throws IOException {
    final Map<String, Object> content = new LinkedHashMap<>();
    content.put(MARKER, LoggingConstants.API_LOG_MARKER);
    content.put(APP_NAME, APP_NAME_VALUE);
    content.put(ORIGIN, response.getOrigin().name().toLowerCase(Locale.ROOT));
    content.put(TYPE, "response");
    content.put(CORRELATION_ID, correlation.getId());
    content.put(DURATION, correlation.getDuration().toMillis());
    content.put(PROTOCOL, response.getProtocolVersion());
    content.put(STATUS, response.getStatus());
    prepareHeaders1(response).ifPresent(headers -> content.put(HEADERS, headers.toString()));
    prepareBody(response).ifPresent(body -> content.put(BODY, body));
    return content;
  }

  @Override
  public Map<String, Object> prepare(final Precorrelation precorrelation, final HttpRequest request)
      throws IOException {
    final String correlationId = precorrelation.getId();

    final Map<String, Object> content = new LinkedHashMap<>();
    content.put(MARKER, LoggingConstants.API_LOG_MARKER);
    content.put(APP_NAME, APP_NAME_VALUE);
    content.put(ORIGIN, request.getOrigin().name().toLowerCase(Locale.ROOT));
    content.put(TYPE, "request");
    content.put(CORRELATION_ID, correlationId);
    content.put(PROTOCOL, request.getProtocolVersion());
    content.put(REMOTE, request.getRemote());
    content.put(METHOD, request.getMethod());
    content.put(URI, request.getRequestUri());
    prepareHeaders1(request).ifPresent(headers -> content.put(HEADERS, headers.toString()));
    prepareBody(request).ifPresent(body -> content.put(BODY, body));
    return content;
  }

  private Optional<Object> prepareHeaders1(final HttpMessage message) {

    final List<String> command = new ArrayList<>();
    message
        .getHeaders()
        .forEach(
            (header, values) ->
                values.forEach(
                    value -> command.add(quote(header + ": " + String.join(";", value)))));

    return Optional.ofNullable(command.isEmpty() ? null : String.join("|", command));
  }

  @Override
  public Optional<Object> prepareBody(final HttpMessage message) throws IOException {
    final String contentType = message.getContentType();
    final String body = message.getBodyAsString();
    if (body.isEmpty()) {
      return Optional.empty();
    }
    if (JsonMediaType.JSON.test(contentType)) {
      return Optional.of(new JsonBody(quote(body)));
    } else {
      return Optional.of(quote(body));
    }
  }

  @Override
  public String format(final @NonNull Map<String, Object> content) throws IOException {

    return mapper.writeValueAsString(content);
  }

  @AllArgsConstructor
  private static final class JsonBody {

    String json;

    @JsonRawValue
    @JsonValue
    public String getJson() {
      return json;
    }
  }

  private static String quote(final String s) {
    return "\"" + escape(s) + "\"";
  }

  private static String escape(final String s) {
    return s.replace("\"", "\\\"");
  }
}
