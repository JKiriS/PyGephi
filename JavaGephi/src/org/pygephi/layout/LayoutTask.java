package org.pygephi.layout;

import org.pygephi.core.GLongTask;
import org.pygephi.core.PyGraph;

public class LayoutTask implements GLongTask {
	
	GLayout layout;
	PyGraph graph;
	
	public LayoutTask(GLayout layout){
		this.layout = layout;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub
		layout.goAlgo();
		if(graph != null && graph.getFrame() != null && graph.getFrame().isVisible())
		    graph.refresh();
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		layout.endAlgo();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		layout.initAlgo();
	}

	@Override
	public boolean canGo() {
		// TODO Auto-generated method stub
		return layout.canAlgo();
	}

	@Override
	public void setGraph(PyGraph g) {
		// TODO Auto-generated method stub
		this.graph = g;
	}

}
