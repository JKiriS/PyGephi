package org.pygephi.layout;

import org.pygephi.core.PyGraph;

public interface GLayout{

	public boolean canAlgo();

	public void endAlgo();

	public void goAlgo();

	public void initAlgo();

	public void resetPropertiesValues();
	
	public void setGraph(PyGraph g);
	
}
