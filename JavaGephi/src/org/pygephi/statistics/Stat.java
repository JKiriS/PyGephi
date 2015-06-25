package org.pygephi.statistics;

import org.pygephi.core.PyGraph;

public interface Stat {
	public void execute();
	public boolean cancel();
	public void setGraph(PyGraph g);
}
