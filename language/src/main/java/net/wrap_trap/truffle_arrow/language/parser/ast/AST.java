package net.wrap_trap.truffle_arrow.language.parser.ast;

import lombok.Value;
import net.wrap_trap.truffle_arrow.language.FieldType;

import java.util.List;

public class AST {

  public interface ASTNode {}
  public interface Expression extends ASTNode {}

  @Value
  public static class IntValue implements Expression {
    int value;
  }

  public static IntValue intValue(int value) {
    return new IntValue(value);
  }

  @Value
  public static class DoubleValue implements Expression {
    double value;
  }

  public static DoubleValue doubleValue(double value) {
    return new DoubleValue(value);
  }

  @Value
  public static class StringValue implements Expression {
    String value;
  }

  public static StringValue stringValue(String value) {
    return new StringValue(value);
  }

  @Value
  public static class Variable implements Expression {
    String name;

    public String getVariableName() {
      return name.substring(1);
    }
  }

  @Value
  public static class FieldDef implements Expression {
    String name;
    FieldType type;
  }

  public static FieldDef fieldDef(String fieldDef) {
    String[] elements = fieldDef.split(":");
    if (elements.length != 2) {
      throw new IllegalArgumentException("Invalid FieldDef: " + fieldDef);
    }
    return new FieldDef(elements[0], FieldType.of(elements[1]));
  }

  @Value
  public static class BinaryOperator implements Expression {
    Expression left;
    Expression right;
    String op;
  }

  public static BinaryOperator binary(Expression left, Expression right, String op) {
    return new BinaryOperator(left, right, op);
  }

  public static Variable variable(String name) {
    return new Variable(name);
  }

  @Value
  public static class Command implements ASTNode {
    String command;
    Expression param;
  }

  public static Command command(String command, Expression param) {
    return new Command(command, param);
  }

  @Value
  public static class Assignment implements ASTNode {
    Variable variable;
    Expression expression;
  }

  public static Assignment assignment(Variable variable, Expression expression) {
    return new Assignment(variable, expression);
  }

  @Value
  public static class MapValue implements Expression { }

  public static MapValue mapValue() {
    return new MapValue();
  }

  @Value
  public static class MapMember implements Expression {
    Variable map;
    String name;
  }

  public static MapMember mapMember(Variable map, String name) {
    return new MapMember(map, name);
  }

  @Value
  public static class MapMemberAssignment implements ASTNode {
    MapMember mapMember;
    Expression expression;
  }

  public static MapMemberAssignment mapMemberAssignment(MapMember mapMember, Expression expression) {
    return new MapMemberAssignment(mapMember, expression);
  }

  @Value
  public static class If implements ASTNode {
    Expression expression;
    List<ASTNode> statements;
  }

  public static If ifs(Expression expression, List<ASTNode> statements) {
    return new If(expression, statements);
  }

  @Value
  public static class Loop implements ASTNode {
    StringValue path;
    List<ASTNode> statements;
  }

  public static Loop loop(StringValue path, List<ASTNode> statements) {
    return new Loop(path, statements);
  }

  @Value
  public static class Arrays implements Expression {
    List<FieldDef> fieldDefs;
  }

  public static Arrays arrays(List<FieldDef> fieldDefs) {
    return new Arrays(fieldDefs);
  }

  @Value
  public static class Store implements ASTNode {
    List<Variable> variables;
  }

  public static Store store(List<Variable> variables) {
    return new Store(variables);
  }
}
