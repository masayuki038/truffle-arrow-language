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

package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import net.wrap_trap.truffle_arrow.language.FieldType;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.FieldDef;


public class ExprFieldDef extends ExprBase {

  final String name;
  final FieldType type;

  public ExprFieldDef(String name, FieldType type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return new FieldDef(this.name, this.type);
  }

  @Override
  void executeVoid(VirtualFrame frame) { }

  @Override
  public String toString() {
    return "FieldDef name: " + this.name + " type: " + this.type;
  }
}
