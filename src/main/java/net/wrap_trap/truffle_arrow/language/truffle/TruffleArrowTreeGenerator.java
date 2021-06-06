package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import lombok.RequiredArgsConstructor;
import net.wrap_trap.truffle_arrow.language.parser.ast.AST;
import net.wrap_trap.truffle_arrow.language.truffle.node.*;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class TruffleArrowTreeGenerator {
  private final TruffleArrowLanguage lang;

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
    ExprStringNode pathNode  = visit(frame, loopNode.getPath());
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
      case "<":
        return ExprLessThanNodeGen.create(left, right);
      case ">":
        return ExprGreaterThanNodeGen.create(left, right);
    }
    throw new RuntimeException("Unknown binop " + op);
  }

  StatementBase visit(FrameDescriptor frame, AST.Assignment assign) {
    FrameSlot slot = frame.findOrAddFrameSlot(assign.getVariable().getVariableName(), FrameSlotKind.Illegal);
    return StatementWriteLocalNodeGen.create(visit(frame, assign.getExpression()), slot);
  }
  ExprBase visit(FrameDescriptor frame, AST.Variable variable) {
    FrameSlot slot = frame.findOrAddFrameSlot(variable.getVariableName(), FrameSlotKind.Illegal);
    return ExprReadLocalNodeGen.create(slot);
  }

  ExprDoubleNode visit(FrameDescriptor frame, AST.IntValue value) {
    return new ExprDoubleNode(value.getValue());
  }
  ExprStringNode visit(FrameDescriptor frame, AST.StringValue value) {
    return new ExprStringNode(value.getValue());
  }
}
