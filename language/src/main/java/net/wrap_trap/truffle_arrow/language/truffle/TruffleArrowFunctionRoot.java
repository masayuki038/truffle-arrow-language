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

package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import net.wrap_trap.truffle_arrow.language.truffle.node.ExprBase;

public class TruffleArrowFunctionRoot extends RootNode {

  @Child
  private ExprBase exprBase;
  private final String name;
  private final SourceSection sourceSection;

  public TruffleArrowFunctionRoot(TruffleLanguage<?> language, FrameDescriptor frameDescriptor, SourceSection sourceSection, String name, ExprBase exprBase) {
    super(language, frameDescriptor);
    this.sourceSection = sourceSection;
    this.name = name;
    this.exprBase = exprBase;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    return this.exprBase.executeGeneric(frame);
  }

  @Override
  public String getName() {
    return this.name;
  }
}
