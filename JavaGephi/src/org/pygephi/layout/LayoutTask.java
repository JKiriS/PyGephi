package org.pygephi.layout;

import org.pygephi.core.GLongTask;

public class LayoutTask implements GLongTask {
	
	GLayout layout;
	
	public LayoutTask(GLayout layout){
		this.layout = layout;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub
		layout.goAlgo();
		if(layout.getGraph().getFrame() != null && layout.getGraph().getFrame().isVisible())
		    layout.getGraph().refresh();
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

}
