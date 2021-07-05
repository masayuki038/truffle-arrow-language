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

package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "-")
public abstract class ExprMinus extends ExprBinary {

  @Specialization
  protected long add(int left, int right) {
    return left - right;
  }

  @Specialization
  protected long add(long left, long right) {
    return left - right;
  }

  @Specialization
  protected long add(long left, int right) {
    return left - right;
  }

  @Specialization
  protected long add(int left, long right) {
    return left - right;
  }

  @Specialization
  protected double add(double left, double right) {
    return left - right;
  }

  @Specialization
  protected SqlNull leftNull(SqlNull left, Object right) {
    return SqlNull.INSTANCE;
  }

  @Specialization
  protected SqlNull rightNull(Object left, SqlNull right) {
    return SqlNull.INSTANCE;
  }
}
