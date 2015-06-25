package org.pygephi.core;

import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;

public class GEdge{
	
	Edge e;
	
	public GEdge(Edge e){
		this.e = e;
	}

	public Attributes getAttributes() {
		// TODO Auto-generated method stub
		return e.getAttributes();
	}

	public EdgeData getEdgeData() {
		// TODO Auto-generated method stub
		return e.getEdgeData();
	}

	public int getId() {
		// TODO Auto-generated method stub
		return e.getId();
	}

	public GNode getSource() {
		// TODO Auto-generated method stub
		return new GNode(e.getSource());
	}

	public GNode getTarget() {
		// TODO Auto-generated method stub
		return new GNode(e.getTarget());
	}

	public float getWeight() {
		// TODO Auto-generated method stub
		return e.getWeight();
	}

	public float getWeight(double arg0, double arg1) {
		// TODO Auto-generated method stub
		return e.getWeight(arg0, arg1);
	}

	public boolean isDirected() {
		// TODO Auto-generated method stub
		return e.isDirected();
	}

	public boolean isSelfLoop() {
		// TODO Auto-generated method stub
		return e.isSelfLoop();
	}

	public void setWeight(float w) {
		// TODO Auto-generated method stub
		e.setWeight(w);
	}
	
}
