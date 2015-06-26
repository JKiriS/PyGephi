package org.pygephi.layout;

import org.pygephi.core.GNode;
import org.pygephi.core.PyGraph;

public class SLayout implements GLayout{
	
    private double scale;
    private PyGraph graph;
    private boolean converged;
	
	public SLayout(float s){
		this.scale = s;
	}

    public void initAlgo() {
        setConverged(false);
    }

    public void goAlgo() {
        double xMean = 0, yMean = 0;
        GNode[] nodes = graph.getNodes();
        for (GNode n : nodes) {
            xMean += n.x();
            yMean += n.y();
        }
        xMean /= graph.getNodeCount();
        yMean /= graph.getNodeCount();

        for (GNode n : nodes) {
            double dx = (n.x() - xMean) * getScale();
            double dy = (n.y() - yMean) * getScale();

            n.setX((float) (xMean + dx));
            n.setY((float) (yMean + dy));
        }
        setConverged(true);
    }

    public void endAlgo() {
    	System.out.println("11111");
    }

    public void resetPropertiesValues() {
    }

    /**
     * @return the scale
     */
    public Double getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }

	@Override
	public boolean canAlgo() {
		// TODO Auto-generated method stub
		return !this.converged;
	}

	@Override
	public void setGraph(PyGraph g) {
		// TODO Auto-generated method stub
		this.graph = g;
	}
	
	public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public boolean isConverged() {
        return converged;
    }

	@Override
	public PyGraph getGraph() {
		// TODO Auto-generated method stub
		return this.graph;
	}
}
