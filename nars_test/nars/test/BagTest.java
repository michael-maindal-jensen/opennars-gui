/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.test;

import nars.storage.DefaultBag;
import org.junit.Test;

/**
 *
 * @author me
 */
public class BagTest {
    
    @Test
    public void testVariableBag() {
        int capacity = 10000;
        int forgetRate = 10;
        
        DefaultBag lowLevels = new DefaultBag(10, capacity, forgetRate);
        DefaultBag normalLevels = new DefaultBag(100, capacity, forgetRate);
        DefaultBag highLevels = new DefaultBag(150, capacity, forgetRate);
        
        //CONTINUE HERE
        
    }
    
}
