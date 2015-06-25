package org.pygephi.layout;

import org.gephi.layout.plugin.ForceVectorNodeLayoutData;
import org.openide.util.Lookup;
import org.pygephi.core.GEdge;
import org.pygephi.core.GNode;
import org.pygephi.core.PyGraph;

public class FRLayout implements GLayout{
	
	private static final float SPEED_DIVISOR = 800;
    private static final float AREA_MULTIPLICATOR = 10000;
    //Graph
    protected PyGraph graph;
    //Properties
    private float area;
    private double gravity;
    private double speed;
	
	public FRLayout(){
		this.graph = Lookup.getDefault().lookup(PyGraph.class);
		this.resetPropertiesValues();
	}
	
    public void resetPropertiesValues() {
        speed = 1;
        area = 10000;
        gravity = 10;
    }

    public void initAlgo() {
    }

    public void goAlgo() {
        graph.readLock();
        GNode[] nodes = graph.getNodes();
        GEdge[] edges = graph.getEdges();

        for (GNode n : nodes) {
            if (n.getLayoutData() == null || !(n.getLayoutData() instanceof ForceVectorNodeLayoutData)) {
                n.setLayoutData(new ForceVectorNodeLayoutData());
            }
            ForceVectorNodeLayoutData layoutData = n.getLayoutData();
            layoutData.dx = 0;
            layoutData.dy = 0;
        }

        float maxDisplace = (float) (Math.sqrt(AREA_MULTIPLICATOR * area) / 10f);					// Déplacement limite : on peut le calibrer...
        float k = (float) Math.sqrt((AREA_MULTIPLICATOR * area) / (1f + nodes.length));		// La variable k, l'idée principale du layout.

        for (GNode N1 : nodes) {
            for (GNode N2 : nodes) {	// On fait toutes les paires de noeuds
                if (N1 != N2) {
                    float xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
                    float yDist = N1.y() - N2.y();
                    float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

                    if (dist > 0) {
                        float repulsiveF = k * k / dist;			// Force de répulsion
                        ForceVectorNodeLayoutData layoutData = N1.getLayoutData();
                        layoutData.dx += xDist / dist * repulsiveF;		// on l'applique...
                        layoutData.dy += yDist / dist * repulsiveF;
                    }
                }
            }
        }
        for (GEdge E : edges) {
            // Idem, pour tous les noeuds on applique la force d'attraction

            GNode Nf = E.getSource();
            GNode Nt = E.getTarget();

            float xDist = Nf.x() - Nt.x();
            float yDist = Nf.y() - Nt.y();
            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            float attractiveF = dist * dist / k;

            if (dist > 0) {
                ForceVectorNodeLayoutData sourceLayoutData = Nf.getLayoutData();
                ForceVectorNodeLayoutData targetLayoutData = Nt.getLayoutData();
                sourceLayoutData.dx -= xDist / dist * attractiveF;
                sourceLayoutData.dy -= yDist / dist * attractiveF;
                targetLayoutData.dx += xDist / dist * attractiveF;
                targetLayoutData.dy += yDist / dist * attractiveF;
            }
        }
        // gravity
        for (GNode n : nodes) {
            ForceVectorNodeLayoutData layoutData = n.getLayoutData();
            float d = (float) Math.sqrt(n.x() * n.x() + n.y() * n.y());
            float gf = 0.01f * k * (float) gravity * d;
            layoutData.dx -= gf * n.x() / d;
            layoutData.dy -= gf * n.y() / d;
        }
        // speed
        for (GNode n : nodes) {
            ForceVectorNodeLayoutData layoutData = n.getLayoutData();
            layoutData.dx *= speed / SPEED_DIVISOR;
            layoutData.dy *= speed / SPEED_DIVISOR;
        }
        for (GNode n : nodes) {
            // Maintenant on applique le déplacement calculé sur les noeuds.
            // nb : le déplacement à chaque passe "instantanné" correspond à la force : c'est une sorte d'accélération.
            ForceVectorNodeLayoutData layoutData = n.getLayoutData();
            float xDist = layoutData.dx;
            float yDist = layoutData.dy;
            float dist = (float) Math.sqrt(layoutData.dx * layoutData.dx + layoutData.dy * layoutData.dy);
            if (dist > 0 && !n.isFixed()) {
                float limitedDist = Math.min(maxDisplace * ((float) speed / SPEED_DIVISOR), dist);
                n.setX(n.x() + xDist / dist * limitedDist);
                n.setY(n.y() + yDist / dist * limitedDist);
            }
        }
        graph.readUnlock();
    }

    public void endAlgo() {
        for (GNode n : graph.getNodes()) {
            n.setLayoutData(null);
        }
    }

    public boolean canAlgo() {
        return true;
    }

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
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

	@Override
	public void setGraph(PyGraph g) {
		// TODO Auto-generated method stub
		this.graph = g;
	}

}
