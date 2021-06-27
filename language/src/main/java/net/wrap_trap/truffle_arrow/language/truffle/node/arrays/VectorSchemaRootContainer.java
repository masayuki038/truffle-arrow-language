package net.wrap_trap.truffle_arrow.language.truffle.node.arrays;

import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;

import java.util.List;


/**
 * A container for VectorSchemaRoot
 */
public interface VectorSchemaRootContainer {

  void addValues(List<Object> values);

  List<VectorSchemaRoot> getVectorSchemaRoots();

  void setRowCounts();
}
