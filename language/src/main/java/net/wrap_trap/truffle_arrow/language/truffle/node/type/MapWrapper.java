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

package net.wrap_trap.truffle_arrow.language.truffle.node.type;

import com.oracle.truffle.api.interop.TruffleObject;

import java.util.Map;

public class MapWrapper implements TruffleObject {
  private final Map<Object, Object> map;

  public MapWrapper(Map<Object, Object> map) {
    this.map = map;
  }

  public Map<Object, Object> getMap() {
    return map;
  }

  @Override
  public int hashCode() {
    return this.map.hashCode();
  }

  @Override
  public String toString() {
    return this.map.toString();
  }
}
