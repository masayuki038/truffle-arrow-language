package net.wrap_trap.truffle_arrow.language.parser;

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
    "<", ">", "+", "-", "(", ")", ";", "=", ",", "{", "}", "==", ".", "&&", "||", "like"};
  static String[] keywords = {"echo", "if", "loop", "yield", "return"};

  static Parser<Void> ignored = Scanners.WHITESPACES.optional();
  static Terminals terms = Terminals.operators(operators).words(Scanners.IDENTIFIER).keywords(keywords).build();

  static Pattern varToken = isChar('$').next(Patterns.or(isChar(CharPredicates.IS_ALPHA), isChar('_')).many1());
  static Parser<String> varParser = varToken.toScanner("variable").source();

  static Pattern fieldDefToken = isChar(CharPredicates.IS_ALPHA_).next(isChar(CharPredicates.IS_ALPHA_NUMERIC_).many()).next(isChar(':')).next(isChar(CharPredicates.IS_ALPHA_).many1());
  static Parser<String> fieldDefParser = fieldDefToken.toScanner("fieldDef").source();

  enum Tag {
    VARIABLE,
    FIELDDEF
  }

  public static final Parser<Tokens.Fragment> VAR_TOKENIZER =
    varParser.map(text -> Tokens.fragment(text, Tag.VARIABLE));

  public static final Parser<Tokens.Fragment> FIELDDEF_TOKENIZER =
    fieldDefParser.map(text -> Tokens.fragment(text, Tag.FIELDDEF));

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

  static Parser<?> tokenizer = Parsers.or(
    FIELDDEF_TOKENIZER,
    terms.tokenizer(),
    Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
    VAR_TOKENIZER,
    Terminals.IntegerLiteral.TOKENIZER,
    Terminals.Identifier.TOKENIZER
    );

  public static Parser<AST.IntValue> integer() {
    return Terminals.IntegerLiteral.PARSER.map(s -> AST.intValue(Double.parseDouble(s)));
  }

  public static Parser<AST.StringValue> string() {
    return Terminals.StringLiteral.PARSER.map(AST::stringValue);
  }

  public static Parser<AST.Variable> variable() {
    return VAR_PARSER.map(AST::variable);
  }

  public static Parser<AST.FieldDef> fieldDef() { return FIELDDEF_PARSER.map(AST::fieldDef); }

  public static Parser<AST.Expression> value() {
    return Parsers.or(mapMember(), newMap(),  variable(), integer(), string(),
      terms.token("(").next(pr -> expression().followedBy(terms.token(")"))));
  }

  public static Parser<AST.Expression> operator() {
    return new OperatorTable<AST.Expression>()
             .infixl(terms.token(".").retn((l, r) -> AST.binary(l, r, ".")), 10)
             .infixl(terms.token("+").retn((l, r) -> AST.binary(l, r, "+")), 10)
             .infixl(terms.token("-").retn((l, r) -> AST.binary(l, r, "-")), 10)
             .build(value());
  }

  public static Parser<AST.Expression> bicond() {
    return operator().next(l ->
                             terms.token("==", "<", ">", "&&", "||", "like").source()
                               .next(op -> operator().map(r -> (AST.Expression) AST.binary(l, r, op.trim()))).optional(l));
  }

  public static Parser<AST.Expression> expression() {
    return bicond();
  }

  public static Parser<AST.Command> command() {
    return terms.token("echo").or(terms.token("return"))
             .next(t -> expression().map(exp -> AST.command(t.toString(), exp)));
  }

  public static Parser<AST.Assignment> assignment() {
    return variable().followedBy(terms.token("="))
             .next(v -> expression().map(exp -> AST.assignment(v, exp)));
  }

  public static Parser<String> identifier() {
    return Terminals.Identifier.PARSER;
  }

  public static Parser<AST.If> ifStatement() {
    return terms.token("if").next(t -> expression()
                                         .between(terms.token("("), terms.token(")"))
                                         .next(exp -> statements()
                                                        .map(statements -> AST.ifs(exp, statements))));
  }

  public static Parser<AST.Loop> loop() {
    return terms.token("loop").next(t -> string()
                                           .between(terms.token("("), terms.token(")"))
                                           .next(path -> statements()
                                              .next(statements -> terms.token("yield")
                                                .next(yield -> fieldDef().sepBy(terms.token(",")).between(terms.token("("), terms.token(")"))
                                                  .map(fields -> AST.loop(path, statements, fields))))));
  }

  public static Parser<AST.ASTNode> statement() {
    return
      Parsers.or(
        Parsers.or(mapMemberAssignment(), assignment(), bicond(), command())
          .followedBy(terms.token(";")),
        ifStatement(), loop());
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
    return variable().followedBy(terms.token("."))
             .next(v -> identifier().map(i -> AST.mapMember(v, i)));
  }

  public static Parser<AST.MapMemberAssignment> mapMemberAssignment() {
    return mapMember().followedBy(terms.token("="))
             .next(member -> expression().map(exp -> AST.mapMemberAssignment(member, exp)));

  }

  public static Parser<List<AST.ASTNode>> script() {
    return statement().many();
  }

  public static Parser<List<AST.ASTNode>> createParser() {
    return script().from(tokenizer, ignored);
  }
}
