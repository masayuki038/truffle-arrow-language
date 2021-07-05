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

import org.apache.arrow.vector.types.Types;
import org.apache.arrow.vector.types.Types.MinorType;
import org.apache.arrow.vector.types.pojo.ArrowType;

import java.util.HashMap;
import java.util.Map;

/**
 * Field Type for Apache Arrow
 */
public enum ArrowFieldType {
  STRING(String.class),
  BOOLEAN(Boolean.class),
  BYTE(Byte.class),
  CHAR(Character.class),
  SHORT(Short.class),
  INT(Integer.class),
  LONG(Long.class),
  FLOAT(Float.class),
  DOUBLE(Double.class),
  DECIMAL(java.math.BigDecimal.class, 18, 8),
  DATE(java.sql.Date.class),
  TIME(java.sql.Time.class),
  TIMESTAMP(java.sql.Timestamp.class),
  BYTEARRAY(byte[].class);

  private final Class clazz;
  private int precision;
  private int scale;
  private static final Map<MinorType, ArrowFieldType> MAP = new HashMap<>();

  static {
    MAP.put(MinorType.VARCHAR, STRING);
    MAP.put(MinorType.BIT, BOOLEAN);
    MAP.put(MinorType.INT, INT);
    MAP.put(MinorType.BIGINT, LONG);
    MAP.put(MinorType.FLOAT4, FLOAT);
    MAP.put(MinorType.FLOAT8, DOUBLE);
    MAP.put(MinorType.DECIMAL, DECIMAL);
    MAP.put(MinorType.DATEDAY, DATE);
    MAP.put(MinorType.TIMESEC, TIME);
    MAP.put(MinorType.TIMESTAMPSECTZ, TIMESTAMP);
    MAP.put(MinorType.TIMESTAMPMILLITZ, TIMESTAMP);
    MAP.put(MinorType.VARBINARY, BYTEARRAY);
  }

  ArrowFieldType(Class clazz) {
    this.clazz = clazz;
  }

  ArrowFieldType(Class clazz, int precision, int scale) {
    this.clazz = clazz;
    this.precision = precision;
    this.scale = scale;
  }

  public static ArrowFieldType of(ArrowType arrowType) {
    MinorType minorType = Types.getMinorTypeForArrowType(arrowType);
    return MAP.get(minorType);
  }
}