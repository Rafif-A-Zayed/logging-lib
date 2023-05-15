package com.addon.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoggingEventType {
  EVENT_AUDIT_LOG("event.audit.log"),
  API_AUDIT_LOG("api.audit.log");

  private final String type;
}
