/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.caliper.bridge;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A message representing output produced by the JVM when {@code -XX:+PrintFlagsFinal} is enabled.
 */
public final class VmOptionLogMessage extends LogMessage {
  // TODO(gak): consider putting these in a file
  @VisibleForTesting static final Pattern PATTERN =
      Pattern.compile("\\s*(\\w+)\\s+(\\w+)\\s+:?=\\s+([^\\s]*)\\s+\\{([^}]*)\\}\\s*");

  public static final class Parser implements TryParser<VmOptionLogMessage> {
    @Override
    public Optional<VmOptionLogMessage> tryParse(String text) {
      Matcher matcher = PATTERN.matcher(text);
      return matcher.matches() ? Optional.of(
          new VmOptionLogMessage(matcher.group(2), matcher.group(3)))
          : Optional.<VmOptionLogMessage>absent();
    }
  }

  private final String name;
  private final String value;

  private VmOptionLogMessage(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String name() {
    return name;
  }

  public String value() {
    return value;
  }

  @Override
  public void accept(LogMessageVisitor visitor) {
    visitor.visit(this);
  }
}
