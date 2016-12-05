/*
 * Copyright (C) 2014 sue
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
 * along with this program.  If not.getCharts(see <http://www.gnu.org/licenses/>.
 */
package nars.lab.timeline;

import automenta.vivisect.TreeMLData.FirstOrderDifferenceTimeSeries;
import nars.NAR;
import nars.config.Plugins;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.TimelineVis;
import automenta.vivisect.timeline.BarChart;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.StackedPercentageChart;
import nars.gui.EventChart;
import nars.gui.util.NARTrace;

/**
 *
 */
public class MultiChart1 extends TimelineExample {
    
    public static void main(String[] args) {
        int cycles = 500;
        
        NAR nar = new NAR();
        NARTrace t = new NARTrace(nar);
        nar.addInput("<a --> b>.");
        nar.addInput("<b --> c>.");
        nar.addInput("<(^pick,x) =\\> a>.");
        nar.addInput("<(*, b, c) <-> x>.");
        nar.addInput("a!");
        nar.run(cycles);

        new NWindow("_", 
            new PCanvas(
                new TimelineVis(
                    new EventChart(t, true, false, false).height(3),
                    new BarChart(new FirstOrderDifferenceTimeSeries("d(concepts)", t.charts.get("concept.count"))),

                    new StackedPercentageChart(t.getCharts("concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3")).height(2),
                    new LineChart(t.getCharts("concept.priority.mean")).height(1),

                    new EventChart(t, false, true, false).height(3),

                    new LineChart(t.getCharts("task.novel.add", "task.immediate_processed")).height(3),
                    new LineChart(t.getCharts("task.goal.process", "task.question.process", "task.judgment.process")).height(3),
                    new LineChart(t.getCharts("emotion.busy")).height(1),
                    new EventChart(t, false, false, true).height(3)
        ))).show(800, 800, true);
    }
    
}
