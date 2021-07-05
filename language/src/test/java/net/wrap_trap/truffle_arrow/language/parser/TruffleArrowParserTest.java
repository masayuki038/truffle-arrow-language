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

package net.wrap_trap.truffle_arrow.language.parser;

import org.jparsec.Parser;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static net.wrap_trap.truffle_arrow.language.parser.ast.AST.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class TruffleArrowParserTest {

  private final static String SAMPLE =
      "echo \"a\";\n" +
      "echo \"b\";\n" +
      "$a = 1;\n" +
      "if ($a == 0) echo $a;\n" +
      "$b = {};\n" +
      "$b[\"foo\"] = 1;\n" +
      "$b[\"bar\"] = \"hoge\";\n" +
      "echo $b[\"bar\"];\n" +
      "if (($a == 0) && ($b == 1)) echo $a;\n" +
      "if ((($a == 0) || ($b == 1)) && ($c == 2)) echo $a;\n" +
      "if (($a like \"%a%\") || ($b like \"_b_\")) echo $a;\n";

  @Test
  public void testIntValue() {
    Parser<IntValue> parser = TruffleArrowParser.integer().from(TruffleArrowParser.tokenizer, TruffleArrowParser.ignored);
    assertThat(parser.parse("123"), is(intValue(123)));
  }

  @Test
  public void testIdentifier() {
    Parser<String> parser = TruffleArrowParser.identifier().from(TruffleArrowParser.tokenizer, TruffleArrowParser.ignored);
    assertThat(parser.parse("abc"), is("abc"));
  }

  @Test
  public void testVariable() {
    Parser<Variable> parser = TruffleArrowParser.variable().from(TruffleArrowParser.tokenizer, TruffleArrowParser.ignored);
    assertThat(parser.parse("$hoge"), is(variable("$hoge")));
  }

  @Test
  public void testValue() {
    Parser<Expression> parser = TruffleArrowParser.value().from(TruffleArrowParser.tokenizer, TruffleArrowParser.ignored);
    assertThat(parser.parse("$hoge"), is(variable("$hoge")));
    assertThat(parser.parse("$F_INT"), is(variable("$F_INT")));
    assertThat(parser.parse("123"), is(intValue(123)));
    assertThat(parser.parse("0"), is(intValue(0)));
    assertThat(parser.parse("123.45"), is(doubleValue(123.45)));
  }

  @Test
  public void testBicond() {
    Parser<Expression> parser = TruffleArrowParser.bicond().from(TruffleArrowParser.tokenizer, TruffleArrowParser.ignored);
    assertThat(parser.parse("2 > 1"), is(binary(intValue(2), intValue(1), ">")));
    assertThat(parser.parse("$a == 1"), is(binary(variable("$a"), intValue(1), "==")));
    assertThat(parser.parse("$a <> 1"), is(binary(variable("$a"), intValue(1), "<>")));
    assertThat(parser.parse("$a >= 1"), is(binary(variable("$a"), intValue(1), ">=")));
    assertThat(parser.parse("$a <= 1"), is(binary(variable("$a"), intValue(1), "<=")));
    assertThat(parser.parse("\"a\" < $a"), is(binary(stringValue("a"), variable("$a"), "<")));
    assertThat(parser.parse("$a && $b"), is(binary(variable("$a"), variable("$b"), "&&")));
    assertThat(parser.parse("$a || $b"), is(binary(variable("$a"), variable("$b"), "||")));
    assertThat(parser.parse("($a && $b) || $c"), is(binary(binary(variable("$a"), variable("$b"), "&&"), variable("$c"), "||")));
    assertThat(parser.parse("$a like \"%a%\""), is(binary(variable("$a"), stringValue("%a%"), "like")));
    assertThat(parser.parse("$b like \"_b_\""), is(binary(variable("$b"), stringValue("_b_"), "like")));
  }

  @Test
  public void testOperator() {
    Parser<Expression> parser = TruffleArrowParser.operator().from(TruffleArrowParser.tokenizer, TruffleArrowParser.ignored);
    assertThat(parser.parse("12+3"), is(binary(intValue(12), intValue(3), "+")));
    assertThat(parser.parse("12+$a"), is(binary(intValue(12), variable("$a"), "+")));
    assertThat(parser.parse("$ab+123"), is(binary(variable("$ab"), intValue(123), "+")));
    assertThat(parser.parse("$a+$b"), is(binary(variable("$a"), variable("$b"), "+")));
    assertThat(parser.parse("$a * $b"), is(binary(variable("$a"), variable("$b"), "*")));
    assertThat(parser.parse("$a / $b"), is(binary(variable("$a"), variable("$b"), "/")));
  }

  @Test
  public void testCommand() {
    Parser<Command> parser = parser(TruffleArrowParser.command());
    assertThat(parser.parse("echo 123"), is(command("echo", intValue(123))));
    assertThat(parser.parse("echo 123<3"), is(command("echo", binary(intValue(123), intValue(3), "<"))));
    assertThat(parser.parse("echo 23+3"), is(command("echo", binary(intValue(23), intValue(3), "+"))));
    assertThat(parser.parse("echo $a"), is(command("echo", variable("$a"))));
    assertThat(parser.parse("echo \"a\""), is(command("echo", stringValue("a"))));
  }

  @Test
  public void testAssignment() {
    Parser<Assignment> parser = parser(TruffleArrowParser.assignment());
    assertThat(parser.parse("$a=123"), is(assignment(variable("$a"), intValue(123))));
  }

  @Test
  public void testStatement() {
    Parser<ASTNode> parser = parser(TruffleArrowParser.statement());
    assertThat(parser.parse("$a;"), is(variable("$a")));
    assertThat(parser.parse("$a=$a+1;"), is(assignment(variable("$a"), binary(variable("$a"), intValue(1), "+"))));
    assertThat(parser.parse("echo \"aaa\";"), is(command("echo", stringValue("aaa"))));
    assertThat(parser.parse("$a[\"a1\"]=\"hoge\";"), is(mapMemberAssignment(variable("$a"), stringValue("a1"), stringValue("hoge"))));
  }

  @Test
  public void testIfs() {
    Parser<If> parser = parser(TruffleArrowParser.ifStatement());
    assertThat(
      parser.parse("if ($a < 3) echo $a;"), is(ifs(binary(variable("$a"), intValue(3), "<")
        , Arrays.asList(command("echo", variable("$a"))))));
  }

  @Test
  public void testIfsWithBlocks() {
    String ifs = "if ($a < 3) { \n"+
                 "  echo $a;\n" +
                 "  echo \"$a < 3\";\n" +
                 "}\n";

    Parser<If> parser = parser(TruffleArrowParser.ifStatement());
    assertThat(
      parser.parse(ifs), is(ifs(binary(variable("$a"), intValue(3), "<")
        , Arrays.asList(command("echo", variable("$a"))
          , command("echo", stringValue("$a < 3"))))));
  }

  @Test
  public void testLoadWithBlocks() {
    String loop = "load (\"/path/to/dir\") { \n"+
                   "  echo $a;\n" +
                   "  echo \"$a < 3\";\n" +
                   "}";

    Parser<Load> parser = parser(TruffleArrowParser.load());
    assertThat(
      parser.parse(loop), is(load(stringValue("/path/to/dir"),
        Arrays.asList(
          command("echo", variable("$a"))
          , command("echo", stringValue("$a < 3"))))));
  }

  @Test
  public void testGet() {
    Parser<Get> parser = parser(new TruffleArrowParser().get());
    assertThat(parser.parse("get(1, 0)"), is(get(intValue(1), intValue(0))));
    assertThat(parser.parse("get($map[$foo], 0)"), is(get(mapMember(variable("$map"), variable("$foo")), intValue(0))));
    assertThat(parser.parse("get($map[\"foo\"], 1)"), is(get(mapMember(variable("$map"), stringValue("foo")), intValue(1))));
  }

  @Test
  public void testNewMap() {
    Parser<Expression> parser = parser(new TruffleArrowParser().newMap());
    assertThat(parser.parse("{}"), is(mapValue()));
    assertThat(parser.parse("{ }"), is(mapValue()));
  }

  @Test
  public void testMapMember() {
    Parser<MapMember> parser = parser(new TruffleArrowParser().mapMember());
    assertThat(parser.parse("$a[\"foo\"]"), is(mapMember(variable("$a"), stringValue("foo"))));
    assertThat(parser.parse("$a[1]"), is(mapMember(variable("$a"), intValue(1))));
    assertThat(parser.parse("$a[1.1]"), is(mapMember(variable("$a"), doubleValue(1.1))));
    assertThat(parser.parse("$a[$b]"), is(mapMember(variable("$a"), variable("$b"))));
  }

  @Test
  public void testMapMemberAssignment() {
    Parser<MapMemberAssignment> parser = parser(new TruffleArrowParser().mapMemberAssignment());
    assertThat(parser.parse("$a[\"foo\"] = 1"), is(mapMemberAssignment(variable("$a"), stringValue("foo"), intValue(1))));
    assertThat(parser.parse("$a[1]=\"hoge\""), is(mapMemberAssignment(variable("$a"), intValue(1), stringValue("hoge"))));
    assertThat(parser.parse("$a[1.1]=2.2"), is(mapMemberAssignment(variable("$a"), doubleValue(1.1), doubleValue(2.2))));
    assertThat(parser.parse("$a[$b]=$c"), is(mapMemberAssignment(variable("$a"), variable("$b"), variable("$c"))));
  }

  @Test
  public void testScript() {
    Parser<List<ASTNode>> parser = parser(TruffleArrowParser.script());
    List<ASTNode> asts = parser.parse(SAMPLE);
    assertThat(asts.size(), is(11));
  }

  <T> Parser<T> parser(Parser<T> p) {
    return p.from(TruffleArrowParser.tokenizer, TruffleArrowParser.ignored);
  }
}
