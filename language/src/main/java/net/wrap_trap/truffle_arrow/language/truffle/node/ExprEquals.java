package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.apache.arrow.vector.util.Text;

import java.util.Objects;


@NodeInfo(shortName = "==")
abstract public class ExprEquals extends ExprBinary {

  @Specialization
  protected boolean eq(boolean left, boolean right) {
    return left == right;
  }

  @Specialization
  protected boolean eq(int left, int right) {
    return left == right;
  }

  @Specialization
  protected boolean eq(long left, long right) {
    return left == right;
  }

  @Specialization
  protected boolean eq(double left, double right) {
    return left == right;
  }

  @Specialization
  protected boolean ge(Object left, SqlNull right) {
    return (left == SqlNull.INSTANCE);
  }

  @Specialization
  protected boolean ge(SqlNull left, Object right) {
    return (right == SqlNull.INSTANCE);
  }

  @Specialization
  protected boolean eq(Text left, Text right) {
    return Objects.equals(left.toString(), right.toString());
  }

  @Specialization
  protected boolean eq(Text left, String right) {
    return Objects.equals(left.toString(), right);
  }

  @Specialization
  protected boolean eq(String left, Text right) {
    return Objects.equals(left, right.toString());
  }

  @Specialization
  protected boolean eq(String left, String right) {
    return Objects.equals(left, right);
  }

  @Specialization
  protected boolean eq(Text left, Object right) {
    return eq(left.toString(), right);
  }

  @Specialization
  protected boolean eq(Object left, Text right) {
    return eq(left, right.toString());
  }

  @Specialization
  @CompilerDirectives.TruffleBoundary
  protected boolean eq(Object left, Object right) {
    return ((Comparable) left).compareTo(right) == 0;
  }
}
