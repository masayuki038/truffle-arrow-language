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

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.Source;
import net.wrap_trap.truffle_arrow.language.parser.TruffleArrowParser;
import net.wrap_trap.truffle_arrow.language.parser.ast.AST;
import net.wrap_trap.truffle_arrow.language.truffle.node.ExprBase;
import net.wrap_trap.truffle_arrow.language.truffle.node.ExprReadArgument;
import net.wrap_trap.truffle_arrow.language.truffle.node.Statements;
import net.wrap_trap.truffle_arrow.language.truffle.node.builtins.TruffleArrowBuiltin;
import org.jparsec.Parser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@TruffleLanguage.Registration(id=TruffleArrowLanguage.ID, name = "TruffleArrow", version = "0.1", mimeType = TruffleArrowLanguage.MIME_TYPE)
@ProvidedTags({StandardTags.RootTag.class, StandardTags.ExpressionTag.class, StandardTags.StatementTag.class,
  DebuggerTags.AlwaysHalt.class, StandardTags.RootBodyTag.class, StandardTags.ReadVariableTag.class, StandardTags.WriteVariableTag.class})
public class TruffleArrowLanguage extends TruffleLanguage<TruffleArrowContext> {
  public static final String ID = "ta";
  public static final String MIME_TYPE = "application/x-truffle-arrow";
  private static final Source BUILTIN_SOURCE = Source.newBuilder(TruffleArrowLanguage.ID, "", "TA builtin").build();

  private final Map<NodeFactory<? extends TruffleArrowBuiltin>, RootCallTarget> builtinTargets = new ConcurrentHashMap<>();

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    Parser<List<AST.ASTNode>> parser = TruffleArrowParser.createParser();
    Source source = request.getSource();
    List<AST.ASTNode> script = parser.parse(source.getReader());
    TruffleArrowTreeGenerator generator = new TruffleArrowTreeGenerator();
    FrameDescriptor frame = new FrameDescriptor();
    Statements statements = generator.visit(frame, script);
    statements.setSourceSection(0, 1);
    statements.addRootTagsToStatements();
    TruffleArrowRootNode root = new TruffleArrowRootNode(this, frame, source.createUnavailableSection(), statements);
    return Truffle.getRuntime().createCallTarget(root);
  }

  @Override
  protected TruffleArrowContext createContext(Env env) {
    return new TruffleArrowContext(this);
  }

  public RootCallTarget lookupBuiltin(NodeFactory<? extends TruffleArrowBuiltin> factory) {
    RootCallTarget target = builtinTargets.get(factory);
    if (target != null) {
      return target;
    }

    int argumentCount = factory.getExecutionSignature().size();
    ExprBase[] argumentNodes = new ExprBase[argumentCount];

    for (int i = 0; i < argumentCount; i++) {
      argumentNodes[i] = new ExprReadArgument(i);
    }
    TruffleArrowBuiltin builtinBodyNode = factory.createNode((Object) argumentNodes);
    builtinBodyNode.addRootTag();

    String name = lookupNodeInfo(builtinBodyNode.getClass()).shortName();
    TruffleArrowFunctionRoot rootNode = new TruffleArrowFunctionRoot(this, new FrameDescriptor(), BUILTIN_SOURCE.createUnavailableSection(), name, builtinBodyNode);

    RootCallTarget newTarget = Truffle.getRuntime().createCallTarget(rootNode);
    RootCallTarget oldTarget = builtinTargets.put(factory, newTarget);
    if (oldTarget != null) {
      return oldTarget;
    }
    return newTarget;
  }

  public static NodeInfo lookupNodeInfo(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    NodeInfo info = clazz.getAnnotation(NodeInfo.class);
    if (info != null) {
      return info;
    } else {
      return lookupNodeInfo(clazz.getSuperclass());
    }
  }


}
