package com.addon.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.addon.logging.LoggingConstants;
import com.addon.logging.LoggingEventType;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.UUID;
import lombok.Setter;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.apache.commons.lang3.StringUtils;

@Setter
public class EventDataProvider extends AbstractFieldJsonProvider<ILoggingEvent>
    implements FieldNamesAware<LogstashFieldNames> {
  private static final String EVENT_ID = "{0}-{1}";
  private static String details = "details";
  private static String eventId = "eventId";
  private static String publisher = "publisher";
  private static String eventType = "eventType";
  private static String actor = "actor";

  private String serviceNumber;
  private String serviceName;

  @Override
  public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
    if (StringUtils.isNotBlank(getFieldName())) {
      if (!isStarted()) {
        throw new IllegalStateException("Generator is not started");
      }
      JsonWritingUtils.writeStringField(
          generator,
          eventId,
          MessageFormat.format(EVENT_ID, serviceNumber, UUID.randomUUID().toString()));
      JsonWritingUtils.writeStringField(generator, publisher, serviceName);

      if (event.getFormattedMessage().contains(LoggingConstants.EVENT_LOG_MARKER)) {
        JsonWritingUtils.writeStringField(
            generator, eventType, LoggingEventType.EVENT_AUDIT_LOG.getType());
      } else if (event.getFormattedMessage().contains(LoggingConstants.API_LOG_MARKER)) {
        JsonWritingUtils.writeStringField(
            generator, eventType, LoggingEventType.API_AUDIT_LOG.getType());
      }
      JsonWritingUtils.writeStringField(generator, actor, "SYSTEM");
      JsonWritingUtils.writeStringField(generator, details, event.getMessage());
    }
  }

  @Override
  public void setFieldNames(final LogstashFieldNames fieldNames) {
    setFieldName(details);
  }
}
