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

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import net.wrap_trap.truffle_arrow.language.parser.ast.AST;
import net.wrap_trap.truffle_arrow.language.truffle.node.*;

import java.util.List;
import java.util.stream.Collectors;


public class TruffleArrowTreeGenerator {

  public Statements visit(FrameDescriptor frame, List<AST.ASTNode> script) {
    StatementBase[] statements = script.stream()
             .map(ast -> visit(frame, ast))
             .toArray(StatementBase[]::new);
    return new Statements(statements);
  }

  StatementBase visit(FrameDescriptor frame, AST.ASTNode ast) {
    if (ast instanceof AST.Expression) {
      return visit(frame, (AST.Expression) ast);
    } else if (ast instanceof AST.Command) {
      return visit(frame, (AST.Command) ast);
    } else if (ast instanceof AST.If) {
      return visit(frame, (AST.If) ast);
    } else if (ast instanceof AST.Assignment) {
      return visit(frame, (AST.Assignment) ast);
    } else if (ast instanceof AST.Load) {
      return visit(frame, (AST.Load) ast);
    } else if (ast instanceof AST.Store) {
      return visit(frame, (AST.Store) ast);
    } else if (ast instanceof AST.MapMemberAssignment) {
      return visit(frame, (AST.MapMemberAssignment) ast);
    }
    throw new IllegalStateException("Unknown ASTNode: " + ast);
  }

  StatementBase visit(FrameDescriptor frame, AST.Command command) {
    ExprBase param = visit(frame, command.getParam());
    if ("echo".equals(command.getCommand())) {
      ExprBase expr = visit(frame, command.getParam());
      ExprBase func = new FunctionLiteral("echo");
      Invoke echo = new Invoke(func, new ExprBase[]{expr});
      echo.setSourceSection(command.getSourceIndex(), command.getSourceLength());
      return echo;
    } else if ("return".equals(command.getCommand())) {
      ReturnNode returns = new ReturnNode(param);
      returns.setSourceSection(command.getSourceIndex(), command.getSourceLength());
      return returns;
    }
    throw new RuntimeException("Unknown AST.Command: " + command);
  }

  ExprIf visit(FrameDescriptor frame, AST.If ifNode) {
    ExprBase cond = visit(frame, ifNode.getExpression());
    StatementBase[] statements = ifNode.getStatements().stream()
                                  .map(ast -> visit(frame, ast))
                                  .toArray(StatementBase[]::new);
    ExprIf ifs =  new ExprIf(cond, new Statements(statements), null);
    ifs.setSourceSection(ifNode.getSourceIndex(), ifNode.getSourceLength());
    return ifs;
  }

  StatementLoad visit(FrameDescriptor frame, AST.Load loadNode) {
    ExprStringLiteral pathNode  = visit(frame, loadNode.getPath());
    List<StatementBase> list =
      loadNode.getStatements().stream().map(s -> visit(frame, s)).collect(Collectors.toList());
    StatementBase[] array = new StatementBase[list.size()];
    list.toArray(array);
    StatementLoad load = new StatementLoad(pathNode, new Statements(array));
    load.setSourceSection(loadNode.getSourceIndex(), loadNode.getSourceLength());
    return load;
  }

   ExprBase visit(FrameDescriptor frame, AST.Expression exp) {
    if (exp instanceof AST.BinaryOperator) {
      return visit(frame, (AST.BinaryOperator) exp);
    } else if (exp instanceof AST.Variable) {
      return visit(frame, (AST.Variable) exp);
    } else if (exp instanceof AST.IntValue) {
      return visit(frame, (AST.IntValue) exp);
    } else if (exp instanceof AST.StringValue) {
      return visit(frame, (AST.StringValue) exp);
    } else if (exp instanceof AST.DoubleValue) {
      return visit(frame, (AST.DoubleValue) exp);
    } else if (exp instanceof AST.Arrays) {
      return visit(frame, (AST.Arrays) exp);
    } else if (exp instanceof AST.Get) {
      return visit(frame, (AST.Get) exp);
    } else if (exp instanceof AST.MapValue) {
      return visit(frame, (AST.MapValue) exp);
    } else if (exp instanceof AST.MapMember) {
      return visit(frame, (AST.MapMember) exp);
    } else if (exp instanceof AST.ArrayValue) {
      return visit(frame, (AST.ArrayValue) exp);
    }
    throw new RuntimeException("Unknown AST.Expression: " + exp);
  }

  ExprBase visit(FrameDescriptor frame, AST.BinaryOperator op) {
    ExprBase left = visit(frame, op.getLeft());
    ExprBase right = visit(frame, op.getRight());

    switch (op.getOp()) {
      case "+":
        return ExprPlusNodeGen.create(left, right);
      case "-":
        return ExprMinusNodeGen.create(left, right);
      case "*":
        return ExprMultipliedByNodeGen.create(left, right);
      case "/":
        return ExprDividedByNodeGen.create(left, right);
      case "<":
        return ExprLessThanNodeGen.create(left, right);
      case ">":
        return ExprGreaterThanNodeGen.create(left, right);
      case "==":
        return ExprEqualsNodeGen.create(left, right);
      case "<>":
        return ExprNotNodeGen.create(ExprEqualsNodeGen.create(left, right));
    }
    throw new RuntimeException("Unknown binop " + op);
  }

  ExprFieldDef visit(FrameDescriptor frame, AST.FieldDef op) {
    return new ExprFieldDef(op.getName(), op.getType());
  }

  StatementBase visit(FrameDescriptor frame, AST.Assignment assign) {
    FrameSlot slot = frame.findOrAddFrameSlot(assign.getVariable().getVariableName(), FrameSlotKind.Illegal);
    return StatementWriteLocalNodeGen.create(visit(frame, assign.getExpression()), slot);
  }

  ExprReadLocal visit(FrameDescriptor frame, AST.Variable variable) {
    FrameSlot slot = frame.findOrAddFrameSlot(variable.getVariableName(), FrameSlotKind.Illegal);
    return ExprReadLocalNodeGen.create(slot);
  }

  ExprIntegerLiteral visit(FrameDescriptor frame, AST.IntValue value) {
    return new ExprIntegerLiteral(value.getValue());
  }

  ExprStringLiteral visit(FrameDescriptor frame, AST.StringValue value) {
    return new ExprStringLiteral(value.getValue());
  }

  ExprDoubleLiteral visit(FrameDescriptor frame, AST.DoubleValue value) {
    return new ExprDoubleLiteral(value.getValue());
  }

  ExprArrays visit(FrameDescriptor frame, AST.Arrays arraysNode) {
    List<ExprFieldDef> fields =
      arraysNode.getFieldDefs().stream().map(s -> visit(frame, s)).collect(Collectors.toList());
    ExprArrays arrays =  new ExprArrays(fields);
    arrays.setSourceSection(arraysNode.getSourceIndex(), arraysNode.getSourceLength());
    return arrays;
  }

  ExprGet visit(FrameDescriptor frame, AST.Get getNode) {
    ExprBase expr = visit(frame, getNode.getExpr());
    ExprBase orElse = visit(frame, getNode.getOrElse());
    ExprGet get = new ExprGet(expr, orElse);
    get.setSourceSection(getNode.getSourceIndex(), getNode.getSourceLength());
    return get;
  }

  ExprNewMapLiteral visit(FrameDescriptor frame, AST.MapValue mapValueNode) {
    return new ExprNewMapLiteral();
  }

  ExprMapMember visit(FrameDescriptor frame, AST.MapMember mapMember) {
    ExprReadLocal readLocal = visit(frame, mapMember.getMap());
    ExprBase member = visit(frame, mapMember.getMember());
    return new ExprMapMember(readLocal, member);
  }

  StatementMapMemberWrite visit(FrameDescriptor frame, AST.MapMemberAssignment mapMemberAssignment) {
    ExprReadLocal readLocal = visit(frame, mapMemberAssignment.getMap());
    ExprBase member = visit(frame, mapMemberAssignment.getMember());
    ExprBase value = visit(frame, mapMemberAssignment.getValue());
    return new StatementMapMemberWrite(readLocal, member, value);
  }

  ExprNewArrayLiteral visit(FrameDescriptor frame, AST.ArrayValue arrayValueNode) {
    List<ExprBase> variables = arrayValueNode.getVariables().stream().map(v -> visit(frame, v)).collect(Collectors.toList());
    return new ExprNewArrayLiteral(variables);
  }

  Invoke visit(FrameDescriptor frame, AST.Store storeNode) {
    if (storeNode.getVariables().size() < 2) {
      throw new IllegalArgumentException("'store' requires two or more variables");
    }

    List<ExprBase> variables = storeNode.getVariables().stream().map(v -> visit(frame, v)).collect(Collectors.toList());
    ExprBase[] params = new ExprBase[variables.size()];
    variables.toArray(params);

    ExprBase func = new FunctionLiteral("store");
    Invoke store = new Invoke(func, params);
    store.setSourceSection(storeNode.getSourceIndex(), storeNode.getSourceLength());
    return store;
  }
}
