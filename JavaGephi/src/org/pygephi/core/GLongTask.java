package org.pygephi.core;

public interface GLongTask {

	public void end();

	public void go();

	public void init();
	
	public boolean canGo();

	public void setGraph(PyGraph g);
	
}
