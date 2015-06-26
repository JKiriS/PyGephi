package org.pygephi.layout;

import org.gephi.layout.plugin.ForceVectorNodeLayoutData;
import org.gephi.layout.plugin.ForceVectorUtils;
import org.pygephi.core.GEdge;
import org.pygephi.core.GNode;
import org.pygephi.core.PyGraph;

public class FALayout implements GLayout{
	
	public FALayout(){
		this.resetPropertiesValues();
	}
	
	protected PyGraph graph;
    //Properties
    public double inertia;
    private double repulsionStrength;
    private double attractionStrength;
    private double maxDisplacement;
    private boolean freezeBalance;
    private double freezeStrength;
    private double freezeInertia;
    private double gravity;
    private double speed;
    private double cooling;
    private boolean outboundAttractionDistribution;
    private boolean adjustSizes;


    @Override
    public void resetPropertiesValues() {
        inertia = 0.1;
        setRepulsionStrength(200d);
        setAttractionStrength(10d);
        setMaxDisplacement(10d);
        setFreezeBalance(true);
        setFreezeStrength(80d);
        setFreezeInertia(0.2);
        setGravity(30d);
        setOutboundAttractionDistribution(false);
        setAdjustSizes(false);
        setSpeed(1d);
        setCooling(1d);
    }

    @Override
    public void initAlgo() {
    }

    @Override
    public void goAlgo() {
        graph.readLock();
        GNode[] nodes = graph.getNodes();
        GEdge[] edges = graph.getEdges();

        for (GNode n : nodes) {
            if (n.getLayoutData() == null || !(n.getLayoutData() instanceof ForceVectorNodeLayoutData)) {
                n.setLayoutData(new ForceVectorNodeLayoutData());
            }
        }

        for (GNode n : nodes) {
            ForceVectorNodeLayoutData layoutData = n.getLayoutData();
            layoutData.old_dx = layoutData.dx;
            layoutData.old_dy = layoutData.dy;
            layoutData.dx *= inertia;
            layoutData.dy *= inertia;
        }
        // repulsion
        if (isAdjustSizes()) {
            for (GNode n1 : nodes) {
                for (GNode n2 : nodes) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor_noCollide(n1.getNodeData(), n2.getNodeData(), getRepulsionStrength() * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                    }
                }
            }
        } else {
            for (GNode n1 : nodes) {
                for (GNode n2 : nodes) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor(n1.getNodeData(), n2.getNodeData(), getRepulsionStrength() * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                    }
                }
            }
        }
        // attraction
        if (isAdjustSizes()) {
            if (isOutboundAttractionDistribution()) {
                for (GEdge e : edges) {
                    GNode nf = e.getSource();
                    GNode nt = e.getTarget();
                    double bonus = (nf.isFixed() || nt.isFixed()) ? (100) : (1);
                    bonus *= e.getWeight();
                    ForceVectorUtils.fcBiAttractor_noCollide(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength() / (1 + graph.getDegree(nf)));
                }
            } else {
                for (GEdge e : edges) {
                    GNode nf = e.getSource();
                    GNode nt = e.getTarget();
                    double bonus = (nf.isFixed() || nt.isFixed()) ? (100) : (1);
                    bonus *= e.getWeight();
                    ForceVectorUtils.fcBiAttractor_noCollide(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength());
                }
            }
        } else {
            if (isOutboundAttractionDistribution()) {
                for (GEdge e : edges) {
                    GNode nf = e.getSource();
                    GNode nt = e.getTarget();
                    double bonus = (nf.isFixed() || nt.isFixed()) ? (100) : (1);
                    bonus *= e.getWeight();
                    ForceVectorUtils.fcBiAttractor(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength() / (1 + graph.getDegree(nf)));
                }
            } else {
                for (GEdge e : edges) {
                    GNode nf = e.getSource();
                    GNode nt = e.getTarget();
                    double bonus = (nf.isFixed() || nt.isFixed()) ? (100) : (1);
                    bonus *= e.getWeight();
                    ForceVectorUtils.fcBiAttractor(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength());
                }
            }
        }
        // gravity
        for (GNode n : nodes) {

            float nx = n.x();
            float ny = n.y();
            double d = 0.0001 + Math.sqrt(nx * nx + ny * ny);
            double gf = 0.0001 * getGravity() * d;
            ForceVectorNodeLayoutData layoutData = n.getLayoutData();
            layoutData.dx -= gf * nx / d;
            layoutData.dy -= gf * ny / d;
        }
        // speed
        if (isFreezeBalance()) {
            for (GNode n : nodes) {
                ForceVectorNodeLayoutData layoutData = n.getLayoutData();
                layoutData.dx *= getSpeed() * 10f;
                layoutData.dy *= getSpeed() * 10f;
            }
        } else {
            for (GNode n : nodes) {
                ForceVectorNodeLayoutData layoutData = n.getLayoutData();
                layoutData.dx *= getSpeed();
                layoutData.dy *= getSpeed();
            }
        }
        // apply forces
        for (GNode n : nodes) {
            ForceVectorNodeLayoutData nLayout = n.getLayoutData();
            if (!n.isFixed()) {
                double d = 0.0001 + Math.sqrt(nLayout.dx * nLayout.dx + nLayout.dy * nLayout.dy);
                float ratio;
                if (isFreezeBalance()) {
                    nLayout.freeze = (float) (getFreezeInertia() * nLayout.freeze + (1 - getFreezeInertia()) * 0.1 * getFreezeStrength() * (Math.sqrt(Math.sqrt((nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy)))));
                    ratio = (float) Math.min((d / (d * (1f + nLayout.freeze))), getMaxDisplacement() / d);
                } else {
                    ratio = (float) Math.min(1, getMaxDisplacement() / d);
                }
                nLayout.dx *= ratio / getCooling();
                nLayout.dy *= ratio / getCooling();
                float x = n.x() + nLayout.dx;
                float y = n.y() + nLayout.dy;

                n.setX(x);
                n.setY(y);
            }
        }
        graph.readUnlock();
    }

    @Override
    public void endAlgo() {
        for (GNode n : graph.getNodes()) {
            n.setLayoutData(null);
        }
    }

    @Override
    public boolean canAlgo() {
        return true;
    }

    public void setInertia(Double inertia) {
        this.inertia = inertia;
    }

    public Double getInertia() {
        return inertia;
    }

    /**
     * @return the repulsionStrength
     */
    public Double getRepulsionStrength() {
        return repulsionStrength;
    }

    /**
     * @param repulsionStrength the repulsionStrength to set
     */
    public void setRepulsionStrength(Double repulsionStrength) {
        this.repulsionStrength = repulsionStrength;
    }

    /**
     * @return the attractionStrength
     */
    public Double getAttractionStrength() {
        return attractionStrength;
    }

    /**
     * @param attractionStrength the attractionStrength to set
     */
    public void setAttractionStrength(Double attractionStrength) {
        this.attractionStrength = attractionStrength;
    }

    /**
     * @return the maxDisplacement
     */
    public Double getMaxDisplacement() {
        return maxDisplacement;
    }

    /**
     * @param maxDisplacement the maxDisplacement to set
     */
    public void setMaxDisplacement(Double maxDisplacement) {
        this.maxDisplacement = maxDisplacement;
    }

    /**
     * @return the freezeBalance
     */
    public Boolean isFreezeBalance() {
        return freezeBalance;
    }

    /**
     * @param freezeBalance the freezeBalance to set
     */
    public void setFreezeBalance(Boolean freezeBalance) {
        this.freezeBalance = freezeBalance;
    }

    /**
     * @return the freezeStrength
     */
    public Double getFreezeStrength() {
        return freezeStrength;
    }

    /**
     * @param freezeStrength the freezeStrength to set
     */
    public void setFreezeStrength(Double freezeStrength) {
        this.freezeStrength = freezeStrength;
    }

    /**
     * @return the freezeInertia
     */
    public Double getFreezeInertia() {
        return freezeInertia;
    }

    /**
     * @param freezeInertia the freezeInertia to set
     */
    public void setFreezeInertia(Double freezeInertia) {
        this.freezeInertia = freezeInertia;
    }

    /**
     * @return the gravity
     */
    public Double getGravity() {
        return gravity;
    }

    /**
     * @param gravity the gravity to set
     */
    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    /**
     * @return the speed
     */
    public Double getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    /**
     * @return the cooling
     */
    public Double getCooling() {
        return cooling;
    }

    /**
     * @param cooling the cooling to set
     */
    public void setCooling(Double cooling) {
        this.cooling = cooling;
    }

    /**
     * @return the outboundAttractionDistribution
     */
    public Boolean isOutboundAttractionDistribution() {
        return outboundAttractionDistribution;
    }

    /**
     * @param outboundAttractionDistribution the outboundAttractionDistribution
     * to set
     */
    public void setOutboundAttractionDistribution(Boolean outboundAttractionDistribution) {
        this.outboundAttractionDistribution = outboundAttractionDistribution;
    }

    /**
     * @return the adjustSizes
     */
    public Boolean isAdjustSizes() {
        return adjustSizes;
    }

    /**
     * @param adjustSizes the adjustSizes to set
     */
    public void setAdjustSizes(Boolean adjustSizes) {
        this.adjustSizes = adjustSizes;
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
