package org.pygephi.statistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import org.pygephi.core.GNode;
import org.pygephi.core.PyGraph;

public class Modularity implements Stat {

	    public static final String MODULARITY_CLASS = "modularity";

	    private boolean isCanceled;
	    private CommunityStructure structure;
	    private double modularity;
	    private double modularityResolution;
	    private boolean isRandomized = false;
	    private boolean useWeight = true;
	    private double resolution = 1.;
	    PyGraph graph;

	    public void setRandom(boolean isRandomized) {
	        this.isRandomized = isRandomized;
	    }

	    public boolean getRandom() {
	        return isRandomized;
	    }

	    public void setUseWeight(boolean useWeight) {
	        this.useWeight = useWeight;
	    }

	    public boolean getUseWeight() {
	        return useWeight;
	    }

	    public void setResolution(double resolution) {
	        this.resolution = resolution;
	    }

	    public double getResolution() {
	        return resolution;
	    }

	    public boolean cancel() {
	        this.isCanceled = true;
	        return true;
	    }


	    class ModEdge {

	        int source;
	        int target;
	        float weight;

	        public ModEdge(int s, int t, float w) {
	            source = s;
	            target = t;
	            weight = w;
	        }
	    }

	    class CommunityStructure {

	        HashMap<Modularity.Community, Float>[] nodeConnectionsWeight;
	        HashMap<Modularity.Community, Integer>[] nodeConnectionsCount;
	        HashMap<GNode, Integer> map;
	        Community[] nodeCommunities;
	        PyGraph graph;
	        double[] weights;
	        double graphWeightSum;
	        LinkedList<ModEdge>[] topology;
	        LinkedList<Community> communities;
	        int N;
	        HashMap<Integer, Community> invMap;

	        CommunityStructure(PyGraph hgraph) {
	            this.graph = hgraph;
	            N = hgraph.getNodeCount();
	            invMap = new HashMap<Integer, Community>();
	            nodeConnectionsWeight = new HashMap[N];
	            nodeConnectionsCount = new HashMap[N];
	            nodeCommunities = new Community[N];
	            map = new HashMap<GNode, Integer>();
	            topology = new LinkedList[N];
	            communities = new LinkedList<Community>();
	            int index = 0;
	            weights = new double[N];
	            for (GNode node : hgraph.getNodes()) {
	                map.put(node, index);
	                nodeCommunities[index] = new Community(this);
	                nodeConnectionsWeight[index] = new HashMap<Community, Float>();
	                nodeConnectionsCount[index] = new HashMap<Community, Integer>();
	                weights[index] = 0;
	                nodeCommunities[index].seed(index);
	                Community hidden = new Community(structure);
	                hidden.nodes.add(index);
	                invMap.put(index, hidden);
	                communities.add(nodeCommunities[index]);
	                index++;
	                if (isCanceled) {
	                    return;
	                }
	            }

	            for (GNode node : hgraph.getNodes()) {
	                int node_index = map.get(node);
	                topology[node_index] = new LinkedList<ModEdge>();

	                for (GNode neighbor : hgraph.getNeighbors(node)) {
	                    if (node == neighbor) {
	                        continue;
	                    }
	                    int neighbor_index = map.get(neighbor);
	                    float weight = 1;
	                    if (useWeight) {
	                        weight = (float) hgraph.getEdge(node.getId(), neighbor.getId()).getWeight();
	                    }

	                    weights[node_index] += weight;
	                    Modularity.ModEdge me = new ModEdge(node_index, neighbor_index, weight);
	                    topology[node_index].add(me);
	                    Community adjCom = nodeCommunities[neighbor_index];
	                    nodeConnectionsWeight[node_index].put(adjCom, weight);
	                    nodeConnectionsCount[node_index].put(adjCom, 1);
	                    nodeCommunities[node_index].connectionsWeight.put(adjCom, weight);
	                    nodeCommunities[node_index].connectionsCount.put(adjCom, 1);
	                    nodeConnectionsWeight[neighbor_index].put(nodeCommunities[node_index], weight);
	                    nodeConnectionsCount[neighbor_index].put(nodeCommunities[node_index], 1);
	                    nodeCommunities[neighbor_index].connectionsWeight.put(nodeCommunities[node_index], weight);
	                    nodeCommunities[neighbor_index].connectionsCount.put(nodeCommunities[node_index], 1);
	                    graphWeightSum += weight;
	                }

	                if (isCanceled) {
	                    return;
	                }
	            }
	            graphWeightSum /= 2.0;
	        }

	        private void addNodeTo(int node, Community to) {
	            to.add(new Integer(node));
	            nodeCommunities[node] = to;

	            for (ModEdge e : topology[node]) {
	                int neighbor = e.target;

	                ////////
	                //Remove Node Connection to this community
	                Float neighEdgesTo = nodeConnectionsWeight[neighbor].get(to);
	                if (neighEdgesTo == null) {
	                    nodeConnectionsWeight[neighbor].put(to, e.weight);
	                } else {
	                    nodeConnectionsWeight[neighbor].put(to, neighEdgesTo + e.weight);
	                }
	                Integer neighCountEdgesTo = nodeConnectionsCount[neighbor].get(to);
	                if (neighCountEdgesTo == null) {
	                    nodeConnectionsCount[neighbor].put(to, 1);
	                } else {
	                    nodeConnectionsCount[neighbor].put(to, neighCountEdgesTo + 1);
	                }

	                ///////////////////
	                Modularity.Community adjCom = nodeCommunities[neighbor];
	                Float wEdgesto = adjCom.connectionsWeight.get(to);
	                if (wEdgesto == null) {
	                    adjCom.connectionsWeight.put(to, e.weight);
	                } else {
	                    adjCom.connectionsWeight.put(to, wEdgesto + e.weight);
	                }

	                Integer cEdgesto = adjCom.connectionsCount.get(to);
	                if (cEdgesto == null) {
	                    adjCom.connectionsCount.put(to, 1);
	                } else {
	                    adjCom.connectionsCount.put(to, cEdgesto + 1);
	                }

	                Float nodeEdgesTo = nodeConnectionsWeight[node].get(adjCom);
	                if (nodeEdgesTo == null) {
	                    nodeConnectionsWeight[node].put(adjCom, e.weight);
	                } else {
	                    nodeConnectionsWeight[node].put(adjCom, nodeEdgesTo + e.weight);
	                }

	                Integer nodeCountEdgesTo = nodeConnectionsCount[node].get(adjCom);
	                if (nodeCountEdgesTo == null) {
	                    nodeConnectionsCount[node].put(adjCom, 1);
	                } else {
	                    nodeConnectionsCount[node].put(adjCom, nodeCountEdgesTo + 1);
	                }

	                if (to != adjCom) {
	                    Float comEdgesto = to.connectionsWeight.get(adjCom);
	                    if (comEdgesto == null) {
	                        to.connectionsWeight.put(adjCom, e.weight);
	                    } else {
	                        to.connectionsWeight.put(adjCom, comEdgesto + e.weight);
	                    }

	                    Integer comCountEdgesto = to.connectionsCount.get(adjCom);
	                    if (comCountEdgesto == null) {
	                        to.connectionsCount.put(adjCom, 1);
	                    } else {
	                        to.connectionsCount.put(adjCom, comCountEdgesto + 1);
	                    }

	                }
	            }
	        }

	        private void removeNodeFrom(int node, Community from) {

	            Community community = nodeCommunities[node];
	            for (ModEdge e : topology[node]) {
	                int neighbor = e.target;

	                ////////
	                //Remove Node Connection to this community
	                Float edgesTo = nodeConnectionsWeight[neighbor].get(community);
	                Integer countEdgesTo = nodeConnectionsCount[neighbor].get(community);
	                if (countEdgesTo - 1 == 0) {
	                    nodeConnectionsWeight[neighbor].remove(community);
	                    nodeConnectionsCount[neighbor].remove(community);
	                } else {
	                    nodeConnectionsWeight[neighbor].put(community, edgesTo - e.weight);
	                    nodeConnectionsCount[neighbor].put(community, countEdgesTo - 1);
	                }

	                ///////////////////
	                //Remove Adjacency Community's connection to this community
	                Modularity.Community adjCom = nodeCommunities[neighbor];
	                Float oEdgesto = adjCom.connectionsWeight.get(community);
	                Integer oCountEdgesto = adjCom.connectionsCount.get(community);
	                if (oCountEdgesto - 1 == 0) {
	                    adjCom.connectionsWeight.remove(community);
	                    adjCom.connectionsCount.remove(community);
	                } else {
	                    adjCom.connectionsWeight.put(community, oEdgesto - e.weight);
	                    adjCom.connectionsCount.put(community, oCountEdgesto - 1);
	                }

	                if (node == neighbor) {
	                    continue;
	                }

	                if (adjCom != community) {
	                    Float comEdgesto = community.connectionsWeight.get(adjCom);
	                    Integer comCountEdgesto = community.connectionsCount.get(adjCom);
	                    if (comCountEdgesto - 1 == 0) {
	                        community.connectionsWeight.remove(adjCom);
	                        community.connectionsCount.remove(adjCom);
	                    } else {
	                        community.connectionsWeight.put(adjCom, comEdgesto - e.weight);
	                        community.connectionsCount.put(adjCom, comCountEdgesto - 1);
	                    }
	                }

	                Float nodeEgesTo = nodeConnectionsWeight[node].get(adjCom);
	                Integer nodeCountEgesTo = nodeConnectionsCount[node].get(adjCom);
	                if (nodeCountEgesTo - 1 == 0) {
	                    nodeConnectionsWeight[node].remove(adjCom);
	                    nodeConnectionsCount[node].remove(adjCom);
	                } else {
	                    nodeConnectionsWeight[node].put(adjCom, nodeEgesTo - e.weight);
	                    nodeConnectionsCount[node].put(adjCom, nodeCountEgesTo - 1);
	                }

	            }
	            from.remove(new Integer(node));
	        }

	        private void moveNodeTo(int node, Community to) {
	            Community from = nodeCommunities[node];
	            removeNodeFrom(node, from);
	            addNodeTo(node, to);
	        }

	        private void zoomOut() {
	            int M = communities.size();
	            LinkedList<ModEdge>[] newTopology = new LinkedList[M];
	            int index = 0;
	            nodeCommunities = new Community[M];
	            nodeConnectionsWeight = new HashMap[M];
	            nodeConnectionsCount = new HashMap[M];
	            HashMap<Integer, Community> newInvMap = new HashMap<Integer, Community>();
	            for (int i = 0; i < communities.size(); i++) {//Community com : mCommunities) {
	                Community com = communities.get(i);
	                nodeConnectionsWeight[index] = new HashMap<Community, Float>();
	                nodeConnectionsCount[index] = new HashMap<Community, Integer>();
	                newTopology[index] = new LinkedList<ModEdge>();
	                nodeCommunities[index] = new Community(com);
	                Set<Community> iter = com.connectionsWeight.keySet();
	                double weightSum = 0;

	                Community hidden = new Community(structure);
	                for (Integer nodeInt : com.nodes) {
	                    Community oldHidden = invMap.get(nodeInt);
	                    hidden.nodes.addAll(oldHidden.nodes);
	                }
	                newInvMap.put(index, hidden);
	                for (Modularity.Community adjCom : iter) {
	                    int target = communities.indexOf(adjCom);
	                    float weight = com.connectionsWeight.get(adjCom);
	                    if (target == index) {
	                        weightSum += 2. * weight;
	                    } else {
	                        weightSum += weight;
	                    }
	                    ModEdge e = new ModEdge(index, target, weight);
	                    newTopology[index].add(e);
	                }
	                weights[index] = weightSum;
	                nodeCommunities[index].seed(index);

	                index++;
	            }
	            communities.clear();

	            for (int i = 0; i < M; i++) {
	                Community com = nodeCommunities[i];
	                communities.add(com);
	                for (ModEdge e : newTopology[i]) {
	                    nodeConnectionsWeight[i].put(nodeCommunities[e.target], e.weight);
	                    nodeConnectionsCount[i].put(nodeCommunities[e.target], 1);
	                    com.connectionsWeight.put(nodeCommunities[e.target], e.weight);
	                    com.connectionsCount.put(nodeCommunities[e.target], 1);
	                }

	            }

	            N = M;
	            topology = newTopology;
	            invMap = newInvMap;
	        }
	    }

	    class Community {

	        double weightSum;
	        CommunityStructure structure;
	        LinkedList<Integer> nodes;
	        HashMap<Modularity.Community, Float> connectionsWeight;
	        HashMap<Modularity.Community, Integer> connectionsCount;

	        public int size() {
	            return nodes.size();
	        }

	        public Community(Modularity.Community com) {
	            structure = com.structure;
	            connectionsWeight = new HashMap<Modularity.Community, Float>();
	            connectionsCount = new HashMap<Modularity.Community, Integer>();
	            nodes = new LinkedList<Integer>();
	            //mHidden = pCom.mHidden;
	        }

	        public Community(CommunityStructure structure) {
	            this.structure = structure;
	            connectionsWeight = new HashMap<Modularity.Community, Float>();
	            connectionsCount = new HashMap<Modularity.Community, Integer>();
	            nodes = new LinkedList<Integer>();
	        }

	        public void seed(int node) {
	            nodes.add(node);
	            weightSum += structure.weights[node];
	        }

	        public boolean add(int node) {
	            nodes.addLast(new Integer(node));
	            weightSum += structure.weights[node];
	            return true;
	        }

	        public boolean remove(int node) {
	            boolean result = nodes.remove(new Integer(node));
	            weightSum -= structure.weights[node];
	            if (nodes.size() == 0) {
	                structure.communities.remove(this);
	            }
	            return result;
	        }
	    }


	    public void execute() {
	        isCanceled = false;

	        graph.readLock();

	        structure = new Modularity.CommunityStructure(graph);
	        int[] comStructure = new int[graph.getNodeCount()];

	        HashMap<String, Double> computedModularityMetrics = computeModularity(graph, structure, comStructure, resolution, isRandomized, useWeight);

	        modularity = computedModularityMetrics.get("modularity");
	        modularityResolution = computedModularityMetrics.get("modularityResolution");

	        saveValues(comStructure, graph, structure);

	        graph.readUnlock();
	    }

	    protected HashMap<String, Double> computeModularity(PyGraph hgraph, CommunityStructure theStructure, int[] comStructure,
	            double currentResolution, boolean randomized, boolean weighted) {
	        isCanceled = false;

	        Random rand = new Random();

	        double totalWeight = theStructure.graphWeightSum;
	        double[] nodeDegrees = theStructure.weights.clone();

	        HashMap<String, Double> results = new HashMap<String, Double>();

	        if (isCanceled) {
	            hgraph.readUnlock();
	            return results;
	        }
	        boolean someChange = true;
	        while (someChange) {
	            someChange = false;
	            boolean localChange = true;
	            while (localChange) {
	                localChange = false;
	                int start = 0;
	                if (randomized) {
	                    start = Math.abs(rand.nextInt()) % theStructure.N;
	                }
	                int step = 0;
	                for (int i = start; step < theStructure.N; i = (i + 1) % theStructure.N) {
	                    step++;
	                    Community bestCommunity = updateBestCommunity(theStructure, i, currentResolution);
	                    if ((theStructure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
	                        theStructure.moveNodeTo(i, bestCommunity);
	                        localChange = true;
	                    }
	                    if (isCanceled) {
	                        hgraph.readUnlock();
	                        return results;
	                    }
	                }
	                someChange = localChange || someChange;
	                if (isCanceled) {
	                    hgraph.readUnlock();
	                    return results;
	                }
	            }

	            if (someChange) {
	                theStructure.zoomOut();
	            }
	        }

	        fillComStructure(hgraph, theStructure, comStructure);
	        double[] degreeCount = fillDegreeCount(hgraph, theStructure, comStructure, nodeDegrees, weighted);

	        double computedModularity = finalQ(comStructure, degreeCount, hgraph, theStructure, totalWeight, 1., weighted);
	        double computedModularityResolution = finalQ(comStructure, degreeCount, hgraph, theStructure, totalWeight, currentResolution, weighted);

	        results.put("modularity", computedModularity);
	        results.put("modularityResolution", computedModularityResolution);

	        return results;
	    }

	    Community updateBestCommunity(CommunityStructure theStructure, int i, double currentResolution) {
	        double best = 0.;
	        Community bestCommunity = null;
	        Set<Community> iter = theStructure.nodeConnectionsWeight[i].keySet();
	        for (Community com : iter) {
	            double qValue = q(i, com, theStructure, currentResolution);
	            if (qValue > best) {
	                best = qValue;
	                bestCommunity = com;
	            }
	        }
	        return bestCommunity;
	    }

	    int[] fillComStructure(PyGraph hgraph, CommunityStructure theStructure, int[] comStructure) {
//	        int[] comStructure = new int[hgraph.getNodeCount()];
	        int count = 0;

	        for (Community com : theStructure.communities) {
	            for (Integer node : com.nodes) {
	                Community hidden = theStructure.invMap.get(node);
	                for (Integer nodeInt : hidden.nodes) {
	                    comStructure[nodeInt] = count;
	                }
	            }
	            count++;
	        }
	        return comStructure;
	    }

	    double[] fillDegreeCount(PyGraph hgraph, CommunityStructure theStructure, int[] comStructure, double[] nodeDegrees, boolean weighted) {
	        double[] degreeCount = new double[theStructure.communities.size()];

	        for (GNode node : hgraph.getNodes()) {
	            int index = theStructure.map.get(node);
	            if (weighted) {
	                degreeCount[comStructure[index]] += nodeDegrees[index];
	            } else {
	                degreeCount[comStructure[index]] += hgraph.getDegree(node);
	            }

	        }
	        return degreeCount;
	    }

	    private double finalQ(int[] struct, double[] degrees, PyGraph hgraph,
	            CommunityStructure theStructure, double totalWeight, double usedResolution, boolean weighted) {

	        double res = 0;
	        double[] internal = new double[degrees.length];
	        for (GNode n : hgraph.getNodes()) {
	            int n_index = theStructure.map.get(n);
	            for (GNode neighbor : hgraph.getNeighbors(n)) {
	                if (n == neighbor) {
	                    continue;
	                }
	                int neigh_index = theStructure.map.get(neighbor);
	                if (struct[neigh_index] == struct[n_index]) {
	                    if (weighted) {
	                        internal[struct[neigh_index]] += hgraph.getEdge(n.getId(), neighbor.getId()).getWeight();
	                    } else {
	                        internal[struct[neigh_index]]++;
	                    }
	                }
	            }
	        }
	        for (int i = 0; i < degrees.length; i++) {
	            internal[i] /= 2.0;
	            res += usedResolution * (internal[i] / totalWeight) - Math.pow(degrees[i] / (2 * totalWeight), 2);//HERE
	        }
	        return res;
	    }

	    private void saveValues(int[] struct, PyGraph hgraph, CommunityStructure theStructure) {
	        for (GNode n : hgraph.getNodes()) {
	            int n_index = theStructure.map.get(n);;
	            n.setValue(MODULARITY_CLASS, struct[n_index]);
	        }
	    }

	    public double getModularity() {
	        return modularity;
	    }

	    private double q(int node, Community community, CommunityStructure theStructure, double currentResolution) {
	        Float edgesToFloat = theStructure.nodeConnectionsWeight[node].get(community);
	        double edgesTo = 0;
	        if (edgesToFloat != null) {
	            edgesTo = edgesToFloat.doubleValue();
	        }
	        double weightSum = community.weightSum;
	        double nodeWeight = theStructure.weights[node];
	        double qValue = currentResolution * edgesTo - (nodeWeight * weightSum) / (2.0 * theStructure.graphWeightSum);
	        if ((theStructure.nodeCommunities[node] == community) && (theStructure.nodeCommunities[node].size() > 1)) {
	            qValue = currentResolution * edgesTo - (nodeWeight * (weightSum - nodeWeight)) / (2.0 * theStructure.graphWeightSum);
	        }
	        if ((theStructure.nodeCommunities[node] == community) && (theStructure.nodeCommunities[node].size() == 1)) {
	            qValue = 0.;
	        }
	        return qValue;
	    }

		@Override
		public void setGraph(PyGraph g) {
			// TODO Auto-generated method stub
			this.graph = g;
		}
	}
