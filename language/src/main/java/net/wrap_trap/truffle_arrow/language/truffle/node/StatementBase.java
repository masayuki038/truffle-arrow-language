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
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;


/**
 * Base of all statements.
 */
@TypeSystemReference(SqlTypes.class)
@GenerateWrapper
@NodeInfo(description = "The abstract base node for all statements")
public abstract class StatementBase extends Node implements InstrumentableNode {

  private boolean hasRootTag;

  private int sourceIndex;
  private int sourceLength;

  public abstract void executeVoid(VirtualFrame frame);

  @Override
  public boolean isInstrumentable() {
    return true;
  }

  @Override
  public WrapperNode createWrapper(ProbeNode probe) {
    return new StatementBaseWrapper(this, probe);
  }

  @Override
  public boolean hasTag(Class<? extends Tag> tag) {
    if (tag == StandardTags.StatementTag.class) {
      return true;
    } else if (tag == StandardTags.RootTag.class || tag == StandardTags.RootBodyTag.class) {
      return hasRootTag;
    }
    return false;
  }

  public final void setSourceSection(int index, int length) {
    this.sourceIndex = index;
    this.sourceLength = length;
  }

  @Override
  @CompilerDirectives.TruffleBoundary
  public final SourceSection getSourceSection() {
    if (this.sourceLength == 0) {
      return null;
    }

    RootNode rootNode = getRootNode();
    if (rootNode == null) {
      // not yet adopted yet
      return null;
    }
    SourceSection rootSourceSection = rootNode.getSourceSection();
    if (rootSourceSection == null) {
      return null;
    }
    Source source = rootSourceSection.getSource();
    return source.createSection(this.sourceIndex, this.sourceLength);
  }

  public final void addRootTag() {
    this.hasRootTag = true;
  }
}
