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
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;

/**
 * Read a local variable.
 * Based on SLReadLocalVariableNode
 */
@NodeField(name = "slot", type = FrameSlot.class)
public abstract class ExprReadLocal extends ExprBase {
  public abstract FrameSlot getSlot();

  @Specialization(guards = "isBoolean(frame)")
  protected boolean readBoolean(VirtualFrame frame) {
    return FrameUtil.getBooleanSafe(frame, getSlot());
  }

  @Specialization(guards = "isInteger(frame)")
  protected int readInteger(VirtualFrame frame) {
    return FrameUtil.getIntSafe(frame, getSlot());
  }

  @Specialization(guards = "isLong(frame)")
  protected long readLong(VirtualFrame frame) {
    return FrameUtil.getLongSafe(frame, getSlot());
  }

  @Specialization(guards = "isDouble(frame)")
  protected double readDouble(VirtualFrame frame) {
    return FrameUtil.getDoubleSafe(frame, getSlot());
  }

  @Specialization(guards = "isObject(frame)")
  protected Object readObject(VirtualFrame frame) {
    if (!frame.isObject(getSlot())) {
      /*
       * The FrameSlotKind has been set to Object, so from now on all writes to the local
       * variable will be Object writes. However, now we are in a frame that still has an old
       * non-Object value. This is a slow-path operation: we read the non-Object value, and
       * write it immediately as an Object value so that we do not hit this path again
       * multiple times for the same variable of the same frame.
       */
      CompilerDirectives.transferToInterpreter();
      Object result = frame.getValue(getSlot());

      frame.setObject(getSlot(), result);

      return result;
    }

    return FrameUtil.getObjectSafe(frame, getSlot());
  }

  protected boolean isBoolean(VirtualFrame frame) {
    return getSlot().getKind() == FrameSlotKind.Boolean;
  }

  protected boolean isInteger(VirtualFrame frame) {
    return getSlot().getKind() == FrameSlotKind.Int;
  }

  protected boolean isLong(VirtualFrame frame) {
    return getSlot().getKind() == FrameSlotKind.Long;
  }

  protected boolean isDouble(VirtualFrame frame) {
    return getSlot().getKind() == FrameSlotKind.Double;
  }

  protected boolean isObject(VirtualFrame frame) {
    return getSlot().getKind() == FrameSlotKind.Object;
  }

  @Override
  public boolean hasTag(Class<? extends Tag> tag) {
    return tag == StandardTags.ReadVariableTag.class || super.hasTag(tag);
  }
}
