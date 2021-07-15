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
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.source.Source;
import net.wrap_trap.truffle_arrow.language.parser.TruffleArrowParser;
import net.wrap_trap.truffle_arrow.language.parser.ast.AST;
import net.wrap_trap.truffle_arrow.language.truffle.node.Statements;
import org.jparsec.Parser;

import java.util.List;


@TruffleLanguage.Registration(id=TruffleArrowLanguage.ID, name = "TruffleArrow", version = "0.1", mimeType = TruffleArrowLanguage.MIME_TYPE)
@ProvidedTags({StandardTags.RootTag.class, StandardTags.ExpressionTag.class, StandardTags.StatementTag.class,
  DebuggerTags.AlwaysHalt.class, StandardTags.RootBodyTag.class, StandardTags.ReadVariableTag.class, StandardTags.WriteVariableTag.class})
public class TruffleArrowLanguage extends TruffleLanguage {
  public static final String ID = "ta";
  public static final String MIME_TYPE = "application/x-truffle-arrow";
  private static final Source SOURCE = Source.newBuilder(ID, "", "TA Source").build();

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
    return new TruffleArrowContext();
  }
}
