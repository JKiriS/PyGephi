package org.pygephi.statistics;

import java.util.HashMap;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.pygephi.core.GEdge;
import org.pygephi.core.GNode;
import org.pygephi.core.PyGraph;

public class PageRank implements Stat {

	    public static final String PAGERANK = "pageranks";

	    private boolean isCanceled;

	    private double epsilon = 0.001;
	    private double probability = 0.85;
	    private boolean useEdgeWeight = false;

	    private double[] pageranks;
	    private boolean isDirected;
	    
	    PyGraph graph;

	    public PageRank() {
//	        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
//	        if (graphController != null && graphController.getGraphModel() != null) {
//	            isDirected = graphController.getGraphModel().isDirected();
//	        }
	    }

	    public void setDirected(boolean isDirected) {
	        this.isDirected = isDirected;
	    }

	    /**
	     *
	     * @return
	     */
	    public boolean getDirected() {
	        return isDirected;
	    }

	    public void execute() {
	        isCanceled = false;
	        
	        Graph g;
	        if (isDirected) {
	            g = graph.getDirectedGraph();
	        } else {
	            g = graph.getUnDirectedGraph();
	        }
	        
	        g.readLock();

	        HashMap<Node, Integer> indicies = createIndiciesMap(g);

	        pageranks = calculatePagerank(g, indicies, isDirected, useEdgeWeight, epsilon, probability);

	        saveCalculatedValues(g, indicies, pageranks);

	        graph.readUnlock();
	    }

	    private void saveCalculatedValues(Graph hgraph, HashMap<Node, Integer> indicies,
	            double[] nodePagrank) {
	        for (Node s : hgraph.getNodes()) {
	            int s_index = indicies.get(s);

	            s.getNodeData().getAttributes().setValue(PAGERANK, nodePagrank[s_index]);
	        }
	    }

	    private void setInitialValues(Graph hgraph, double[] pagerankValues, double[] weights, boolean directed, boolean useWeights) {
	        int N = hgraph.getNodeCount();
	        int index = 0;
	        for (Node s : hgraph.getNodes()) {
	            pagerankValues[index] = 1.0f / N;
	            if (useWeights) {
	                double sum = 0;
	                EdgeIterable eIter;
	                if (directed) {
	                    eIter = ((DirectedGraph) hgraph).getOutEdges(s);
	                } else {
	                    eIter = ((UndirectedGraph) hgraph).getEdges(s);
	                }
	                for (GEdge edge : graph.getEdges()) {
	                    sum += edge.getWeight();
	                }
	                weights[index] = sum;
	            }
	            index++;
	        }
	    }

	    private double calculateR(Graph hgraph, double[] pagerankValues, HashMap<Node, Integer> indicies, boolean directed, double prob) {
	        int N = hgraph.getNodeCount();
	        double r = 0;
	        for (Node s : hgraph.getNodes()) {
	            int s_index = indicies.get(s);
	            boolean out;
	            if (directed) {
	                out = ((DirectedGraph) hgraph).getOutDegree(s) > 0;
	            } else {
	                out = ((UndirectedGraph) hgraph).getDegree(s) > 0;
	            }

	            if (out) {
	                r += (1.0 - prob) * (pagerankValues[s_index] / N);
	            } else {
	                r += (pagerankValues[s_index] / N);
	            }
	            if (isCanceled) {
	                hgraph.readUnlock();
	                return r;
	            }
	        }
	        return r;
	    }

	    private double updateValueForNode(Graph hgraph, Node s, double[] pagerankValues, double[] weights,
	            HashMap<Node, Integer> indicies, boolean directed, boolean useWeights, double r, double prob) {
	        double res = r;
	        EdgeIterable eIter;
	        if (directed) {
	            eIter = ((DirectedGraph) hgraph).getInEdges(s);
	        } else {
	            eIter = hgraph.getEdges(s);
	        }

	        for (Edge edge : eIter) {
	            Node neighbor = hgraph.getOpposite(s, edge);
	            int neigh_index = indicies.get(neighbor);
	            int normalize;
	            if (directed) {
	                normalize = ((DirectedGraph) hgraph).getOutDegree(neighbor);
	            } else {
	                normalize = hgraph.getDegree(neighbor);
	            }
	            if (useWeights) {
	                double weight = edge.getWeight() / weights[neigh_index];
	                res += prob * pagerankValues[neigh_index] * weight;
	            } else {
	                res += prob * (pagerankValues[neigh_index] / normalize);
	            }
	        }
	        return res;
	    }

	    double[] calculatePagerank(Graph hgraph, HashMap<Node, Integer> indicies,
	            boolean directed, boolean useWeights, double eps, double prob) {
	        int N = hgraph.getNodeCount();
	        double[] pagerankValues = new double[N];
	        double[] temp = new double[N];

	        double[] weights = new double[N];

	        setInitialValues(hgraph, pagerankValues, weights, directed, useWeights);

	        while (true) {
	            double r = calculateR(hgraph, pagerankValues, indicies, directed, prob);

	            boolean done = true;
	            for (Node s : hgraph.getNodes()) {
	                int s_index = indicies.get(s);
	                temp[s_index] = updateValueForNode(hgraph, s, pagerankValues, weights, indicies, directed, useWeights, r, prob);

	                if ((temp[s_index] - pagerankValues[s_index]) / pagerankValues[s_index] >= eps) {
	                    done = false;
	                }

	                if (isCanceled) {
	                    hgraph.readUnlock();
	                    return pagerankValues;
	                }

	            }
	            pagerankValues = temp;
	            temp = new double[N];
	            if ((done) || (isCanceled)) {
	                break;
	            }

	        }
	        return pagerankValues;
	    }

	    public HashMap<Node, Integer> createIndiciesMap(Graph hgraph) {
	        HashMap<Node, Integer> newIndicies = new HashMap<Node, Integer>();
	        int index = 0;
	        for (Node s : hgraph.getNodes()) {
	            newIndicies.put(s, index);
	            index++;
	        }
	        return newIndicies;
	    }

	    @Override
	    public boolean cancel() {
	        isCanceled = true;
	        return true;
	    }


	    /**
	     *
	     * @param prob
	     */
	    public void setProbability(double prob) {
	        probability = prob;
	    }

	    /**
	     *
	     * @param eps
	     */
	    public void setEpsilon(double eps) {
	        epsilon = eps;
	    }

	    /**
	     *
	     * @return
	     */
	    public double getProbability() {
	        return probability;
	    }

	    /**
	     *
	     * @return
	     */
	    public double getEpsilon() {
	        return epsilon;
	    }

	    public boolean isUseEdgeWeight() {
	        return useEdgeWeight;
	    }

	    public void setUseEdgeWeight(boolean useEdgeWeight) {
	        this.useEdgeWeight = useEdgeWeight;
	    }

		@Override
		public void setGraph(PyGraph g) {
			// TODO Auto-generated method stub
			this.graph = g;
		}
	}
