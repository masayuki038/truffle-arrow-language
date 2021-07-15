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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;

import java.util.Map;

public class StatementMapMemberWrite extends StatementBase {

  final ExprReadLocal mapVariable;
  final ExprBase member;
  final ExprBase value;

  public StatementMapMemberWrite(ExprReadLocal mapVariable, ExprBase member, ExprBase value) {
    this.mapVariable = mapVariable;
    this.member = member;
    this.value = value;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {
    Object o = this.mapVariable.readObject(frame);
    if (!(o instanceof Map)) {
      throw new IllegalArgumentException("Map expected, but: " + o);
    }
    Map<Object, Object> map = (Map<Object, Object>) o;
    map.put(this.member.executeGeneric(frame), this.value.executeGeneric(frame));
  }

  @Override
  public boolean hasTag(Class<? extends Tag> tag) {
    return tag == StandardTags.WriteVariableTag.class || super.hasTag(tag);
  }
}
