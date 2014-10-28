/*
 * Believe.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.operator.mental;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * Operator that creates a judgment with a given statement
 */
public class Anticipate extends Operator {

    public Anticipate() {
        super("^anticipate");
    }

    /**
     * To create a judgment with a given statement
     * @param args Arguments, a Statement followed by an optional tense
     * @param memory The memory in which the operation is executed
+    * @return Immediate results as Tasks
     */
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        
        Term content = args[0];
        memory.executive.anticipateTime=memory.time() + memory.param.duration.get();
        memory.executive.anticipateTerm=content;
        
        return null;
    }
}