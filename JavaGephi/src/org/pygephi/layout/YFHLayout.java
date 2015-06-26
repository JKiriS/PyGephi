package org.pygephi.layout;

import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Spatial;
import org.gephi.layout.plugin.ForceVectorUtils;
import org.gephi.layout.plugin.force.AbstractForce;
import org.gephi.layout.plugin.force.Displacement;
import org.gephi.layout.plugin.force.ForceVector;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.quadtree.BarnesHut;
import org.gephi.layout.plugin.force.quadtree.QuadTree;
import org.pygephi.core.GEdge;
import org.pygephi.core.GNode;
import org.pygephi.core.PyGraph;

public class YFHLayout implements GLayout{
	
	public YFHLayout(){
		this.displacement = new StepDisplacement(1f);
		this.resetPropertiesValues();
	}
	
	public YFHLayout(float s){
		this.displacement = new StepDisplacement(s);
		this.resetPropertiesValues();
	}
	
	private float optimalDistance;
    private float relativeStrength;
    private float step;
    private float initialStep;
    private int progress;
    private float stepRatio;
    private int quadTreeMaxLevel;
    private float barnesHutTheta;
    private float convergenceThreshold;
    private boolean adaptiveCooling;
    private Displacement displacement;
    private double energy0;
    private double energy;
    private PyGraph graph;
    private boolean converged;

    protected void postAlgo() {
        updateStep();
        if (Math.abs((energy - energy0) / energy) < getConvergenceThreshold()) {
            setConverged(true);
        }
    }

    private Displacement getDisplacement() {
        displacement.setStep(step);
        return displacement;
    }

    private AbstractForce getEdgeForce() {
        return new SpringForce(getOptimalDistance());
    }

    private AbstractForce getNodeForce() {
        return new ElectricalForce(getRelativeStrength(), getOptimalDistance());
    }

    private void updateStep() {
        if (isAdaptiveCooling()) {
            if (energy < energy0) {
                progress++;
                if (progress >= 5) {
                    progress = 0;
                    setStep(step / getStepRatio());
                }
            } else {
                progress = 0;
                setStep(step * getStepRatio());
            }
        } else {
            setStep(step * getStepRatio());
        }
    }

    @Override
    public void resetPropertiesValues() {
        setStepRatio((float) 0.95);
        setRelativeStrength((float) 0.2);
        if (graph != null) {
            setOptimalDistance((float) (Math.pow(getRelativeStrength(), 1.0 / 3) * getAverageEdgeLength(graph)));
        } else {
            setOptimalDistance(100.0f);
        }

        setInitialStep(optimalDistance / 5);
        setStep(initialStep);
        setQuadTreeMaxLevel(10);
        setBarnesHutTheta(1.2f);
        setAdaptiveCooling(true);
        setConvergenceThreshold(1e-4f);
    }

    public float getAverageEdgeLength(PyGraph graph) {
        float edgeLength = 0;
        int count = 1;
        for (GEdge e : graph.getEdges()) {
            edgeLength += ForceVectorUtils.distance(
                    e.getSource().getNodeData(), e.getTarget().getNodeData());
            count++;
        }

        return edgeLength / count;
    }

    public void initAlgo() {
        if (graph == null) {
            return;
        }
        energy = Float.POSITIVE_INFINITY;
        for (GNode n : graph.getNodes()) {
            n.setLayoutData(new ForceVector());
        }
        progress = 0;
        setConverged(false);
        setStep(initialStep);
    }

    @Override
    public void endAlgo() {
        for (GNode n : graph.getNodes()) {
            n.setLayoutData(null);
        }
    }

    @Override
    public void goAlgo() {
        graph.readLock();
        GNode[] nodes = graph.getNodes();
        for (GNode n : nodes) {
            if (n.getLayoutData() == null || !(n.getLayoutData() instanceof ForceVector)) {
                n.setLayoutData(new ForceVector());
            }
        }

        // Evaluates n^2 inter node forces using BarnesHut.
        QuadTree tree = QuadTree.buildTree(graph.getGraphModel().getHierarchicalGraph(), getQuadTreeMaxLevel());

//        double electricEnergy = 0; ///////////////////////
//        double springEnergy = 0; ///////////////////////
        BarnesHut barnes = new BarnesHut(getNodeForce());
        barnes.setTheta(getBarnesHutTheta());
        for (GNode node : nodes) {
            ForceVector layoutData = node.getLayoutData();

            ForceVector f = barnes.calculateForce(node.getNodeData(), tree);
            layoutData.add(f);
//            electricEnergy += f.getEnergy();
        }

        // Apply edge forces.

        for (GEdge e : graph.getEdges()) {
            if (!e.getSource().equals(e.getTarget())) {
                GNode n1 = e.getSource();
                GNode n2 = e.getTarget();
                ForceVector f1 = n1.getLayoutData();
                ForceVector f2 = n2.getLayoutData();

                ForceVector f = getEdgeForce().calculateForce(n1.getNodeData(), n2.getNodeData());
                f1.add(f);
                f2.subtract(f);
            }
        }

        // Calculate energy and max force.
        energy0 = energy;
        energy = 0;
        double maxForce = 1;
        for (GNode n : nodes) {
            ForceVector force = n.getLayoutData();

            energy += force.getNorm();
            maxForce = Math.max(maxForce, force.getNorm());
        }

        // Apply displacements on nodes.
        for (GNode n : nodes) {
            if (!n.isFixed()) {
                ForceVector force = n.getLayoutData();

                force.multiply((float) (1.0 / maxForce));
                getDisplacement().moveNode(n.getNodeData(), force);
            }
        }
        postAlgo();
//        springEnergy = energy - electricEnergy;
//        System.out.println("electric: " + electricEnergy + "    spring: " + springEnergy);
//        System.out.println("energy0 = " + energy0 + "   energy = " + energy);
        graph.readUnlock();
    }


    /* Maximum level for Barnes-Hut's quadtree */
    public Integer getQuadTreeMaxLevel() {
        return quadTreeMaxLevel;
    }

    public void setQuadTreeMaxLevel(Integer quadTreeMaxLevel) {
        this.quadTreeMaxLevel = quadTreeMaxLevel;
    }

    /* theta is the parameter for Barnes-Hut opening criteria */
    public Float getBarnesHutTheta() {
        return barnesHutTheta;
    }

    public void setBarnesHutTheta(Float barnesHutTheta) {
        this.barnesHutTheta = barnesHutTheta;
    }

    /**
     * @return the optimalDistance
     */
    public Float getOptimalDistance() {
        return optimalDistance;
    }

    /**
     * @param optimalDistance the optimalDistance to set
     */
    public void setOptimalDistance(Float optimalDistance) {
        this.optimalDistance = optimalDistance;
    }

    /**
     * @return the relativeStrength
     */
    public Float getRelativeStrength() {
        return relativeStrength;
    }

    /**
     * @param relativeStrength the relativeStrength to set
     */
    public void setRelativeStrength(Float relativeStrength) {
        this.relativeStrength = relativeStrength;
    }

    /**
     * @param step the step to set
     */
    public void setStep(Float step) {
        this.step = step;
    }

    /**
     * @return the adaptiveCooling
     */
    public Boolean isAdaptiveCooling() {
        return adaptiveCooling;
    }

    /**
     * @param adaptiveCooling the adaptiveCooling to set
     */
    public void setAdaptiveCooling(Boolean adaptiveCooling) {
        this.adaptiveCooling = adaptiveCooling;
    }

    /**
     * @return the stepRatio
     */
    public Float getStepRatio() {
        return stepRatio;
    }

    /**
     * @param stepRatio the stepRatio to set
     */
    public void setStepRatio(Float stepRatio) {
        this.stepRatio = stepRatio;
    }

    /**
     * @return the convergenceThreshold
     */
    public Float getConvergenceThreshold() {
        return convergenceThreshold;
    }

    /**
     * @param convergenceThreshold the convergenceThreshold to set
     */
    public void setConvergenceThreshold(Float convergenceThreshold) {
        this.convergenceThreshold = convergenceThreshold;
    }

    /**
     * @return the initialStep
     */
    public Float getInitialStep() {
        return initialStep;
    }

    /**
     * @param initialStep the initialStep to set
     */
    public void setInitialStep(Float initialStep) {
        this.initialStep = initialStep;
    }

    /**
     * Fa = (n2 - n1) * ||n2 - n1|| / K
     *
     * @author Helder Suzuki <heldersuzuki@gephi.org>
     */
    public class SpringForce extends AbstractForce {

        private float optimalDistance;

        public SpringForce(float optimalDistance) {
            this.optimalDistance = optimalDistance;
        }

        public ForceVector calculateForce(NodeData node1, NodeData node2,
                float distance) {
            ForceVector f = new ForceVector(node2.x() - node1.x(),
                    node2.y() - node1.y());
            f.multiply(distance / optimalDistance);
            return f;
        }

        public void setOptimalDistance(Float optimalDistance) {
            this.optimalDistance = optimalDistance;
        }

        public Float getOptimalDistance() {
            return optimalDistance;
        }

		@Override
		public ForceVector calculateForce(Spatial arg0, Spatial arg1, float arg2) {
			// TODO Auto-generated method stub
			return null;
		}
    }

    /**
     * Fr = -C*K*K*(n2-n1)/||n2-n1||
     *
     * @author Helder Suzuki <heldersuzuki@gephi.org>
     */
    public class ElectricalForce extends AbstractForce {

        private float relativeStrength;
        private float optimalDistance;

        public ElectricalForce(float relativeStrength, float optimalDistance) {
            this.relativeStrength = relativeStrength;
            this.optimalDistance = optimalDistance;
        }

        public ForceVector calculateForce(NodeData node1, NodeData node2,
                float distance) {
            ForceVector f = new ForceVector(node2.x() - node1.x(),
                    node2.y() - node1.y());
            float scale = -relativeStrength * optimalDistance * optimalDistance / (distance * distance);
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                scale = -1;
            }

            f.multiply(scale);
            return f;
        }

		@Override
		public ForceVector calculateForce(Spatial arg0, Spatial arg1, float arg2) {
			// TODO Auto-generated method stub
			return null;
		}
    }
	
    public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public boolean isConverged() {
        return converged;
    }

	@Override
	public boolean canAlgo() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setGraph(PyGraph g) {
		// TODO Auto-generated method stub
		this.graph = g;
	}

	@Override
	public PyGraph getGraph() {
		// TODO Auto-generated method stub
		return this.graph;
	}

}
