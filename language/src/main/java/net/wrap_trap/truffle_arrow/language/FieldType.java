/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wrap_trap.truffle_arrow.language;

import java.util.HashMap;
import java.util.Map;


public enum FieldType {
  INT("INT"),
  BIGINT("BIGINT"),
  DOUBLE("DOUBLE"),
  STRING("STRING");

  private String typeName;
  private static final Map<String, FieldType> MAP = new HashMap<>();

  FieldType(String typeName) {
    this.typeName = typeName;
  }

  static {
    MAP.put(INT.toString(), INT);
    MAP.put(BIGINT.toString(), BIGINT);
    MAP.put(DOUBLE.toString(), DOUBLE);
    MAP.put(STRING.toString(), STRING);
  }

  @Override
  public String toString() {
    return typeName;
  }

  public static FieldType of(String typeName) {
    return MAP.get(typeName);
  }
}
