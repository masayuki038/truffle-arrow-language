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

import net.wrap_trap.truffle_arrow.language.FieldType;
import net.wrap_trap.truffle_arrow.language.parser.ast.AST;
import org.jparsec.*;
import org.jparsec.pattern.CharPredicates;
import org.jparsec.pattern.Pattern;
import org.jparsec.pattern.Patterns;

import java.util.Arrays;
import java.util.List;

import static org.jparsec.pattern.Patterns.isChar;


public class TruffleArrowParser {
  static String[] operators = {
    "<", ">", "+", "-", "*", "/", "(", ")", ";", "=", ",", "{", "}", "[", "]", "==", "<>", "<=", ">=", ".", "&&", "||", "like"};
  static String[] keywords = {"echo", "if", "load", "arrays", "store", "get", "return"};

  static Parser<Void> ignored = Scanners.WHITESPACES.optional();
  static Terminals terms = Terminals.operators(operators).words(Scanners.IDENTIFIER).keywords(keywords).build();

  static Pattern varToken = isChar('$').next(Patterns.or(isChar(CharPredicates.IS_ALPHA), isChar('_')).many1());
  static Parser<String> varParser = varToken.toScanner("variable").source();

  static Pattern fieldDefToken =
    isChar(CharPredicates.IS_ALPHA_)
      .next(isChar(CharPredicates.IS_ALPHA_NUMERIC_).many())
      .next(isChar(':'))
      .next(Patterns.or(
        Patterns.string(FieldType.INT.toString()),
        Patterns.string(FieldType.BIGINT.toString()),
        Patterns.string(FieldType.DOUBLE.toString()),
        Patterns.string(FieldType.STRING.toString())));
  static Parser<String> fieldDefParser = fieldDefToken.toScanner("fieldDef").source();

  static Pattern decimalToken = Patterns.INTEGER.optional().next(Patterns.FRACTION);
  static final Parser<String> decimalParser = decimalToken.toScanner("decimal point number").source();

  enum Tag {
    VARIABLE,
    FIELDDEF,
    DECIMAL
  }

  public static final Parser<Tokens.Fragment> VAR_TOKENIZER =
    varParser.map(text -> Tokens.fragment(text, Tag.VARIABLE));

  public static final Parser<Tokens.Fragment> FIELDDEF_TOKENIZER =
    fieldDefParser.map(text -> Tokens.fragment(text, Tag.FIELDDEF));

  public static final Parser<Tokens.Fragment> DECIMAL_TOKENIZER =
    decimalParser.map(text -> Tokens.fragment(text, Tag.DECIMAL));

  private static final Parser<String> createParser(Tag tag) {
    return Parsers.token(t -> {
      Object val = t.value();
      if (val instanceof Tokens.Fragment) {
        Tokens.Fragment c = (Tokens.Fragment) val;
        return tag.equals(c.tag()) ? c.text() : null;
      }
      return null;
    });
  }
  public static final Parser<String> VAR_PARSER = createParser(Tag.VARIABLE);
  public static final Parser<String> FIELDDEF_PARSER = createParser(Tag.FIELDDEF);
  public static final Parser<String> DECIMAL_PARSER = createParser(Tag.DECIMAL);

  static Parser<?> tokenizer = Parsers.or(
    FIELDDEF_TOKENIZER,
    terms.tokenizer(),
    Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
    DECIMAL_TOKENIZER,
    Terminals.IntegerLiteral.TOKENIZER,
    VAR_TOKENIZER,
    Terminals.Identifier.TOKENIZER
    );

  public static Parser<AST.IntValue> integer() {
    return Terminals.IntegerLiteral.PARSER.map(s -> AST.intValue(Integer.parseInt(s)));
  }

  public static Parser<AST.DoubleValue> double_() {
    return DECIMAL_PARSER.map(s -> AST.doubleValue(Double.parseDouble(s)));
  }

  public static Parser<AST.StringValue> string() {
    return Terminals.StringLiteral.PARSER.map(AST::stringValue);
  }

  public static Parser<AST.Variable> variable() {
    return VAR_PARSER.map(AST::variable);
  }

  public static Parser<AST.FieldDef> fieldDef() { return FIELDDEF_PARSER.map(AST::fieldDef); }

  public static Parser<AST.Expression> value() {
    return Parsers.or(get(), arrays(), mapMember(), newMap(), variable(), integer(), double_(), string(), newArray(),
      terms.token("(").next(pr -> expression().followedBy(terms.token(")"))));
  }

  public static Parser<AST.Expression> operator() {
    return new OperatorTable<AST.Expression>()
             .infixl(terms.token(".").retn((l, r) -> AST.binary(l, r, ".")), 10)
             .infixl(terms.token("+").retn((l, r) -> AST.binary(l, r, "+")), 10)
             .infixl(terms.token("-").retn((l, r) -> AST.binary(l, r, "-")), 10)
             .infixl(terms.token("*").retn((l, r) -> AST.binary(l, r, "*")), 10)
             .infixl(terms.token("/").retn((l, r) -> AST.binary(l, r, "/")), 10)
             .build(value());
  }

  public static Parser<AST.Expression> bicond() {
    return operator().next(l ->
                             terms.token("==", "<>", "<", ">", "<=", ">=", "&&", "||", "like").source()
                               .next(op -> operator().map(r -> (AST.Expression) AST.binary(l, r, op.trim()))).optional(l));
  }

  public static Parser<AST.Expression> expression() {
    return bicond();
  }

  public static Parser<AST.Command> command() {
    return terms.token("echo").or(terms.token("return"))
             .next(t -> expression().map(exp -> AST.command(t.toString(), exp, t.index(), t.length())));
  }

  public static Parser<AST.Assignment> assignment() {
    return variable().followedBy(terms.token("="))
             .next(v -> expression().map(exp -> AST.assignment(v, exp)));
  }

  public static Parser<AST.Arrays> arrays() {
    return terms.token("arrays")
      .next(a -> fieldDef().sepBy(terms.token(",")).between(terms.token("("), terms.token(")"))
        .map(fields -> AST.arrays(fields, a.index(), a.length())));
  }

  public static Parser<AST.Get> get() {
    return terms.token("get")
            .next(g -> terms.token("(")
              .next(p1 -> expression()
                .next(v -> terms.token(",")
                  .next(c -> expression()
                    .next(orElse -> terms.token(")")
                      .map(p2 -> AST.get(v, orElse, g.index(), g.length())))))));
  }

  public static Parser<AST.Store> store() {
    return terms.token("store")
      .next(s -> expression().sepBy(terms.token(",")).between(terms.token("("), terms.token(")"))
        .map(expression -> AST.store(expression, s.index(), s.length())));
  }

  public static Parser<String> identifier() {
    return Terminals.Identifier.PARSER;
  }

  public static Parser<AST.If> ifStatement() {
    return terms.token("if").next(t -> expression()
                                         .between(terms.token("("), terms.token(")"))
                                         .next(exp -> statements()
                                                        .map(statements -> AST.ifs(exp, statements, t.index(), t.length()))));
  }

  public static Parser<AST.Load> load() {
    return terms.token("load").next(t -> string()
                                           .between(terms.token("("), terms.token(")"))
                                           .next(s -> statements()
                                                        .map(statements -> AST.load(s, statements, t.index(), t.length()))));
  }

  public static Parser<AST.ASTNode> statement() {
    return
      Parsers.or(
        Parsers.or(store(), mapMemberAssignment(), assignment(), bicond(), command())
          .followedBy(terms.token(";")),
        ifStatement(), load());
  }

  public static Parser<List<AST.ASTNode>> statements() {
    return Parsers.or(
      statement().map(s -> Arrays.asList(s)),
      statement().many().between(terms.token("{"), terms.token("}")));
  }

  public static Parser<AST.Expression> newMap() {
    return Parsers.sequence(terms.token("{"), terms.token("}"))
             .map(s -> AST.mapValue());
  }

  public static Parser<AST.MapMember> mapMember() {
    return variable()
              .next(v -> expression()
                .between(terms.token("["), terms.token("]"))
                .map(e -> AST.mapMember(v, e)));
  }

  public static Parser<AST.MapMemberAssignment> mapMemberAssignment() {
    return mapMember().followedBy(terms.token("="))
             .next(member -> expression().map(exp -> AST.mapMemberAssignment(member.getMap(), member.getMember(), exp)));

  }

  public static Parser<AST.ArrayValue> newArray() {
    return variable().sepBy(terms.token(",")).between(terms.token("["), terms.token("]"))
             .map(variable -> AST.arrayValue(variable));
  }

  public static Parser<List<AST.ASTNode>> script() {
    return statement().many();
  }

  public static Parser<List<AST.ASTNode>> createParser() {
    return script().from(tokenizer, ignored);
  }
}
