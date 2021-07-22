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

package net.wrap_trap.truffle_arrow.language.parser.ast;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.wrap_trap.truffle_arrow.language.FieldType;

import java.util.List;

public class AST {

  public interface ASTNode {}
  public interface Expression extends ASTNode {}

  public static abstract class SourceIndexable {

    private int sourceIndex;
    private int sourceLength;

    public SourceIndexable(int sourceIndex, int sourceLength) {
      this.sourceIndex = sourceIndex;
      this.sourceLength = sourceLength;
    }

    public int getSourceIndex() {
      return sourceIndex;
    }

    public int getSourceLength() {
      return sourceLength;
    }
  }

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

  @EqualsAndHashCode
  @ToString
  public static class Command extends SourceIndexable implements ASTNode {
    private String command;
    private Expression param;

    public Command(String command, Expression param, int sourceIndex, int sourceLength) {
      super(sourceIndex, sourceLength);
      this.command = command;
      this.param = param;
    }

    public String getCommand() {
      return command;
    }

    public Expression getParam() {
      return param;
    }
  }

  public static Command command(String command, Expression param, int sourceIndex, int sourceLength) {
    return new Command(command, param, sourceIndex, sourceLength);
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
    Expression member;
  }

  public static MapMember mapMember(Variable map, Expression member) {
    return new MapMember(map, member);
  }

  @Value
  public static class MapMemberAssignment implements ASTNode {
    Variable map;
    Expression member;
    Expression value;
  }

  public static MapMemberAssignment mapMemberAssignment(Variable map, Expression member, Expression value) {
    return new MapMemberAssignment(map, member, value);
  }

  @EqualsAndHashCode
  @ToString
  public static class If extends SourceIndexable implements ASTNode {
    private Expression expression;
    private List<ASTNode> statements;

    public If(Expression expression, List<ASTNode> statements, int sourceIndex, int sourceLength) {
      super(sourceIndex, sourceLength);
      this.expression = expression;
      this.statements = statements;
    }

    public Expression getExpression() {
      return expression;
    }

    public List<ASTNode> getStatements() {
      return statements;
    }
  }

  public static If ifs(Expression expression, List<ASTNode> statements, int sourceIndex, int sourceLength) {
    return new If(expression, statements, sourceIndex, sourceLength);
  }

  @EqualsAndHashCode
  @ToString
  public static class Load extends SourceIndexable implements ASTNode {
    private StringValue path;
    private List<ASTNode> statements;

    public Load(StringValue path, List<ASTNode> statements, int sourceIndex, int sourceLength) {
      super(sourceIndex, sourceLength);
      this.path = path;
      this.statements = statements;
    }

    public StringValue getPath() {
      return path;
    }

    public List<ASTNode> getStatements() {
      return statements;
    }
  }

  public static Load load(StringValue path, List<ASTNode> statements, int sourceIndex, int sourceLength) {
    return new Load(path, statements, sourceIndex, sourceLength);
  }

  @EqualsAndHashCode
  @ToString
  public static class Arrays extends SourceIndexable implements Expression {
    private ArrayValue fieldDefs;

    public Arrays(ArrayValue fieldDefs, int sourceIndex, int sourceLength) {
      super(sourceIndex, sourceLength);
      this.fieldDefs = fieldDefs;
    }

    public ArrayValue getFieldDefs() {
      return fieldDefs;
    }
  }

  public static Arrays arrays(ArrayValue fieldDefs, int sourceIndex, int sourceLength) {
    return new Arrays(fieldDefs, sourceIndex, sourceLength);
  }

  @EqualsAndHashCode
  @ToString
  public static class Get extends SourceIndexable implements Expression {
    private Expression expr;
    private Expression orElse;

    public Get(Expression expr, Expression orElse, int sourceIndex, int sourceLength) {
      super(sourceIndex, sourceLength);
      this.expr = expr;
      this.orElse = orElse;
    }

    public Expression getExpr() {
      return expr;
    }

    public Expression getOrElse() {
      return orElse;
    }
  }

  public static Get get(Expression expr, Expression orElse, int sourceIndex, int sourceLength) {
    return new Get(expr, orElse, sourceIndex, sourceLength);
  }

  @EqualsAndHashCode
  @ToString
  public static class Store extends SourceIndexable implements ASTNode {
    private List<Expression> expression;

    public Store(List<Expression> expression, int sourceIndex, int sourceLength) {
      super(sourceIndex, sourceLength);
      this.expression = expression;
    }

    public List<Expression> getVariables() {
      return expression;
    }
  }

  public static Store store(List<Expression> expression, int sourceIndex, int sourceLength) {
    return new Store(expression, sourceIndex, sourceLength);
  }

  @Value
  public static class ArrayValue implements Expression {
    List<Expression> variables;
  }

  public static ArrayValue arrayValue(List<Expression> variables) {
    return new ArrayValue(variables);
  }
}
