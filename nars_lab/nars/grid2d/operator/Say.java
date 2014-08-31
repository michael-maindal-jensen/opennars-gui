/*
 * Sample.java
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
package nars.grid2d.operator;
 
import java.util.List;
import nars.entity.Task;
import nars.grid2d.Grid2DSpace;
import nars.grid2d.TestChamber;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.core.Memory;

/**
 *  A class used as a template for Operator definition.
 * TODO: memory.registerOperator(new Goto("^goto"));
 */
public class Say extends Operator {

    TestChamber chamb;
    public Say(TestChamber chamb, String name) {
        super(name);
        this.chamb=chamb;
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
        //Operation content = (Operation) task.getContent();
        //Operator op = content.getOperator();
         
        
        System.out.println("Executed: " + this);
        for (Term t : args) {
            System.out.println(" --- " + t);
        }
        
        
       // if(nars.grid2d.Grid2DSpace.world_used) {
            //ok lets start pathfinding tool
            //nars.grid2d.Grid2DSpace.pathFindAndGoto(arg);
       // }
        
       
        
        return null;
    }

}

