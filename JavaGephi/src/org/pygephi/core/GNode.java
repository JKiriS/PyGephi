package org.pygephi.core;

import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.TextData;
import org.gephi.graph.spi.LayoutData;
import org.openide.util.Lookup;

public class GNode{
	
	Node n;
	
	public GNode(Node n){
		this.n = n;
	}
	
	public void setValue(String key, Object value){
		n.getAttributes().setValue(key, value);
	}
	
	public Object getValue(String key){
		return n.getAttributes().getValue(key);
	}

	public float alpha() {
		// TODO Auto-generated method stub
		return n.getNodeData().alpha();
	}

	public float b() {
		// TODO Auto-generated method stub
		return n.getNodeData().b();
	}

	public float g() {
		// TODO Auto-generated method stub
		return n.getNodeData().g();
	}

	public Model getModel() {
		// TODO Auto-generated method stub
		return n.getNodeData().getModel();
	}

	public float getRadius() {
		// TODO Auto-generated method stub
		return n.getNodeData().getRadius();
	}

	public float getSize() {
		// TODO Auto-generated method stub
		return n.getNodeData().getSize();
	}

	public TextData getTextData() {
		// TODO Auto-generated method stub
		return n.getNodeData().getTextData();
	}

	public float r() {
		// TODO Auto-generated method stub
		return n.getNodeData().r();
	}

	public void setAlpha(float alpha) {
		// TODO Auto-generated method stub
		n.getNodeData().setAlpha(alpha);
	}

	public void setB(float b) {
		// TODO Auto-generated method stub
		n.getNodeData().setB(b);
	}

	public void setColor(float r, float g, float b) {
		// TODO Auto-generated method stub
		n.getNodeData().setColor(r, g, b);
	}

	public void setG(float g) {
		// TODO Auto-generated method stub
		n.getNodeData().setG(g);
	}

	public void setModel(Model model) {
		// TODO Auto-generated method stub
		n.getNodeData().setModel(model);
	}

	public void setR(float r) {
		// TODO Auto-generated method stub
		n.getNodeData().setR(r);
	}

	public void setSize(float s) {
		// TODO Auto-generated method stub
		n.getNodeData().setSize(s);
	}

	public void setX(float x) {
		// TODO Auto-generated method stub
		n.getNodeData().setX(x);
	}

	public void setY(float y) {
		// TODO Auto-generated method stub
		n.getNodeData().setY(y);
	}

	public void setZ(float z) {
		// TODO Auto-generated method stub
		n.getNodeData().setZ(z);
	}

	public float x() {
		// TODO Auto-generated method stub
		return n.getNodeData().x();
	}

	public float y() {
		// TODO Auto-generated method stub
		return n.getNodeData().y();
	}

	public float z() {
		// TODO Auto-generated method stub
		return n.getNodeData().z();
	}

	public Attributes getAttributes() {
		// TODO Auto-generated method stub
		return n.getNodeData().getAttributes();
	}

	public String getId() {
		// TODO Auto-generated method stub
		return n.getNodeData().getId();
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return n.getNodeData().getLabel();
	}

	public <T extends LayoutData> T getLayoutData() {
		// TODO Auto-generated method stub
		return n.getNodeData().getLayoutData();
	}

	public Node getNode(int i) {
		// TODO Auto-generated method stub
		return n.getNodeData().getNode(i);
	}
	
	public Node getNode(){
		return n;
	}
	
	public NodeData getNodeData(){
		return n.getNodeData();
	}

	public Node getRootNode() {
		// TODO Auto-generated method stub
		return n.getNodeData().getRootNode();
	}

	public boolean isFixed() {
		// TODO Auto-generated method stub
		return n.getNodeData().isFixed();
	}

	public void setFixed(boolean fix) {
		// TODO Auto-generated method stub
		n.getNodeData().setFixed(fix);
	}

	public void setLabel(String label) {
		// TODO Auto-generated method stub
		n.getNodeData().setLabel(label);
	}

	public void setLayoutData(LayoutData ld) {
		// TODO Auto-generated method stub
		n.getNodeData().setLayoutData(ld);
	}
	
}