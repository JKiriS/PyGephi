package org.pygephi.statistics;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.pygephi.core.GEdge;
import org.pygephi.core.GNode;
import org.pygephi.core.PyGraph;

class Renumbering implements Comparator<EdgeWrapper> {

    @Override
    public int compare(EdgeWrapper o1, EdgeWrapper o2) {
        if (o1.wrapper.getID() < o2.wrapper.getID()) {
            return -1;
        } else if (o1.wrapper.getID() > o2.wrapper.getID()) {
            return 1;
        } else {
            return 0;
        }
    }
}

/**
 *
 * @author pjmcswee
 */
class EdgeWrapper {

    public int count;
    public ArrayWrapper wrapper;

    public EdgeWrapper(int count, ArrayWrapper wrapper) {
        this.count = count;
        this.wrapper = wrapper;
    }
}

/**
 *
 * @author pjmcswee
 */
class ArrayWrapper implements Comparable {

    private EdgeWrapper[] array;
    private int ID;
    public Node node;

    /**
     * Empty Constructor/
     */
    ArrayWrapper() {
    }

    /**
     *
     * @return The ID of this array wrapper
     */
    public int getID() {
        return ID;
    }

    /**
     *
     * @return The adjacency array
     */
    public EdgeWrapper[] getArray() {
        return array;
    }

    public void setArray(EdgeWrapper[] array) {
        this.array = array;
    }

    /**
     *
     * @param pArray
     */
    ArrayWrapper(int ID, EdgeWrapper[] array) {
        this.array = array;
        this.ID = ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     *
     * @param pIndex
     * @return
     */
    public int get(int index) {
        if (index >= array.length) {
            return -1;
        }
        return array[index].wrapper.ID;
    }

    public int getCount(int index) {
        if (index >= array.length) {
            return -1;
        }
        return array[index].count;
    }

    /**
     *
     * @return
     */
    public int length() {
        return array.length;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        ArrayWrapper aw = (ArrayWrapper) o;
        if (aw.length() < length()) {
            return -1;
        }
        if (aw.length() > length()) {
            return 1;
        }
        return 0;
    }
}

/**
 *
 * @author Patrick J. McSweeney
 */
public class ClusteringCoefficient implements Stat {

    public static final String CLUSTERING_COEFF = "clustering";
    /**
     * The avergage Clustering Coefficient.
     */
    private double avgClusteringCoeff;
    /**
     * Indicates should treat graph as undirected.
     */
    private boolean isDirected;
    /**
     * Indicates statistics should stop processing/
     */
    private boolean isCanceled;

    private int[] triangles;
    private ArrayWrapper[] network;
    private int K;
    private int N;
    private double[] nodeClustering;
    private int totalTriangles;
    PyGraph graph;

    public ClusteringCoefficient() {

    }

    public double getAverageClusteringCoefficient() {
        return avgClusteringCoeff;
    }

    public void execute() {
        isCanceled = false;

        HashMap<String, Double> resultValues = new HashMap<String, Double>();
        
        
        Graph hgraph = null;
        if (isDirected) {
            hgraph = graph.getDirectedGraph();
        } else {
            hgraph = graph.getUnDirectedGraph();
        }
        
        
        if (isDirected) {
            avgClusteringCoeff = bruteForce(hgraph);
        } else {
            initStartValues(hgraph);
            resultValues = computeTriangles(hgraph, network, triangles, nodeClustering, isDirected);
            totalTriangles = resultValues.get("triangles").intValue();
            avgClusteringCoeff = resultValues.get("clusteringCoefficient");

        }

        for (int v = 0; v < N; v++) {
            if (network[v].length() > 1) {
                network[v].node.getNodeData().getAttributes().setValue(CLUSTERING_COEFF, nodeClustering[v]);
                if (!isDirected) {
                    network[v].node.getNodeData().getAttributes().setValue("Triangles", triangles[v]);
                }
            }
        }
    }

    public void triangles(Graph hgraph) {
        initStartValues(hgraph);
        HashMap<String, Double> resultValues = computeTriangles(hgraph, network, triangles,
                nodeClustering, isDirected);
        totalTriangles = resultValues.get("triangles").intValue();
        avgClusteringCoeff = resultValues.get("clusteringCoefficient");
    }

    public HashMap<String, Double> computeClusteringCoefficient(Graph hgraph, ArrayWrapper[] currentNetwork,
            int[] currentTriangles, double[] currentNodeClustering, boolean directed) {
        HashMap<String, Double> resultValues = new HashMap<String, Double>();

        if (isDirected) {
            double avClusteringCoefficient = bruteForce(hgraph);
            resultValues.put("clusteringCoefficient", avClusteringCoefficient);
            return resultValues;
        } else {
            initStartValues(hgraph);
            resultValues = computeTriangles(hgraph, currentNetwork, currentTriangles, currentNodeClustering, directed);
            return resultValues;

        }
    }

    public void initStartValues(Graph hgraph) {
        N = hgraph.getNodeCount();
        K = (int) Math.sqrt(N);
        nodeClustering = new double[N];
        network = new ArrayWrapper[N];
        triangles = new int[N];
    }

    public int createIndiciesMapAndInitNetwork(Graph hgraph, HashMap<Node, Integer> indicies, ArrayWrapper[] networks, int currentProgress) {
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            networks[index] = new ArrayWrapper();
            index++;
        }
        return currentProgress;
    }

    private int closest_in_array(ArrayWrapper[] currentNetwork, int v) {
        int right = currentNetwork[v].length() - 1;

        /* optimization for extreme cases */
        if (right < 0) {
            return (-1);
        }
        if (currentNetwork[v].get(0) >= v) {
            return (-1);
        }
        if (currentNetwork[v].get(right) < v) {
            return (right);
        }
        if (currentNetwork[v].get(right) == v) {
            return (right - 1);
        }

        int left = 0, mid;
        while (right > left) {
            mid = (left + right) / 2;
            if (v < currentNetwork[v].get(mid)) {
                right = mid - 1;
            } else if (v > currentNetwork[v].get(mid)) {
                left = mid + 1;
            } else {
                return (mid - 1);
            }
        }

        if (v > currentNetwork[v].get(right)) {
            return (right);
        } else {

            return right - 1;
        }
    }

    /**
     *
     * @param v - The specific node to count the triangles on.
     */
    private void newVertex(ArrayWrapper[] currentNetwork, int[] currentTrianlgles, int v, int n) {
        int[] A = new int[n];

        for (int i = currentNetwork[v].length() - 1; (i >= 0) && (currentNetwork[v].get(i) > v); i--) {
            int neighbor = currentNetwork[v].get(i);
            A[neighbor] = currentNetwork[v].getCount(i);
        }
        for (int i = currentNetwork[v].length() - 1; i >= 0; i--) {
            int neighbor = currentNetwork[v].get(i);
            for (int j = closest_in_array(currentNetwork, neighbor); j >= 0; j--) {
                int next = currentNetwork[neighbor].get(j);
                if (A[next] > 0) {
                    currentTrianlgles[next] += currentNetwork[v].getCount(i);
                    currentTrianlgles[v] += currentNetwork[v].getCount(i);
                    currentTrianlgles[neighbor] += A[next];
                }
            }
        }
    }

    private void tr_link_nohigh(ArrayWrapper[] currentNetwork, int[] currentTriangles, int u, int v, int count, int k) {
        int iu = 0, iv = 0, w;
        while ((iu < currentNetwork[u].length()) && (iv < currentNetwork[v].length())) {
            if (currentNetwork[u].get(iu) < currentNetwork[v].get(iv)) {
                iu++;
            } else if (currentNetwork[u].get(iu) > currentNetwork[v].get(iv)) {
                iv++;
            } else { /* neighbor in common */

                w = currentNetwork[u].get(iu);
                if (w >= k) {
                    currentTriangles[w] += count;
                }
                iu++;
                iv++;
            }
        }
    }

    private HashMap<Node, EdgeWrapper> createNeighbourTable(Graph hgraph, Node node, HashMap<Node, Integer> indicies,
            ArrayWrapper[] networks, boolean directed) {

        HashMap<Node, EdgeWrapper> neighborTable = new HashMap<Node, EdgeWrapper>();

        if (!directed) {
            for (Edge edge : hgraph.getEdges(node)) {
                Node neighbor = hgraph.getOpposite(node, edge);
                neighborTable.put(neighbor, new EdgeWrapper(1, networks[indicies.get(neighbor)]));
            }
        } else {
            for (Node neighbor : ((DirectedGraph) hgraph).getPredecessors(node)) {
                neighborTable.put(neighbor, new EdgeWrapper(1, networks[indicies.get(neighbor)]));
            }

            for (Edge out : ((DirectedGraph) hgraph).getOutEdges(node)) {
                Node neighbor = out.getTarget();
                EdgeWrapper ew = neighborTable.get(neighbor);
                if (ew == null) {
                    neighborTable.put(neighbor, new EdgeWrapper(1, network[indicies.get(neighbor)]));
                } else {
                    ew.count++;
                }
            }
        }
        return neighborTable;
    }

    private EdgeWrapper[] getEdges(HashMap<Node, EdgeWrapper> neighborTable) {

        int i = 0;
        EdgeWrapper[] edges = new EdgeWrapper[neighborTable.size()];
        for (EdgeWrapper e : neighborTable.values()) {
            edges[i] = e;
            i++;
        }
        return edges;
    }

    private int processNetwork(ArrayWrapper[] currentNetwork, int currentProgress) {
        Arrays.sort(currentNetwork);
        for (int j = 0; j < N; j++) {
            currentNetwork[j].setID(j);
        }

        for (int j = 0; j < N; j++) {
            Arrays.sort(currentNetwork[j].getArray(), new Renumbering());
        }
        return currentProgress;
    }

    private int computeRemainingTrianles(Graph hgraph, ArrayWrapper[] currentNetwork, int[] currentTriangles, int currentProgress) {
        int n = hgraph.getNodeCount();
        int k = (int) Math.sqrt(n);
        for (int v = n - 1; (v >= 0) && (v >= k); v--) {
            for (int i = closest_in_array(currentNetwork, v); i >= 0; i--) {
                int u = currentNetwork[v].get(i);
                if (u >= k) {
                    tr_link_nohigh(currentNetwork, currentTriangles, u, v, currentNetwork[v].getCount(i), k);
                }
            }

            if (isCanceled) {
                hgraph.readUnlock();
                return currentProgress;
            }
        }
        return currentProgress;
    }

    private HashMap<String, Double> computeResultValues(Graph hgraph, ArrayWrapper[] currentNetwork,
            int[] currentTriangles, double[] currentNodeClusterig, boolean directed, int currentProgress) {
        int n = hgraph.getNodeCount();
        HashMap<String, Double> totalValues = new HashMap<String, Double>();
        int numNodesDegreeGreaterThanOne = 0;
        int trianglesNumber = 0;
        double currentClusteringCoefficient = 0;
        for (int v = 0; v < n; v++) {
            if (currentNetwork[v].length() > 1) {
                numNodesDegreeGreaterThanOne++;
                double cc = currentTriangles[v];
                trianglesNumber += currentTriangles[v];
                cc /= (currentNetwork[v].length() * (currentNetwork[v].length() - 1));
                if (!directed) {
                    cc *= 2.0f;
                }
                currentNodeClusterig[v] = cc;
                currentClusteringCoefficient += cc;
            }

            if (isCanceled) {
                hgraph.readUnlock();
                return totalValues;
            }
        }
        trianglesNumber /= 3;
        currentClusteringCoefficient /= numNodesDegreeGreaterThanOne;

        totalValues.put("triangles", (double) trianglesNumber);
        totalValues.put("clusteringCoefficient", currentClusteringCoefficient);
        return totalValues;
    }

    private HashMap<String, Double> computeTriangles(Graph hgraph, ArrayWrapper[] currentNetwork, int[] currentTriangles,
            double[] nodeClustering, boolean directed) {

        HashMap<String, Double> resultValues = new HashMap<String, Double>();
        int ProgressCount = 0;

        hgraph.readLock();

        int n = hgraph.getNodeCount();

        /**
         * Create network for processing
         */
        /**
         *         */
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();

        ProgressCount = createIndiciesMapAndInitNetwork(hgraph, indicies, currentNetwork, ProgressCount);

        int index = 0;
        for (Node node : hgraph.getNodes()) {
            HashMap<Node, EdgeWrapper> neighborTable = createNeighbourTable(hgraph, node, indicies, currentNetwork, directed);

            EdgeWrapper[] edges = getEdges(neighborTable);
            currentNetwork[index].node = node;
            currentNetwork[index].setArray(edges);
            index++;

            if (isCanceled) {
                hgraph.readUnlock();
                return resultValues;
            }
        }

        ProgressCount = processNetwork(currentNetwork, ProgressCount);

        int k = (int) Math.sqrt(n);

        for (int v = 0; v < k && v < n; v++) {
            newVertex(currentNetwork, currentTriangles, v, n);
        }

        /* remaining links */
        ProgressCount = computeRemainingTrianles(hgraph, currentNetwork, currentTriangles, ProgressCount);

        resultValues = computeResultValues(hgraph, currentNetwork, currentTriangles, nodeClustering, directed, ProgressCount);

        hgraph.readUnlock();
        return resultValues;
    }

    private double bruteForce(Graph g) {
        float totalCC = 0;

        g.readLock();
        for (Node node : g.getNodes()) {
            float nodeClusteringCoefficient = computeNodeClusteringCoefficient(g, node, isDirected);

            if (nodeClusteringCoefficient > -1) {

                saveCalculatedValue(node, nodeClusteringCoefficient);

                totalCC += nodeClusteringCoefficient;
            }

            if (isCanceled) {
                break;
            }
        }
        double clusteringCoeff = totalCC / graph.getNodeCount();

        graph.readUnlock();

        return clusteringCoeff;
    }

    private float increaseCCifNesessary(Graph hgraph, Node neighbor1, Node neighbor2, boolean directed, float nodeCC) {
        if (neighbor1 == neighbor2) {
            return nodeCC;
        }
        if (directed) {
            if (hgraph.isAdjacent(neighbor1, neighbor2)) {
                nodeCC++;
            }
            if (hgraph.isAdjacent(neighbor2, neighbor1)) {
                nodeCC++;
            }
        } else {
            if (hgraph.isAdjacent(neighbor1, neighbor2)) {
                nodeCC++;
            }
        }
        return nodeCC;
    }

    private float computeNodeClusteringCoefficient(Graph hgraph, Node node, boolean directed) {
        float nodeCC = 0;
        int neighborhood = 0;
        NodeIterable neighbors1 = hgraph.getNeighbors(node);
        for (Node neighbor1 : neighbors1) {
            neighborhood++;
            NodeIterable neighbors2 = hgraph.getNeighbors(node);

            for (Node neighbor2 : neighbors2) {
                nodeCC = increaseCCifNesessary(hgraph, neighbor1, neighbor2, directed, nodeCC);
            }
        }
        nodeCC /= 2.0;

        if (neighborhood > 1) {
            float cc = nodeCC / (.5f * neighborhood * (neighborhood - 1));
            if (directed) {
                cc = nodeCC / (neighborhood * (neighborhood - 1));
            }

            return cc;
        } else {
            return -1.f;
        }
    }

    private void saveCalculatedValue(Node node, float nodeClusteringCoefficient) {
        node.getNodeData().getAttributes().setValue(CLUSTERING_COEFF, nodeClusteringCoefficient);
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    @Override
    public boolean cancel() {
        isCanceled = true;
        return true;
    }


    public double[] getCoefficientReuslts() {
        double[] res = new double[N];
        for (int v = 0; v < N; v++) {
            if (network[v].length() > 1) {
                res[v] = nodeClustering[v];
            }
        }
        return res;
    }

    public double[] getTriangesReuslts() {
        double[] res = new double[N];
        for (int v = 0; v < N; v++) {
            if (network[v].length() > 1) {
                res[v] = triangles[v];
            }
        }
        return res;
    }


	@Override
	public void setGraph(PyGraph g) {
		// TODO Auto-generated method stub
		this.graph = g;
	}
}
