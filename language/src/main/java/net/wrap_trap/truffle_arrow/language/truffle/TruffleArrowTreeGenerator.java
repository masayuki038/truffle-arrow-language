package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.frame.Frame;
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
    } else if (ast instanceof AST.Loop) {
      return visit(frame, (AST.Loop) ast);
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
      return new StatementEcho(expr);
    } else if ("return".equals(command.getCommand())) {
      return new ReturnNode(param);
    }
    throw new RuntimeException("Unknown AST.Command: " + command);
  }

  ExprIf visit(FrameDescriptor frame, AST.If ifNode) {
    ExprBase cond = visit(frame, ifNode.getExpression());
    StatementBase[] statements = ifNode.getStatements().stream()
                                  .map(ast -> visit(frame, ast))
                                  .toArray(StatementBase[]::new);
    return new ExprIf(cond, new Statements(statements), null);
  }

  StatementLoop visit(FrameDescriptor frame, AST.Loop loopNode) {
    ExprStringLiteral pathNode  = visit(frame, loopNode.getPath());
    List<StatementBase> list =
      loopNode.getStatements().stream().map(s -> visit(frame, s)).collect(Collectors.toList());
    StatementBase[] array = new StatementBase[list.size()];
    list.toArray(array);
    return new StatementLoop(pathNode, new Statements(array));
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
    return new ExprArrays(fields);
  }

  ExprGet visit(FrameDescriptor frame, AST.Get getNode) {
    ExprBase expr = visit(frame, getNode.getExpr());
    ExprBase orElse = visit(frame, getNode.getOrElse());
    return new ExprGet(expr, orElse);
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

  StatementStore visit(FrameDescriptor frame, AST.Store storeNode) {
    if (storeNode.getVariables().size() < 2) {
      throw new IllegalArgumentException("'store' requires two or more variables");
    }

    List<ExprBase> variables = storeNode.getVariables().stream().map(v -> visit(frame, v)).collect(Collectors.toList());
    return new StatementStore(variables);
  }
}
