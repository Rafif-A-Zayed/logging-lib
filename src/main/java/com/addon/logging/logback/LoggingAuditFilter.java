package com.addon.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.addon.logging.LoggingConstants;

public class LoggingAuditFilter extends Filter<ILoggingEvent> {

  @Override
  public FilterReply decide(ILoggingEvent event) {
    if (event.getFormattedMessage().contains(LoggingConstants.EVENT_LOG_MARKER)
        || event.getFormattedMessage().contains(LoggingConstants.API_LOG_MARKER)) {
      return FilterReply.NEUTRAL;
    }

    return FilterReply.DENY;
  }
}
