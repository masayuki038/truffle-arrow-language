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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.apache.arrow.vector.util.Text;

import java.util.Objects;


@NodeInfo(shortName = "==")
abstract public class ExprEquals extends ExprBinary {

  @Specialization
  protected boolean eq(boolean left, boolean right) {
    return left == right;
  }

  @Specialization
  protected boolean eq(int left, int right) {
    return left == right;
  }

  @Specialization
  protected boolean eq(long left, long right) {
    return left == right;
  }

  @Specialization
  protected boolean eq(double left, double right) {
    return left == right;
  }

  @Specialization
  protected boolean ge(Object left, SqlNull right) {
    return (left == SqlNull.INSTANCE);
  }

  @Specialization
  protected boolean ge(SqlNull left, Object right) {
    return (right == SqlNull.INSTANCE);
  }

  @Specialization
  protected boolean eq(Text left, Text right) {
    return Objects.equals(left.toString(), right.toString());
  }

  @Specialization
  protected boolean eq(Text left, String right) {
    return Objects.equals(left.toString(), right);
  }

  @Specialization
  protected boolean eq(String left, Text right) {
    return Objects.equals(left, right.toString());
  }

  @Specialization
  protected boolean eq(String left, String right) {
    return Objects.equals(left, right);
  }

  @Specialization
  protected boolean eq(Text left, Object right) {
    return eq(left.toString(), right);
  }

  @Specialization
  protected boolean eq(Object left, Text right) {
    return eq(left, right.toString());
  }

  @Specialization
  @CompilerDirectives.TruffleBoundary
  protected boolean eq(Object left, Object right) {
    return ((Comparable) left).compareTo(right) == 0;
  }
}
