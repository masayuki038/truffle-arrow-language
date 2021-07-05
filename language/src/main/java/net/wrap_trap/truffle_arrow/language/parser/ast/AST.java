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

  @Value
  public static class If implements ASTNode {
    Expression expression;
    List<ASTNode> statements;
  }

  public static If ifs(Expression expression, List<ASTNode> statements) {
    return new If(expression, statements);
  }

  @Value
  public static class Load implements ASTNode {
    StringValue path;
    List<ASTNode> statements;
  }

  public static Load load(StringValue path, List<ASTNode> statements) {
    return new Load(path, statements);
  }

  @Value
  public static class Arrays implements Expression {
    List<FieldDef> fieldDefs;
  }

  public static Arrays arrays(List<FieldDef> fieldDefs) {
    return new Arrays(fieldDefs);
  }

  @Value
  public static class Get implements Expression {
    Expression expr;
    Expression orElse;
  }

  public static Get get(Expression expr, Expression orElse) {
    return new Get(expr, orElse);
  }

  @Value
  public static class Store implements ASTNode {
    List<Variable> variables;
  }

  public static Store store(List<Variable> variables) {
    return new Store(variables);
  }
}
