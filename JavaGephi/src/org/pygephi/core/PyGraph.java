package org.pygephi.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JFrame;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.pygephi.layout.GLayout;
import org.pygephi.layout.LayoutTask;
import org.pygephi.statistics.Stat;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.graph.api.Graph;


import processing.core.PApplet;

public class PyGraph {
	
	ProjectController pc;
	Workspace workspace;
	
	GraphModel graphModel;
	Graph graph;
	
	JFrame frame;
	ProcessingTarget target;
	PreviewController previewController;
	PreviewModel model;
	AttributeModel attributeModel;
	PartitionController partitionController;
	
	HashMap<String, GLongTaskExecutor> longTasks;
	
	HashMap<String, GNode> nodes = new HashMap<String, GNode>();
	HashMap<Integer, GEdge> edges = new HashMap<Integer, GEdge>();
	
	float defaultnodesize = 10f;
	float defaultedgeweight = 1f;
	
	public static final String UNDIRECTED = "undirected";
	public static final String DIRECTED = "directed";
	
	public boolean isdirected = false;
	
	public PyGraph(String type) throws Exception{
		longTasks = new HashMap<String, GLongTaskExecutor>();
		
		pc = Lookup.getDefault().lookup(ProjectController.class);
		if(pc.getCurrentProject() == null)
			pc.newProject();
		workspace = pc.getCurrentWorkspace();

		if(type.equals(DIRECTED)){
			isdirected = true;
		}
		refreshGraphModel();
	}
	
	public PyGraph() throws Exception{
		this(UNDIRECTED);
	}
	
	public void refreshGraphModel(){
		graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		if(isdirected)
			graph = graphModel.getDirectedGraph();
		else
			graph = graphModel.getUndirectedGraph();
	}
	
	public GNode addNode(String id, String label, float size){
		GNode gn;
		gn = getNode(id);
		if(gn == null){
			Node n = graphModel.factory().newNode(id);
			graph.addNode(n);
			gn = new GNode(n);
			gn.setLabel(label);
			gn.setSize(size);
			nodes.put(gn.getId(), gn);
		}
		return gn;
	}
	
	public GNode addNode(){
		Node n = graphModel.factory().newNode();
		graph.addNode(n);
		GNode gn = new GNode(n);
		gn.setSize(defaultnodesize);
		nodes.put(gn.getId(), gn);
		return gn;
	}
	
	public GNode addNode(String id){
		return addNode(id, id, defaultnodesize);
	}
	
	public GNode addNode(String id, float size){
		return addNode(id, id, size);
	}
	
	public GNode getNode(String id){
		Node n = graph.getNode(id);
		if(n != null)
			return nodes.get(n.getNodeData().getId());
		return null;
	}
	
	public GNode[] getNodes(){
		int node_num = graph.getNodeCount();
		GNode[] ns = new GNode[node_num];
		int i = 0;
		for(Node n : graph.getNodes()){
			ns[i++] = nodes.get(n.getNodeData().getId());
		}
		return ns;
	}
	
	public void removeNode(String id){
		Node n = graph.getNode(id);
		if(n != null && graph.removeNode(n)){
			nodes.remove(n.getNodeData().getId());
			syncEdgeMap();
		}
	}
	
	public void setDefaultNodeSize(float s){
		defaultnodesize = s;
	}
	
	public GEdge addEdge(String s, String t, float weight){
		GEdge ge = getEdge(s, t);
		if(ge == null){
			GNode sn = addNode(s);
			GNode tn = addNode(t);
			Edge e = graphModel.factory().newEdge(sn.getNode(), tn.getNode());
			e.setWeight(weight);
			graph.addEdge(e);
			ge = new GEdge(e);
			edges.put(ge.getId(), ge);
		}
		return ge;
	}
	
	public GEdge addEdge(String s, String t){
		return addEdge(s, t, defaultedgeweight);
	}
	
	public void setDefaultEdgeWeight(float w){
		defaultedgeweight = w;
	}
	
	public void removeEdge(String sid, String tid){
		GEdge ge = getEdge(sid, tid);
		if(ge != null){
			graph.removeEdge(ge.e);
			edges.remove(ge.getId());
		}
	}
	
	public GEdge getEdge(int id){
		Edge e = graph.getEdge(id);
		if(e != null)
			return edges.get(e.getId());
		return null;
	}
	
	public GEdge getEdge(String sid, String tid){
		Edge e = graph.getEdge(graph.getNode(sid), graph.getNode(tid));
		if(e != null)
			return edges.get(e.getId());
		return null;
	}
	
	public GEdge[] getEdges(){
		int edge_num = graph.getEdgeCount();
		GEdge[] es = new GEdge[edge_num];
		int i = 0;
		for(Edge e : graph.getEdges()){
			es[i++] = edges.get(e.getId());
		}
		return es;
	}
	
	public GEdge[] getEdges(GNode s) {
		Edge[] es = graph.getEdges(s.getNode()).toArray();
		GEdge[] ges = new GEdge[es.length];
		int i = 0;
		for(Edge e : es){
			ges[i++] = edges.get(e.getId());
		}
		return ges;
	}
	
	public void syncNodeMap(){
		HashMap<String, GNode> newns = new HashMap<String, GNode>();
//		HashMap<Integer, GEdge> edges = new HashMap<Integer, GEdge>();
		String id;
		GNode gn;
		for(Node n : graph.getNodes()){
			id = n.getNodeData().getId();
			gn = nodes.get(id);
			if(gn == null){
				gn = new GNode(n);
			}
			newns.put(id, gn);
		}
		nodes = newns;
	}
	
	public void syncEdgeMap(){
		HashMap<Integer, GEdge> newes = new HashMap<Integer, GEdge>();
		int id;
		GEdge ge;
		for(Edge e : graph.getEdges()){
			id = e.getId();
			ge = edges.get(id);
			if(ge == null){
				ge = new GEdge(e);
			}
			newes.put(id, ge);
		}
		edges = newes;
	}
	
	public int getNodeCount(){
		return graph.getNodeCount();
	}
	
	public int getEdgeCount(){
		return graph.getEdgeCount();
	}
	
	public int getDegree(GNode gn){
		return graph.getDegree(gn.n);
	}
	
	public GNode[] getNeighbors(GNode gn){
		Node[] ns = graph.getNeighbors(gn.n).toArray();
		GNode[] neighbors = new GNode[ns.length];
		for(int i=0; i < ns.length; i++)
			neighbors[i] = nodes.get(ns[i].getNodeData().getId());
		return neighbors;
	}

	public GNode getOpposite(GNode s, GEdge e) {
		Node n = graph.getOpposite(s.getNode(), e.e);
		return nodes.get(n.getNodeData().getId());
	}

	public int getOutDegree(GNode s) {
		return graphModel.getDirectedGraph().getOutDegree(s.getNode());
	}

	public GEdge[] getInEdges(GNode s) {
		Edge[] es = ((DirectedGraph) graph).getInEdges(s.getNode()).toArray();
		GEdge[] ges = new GEdge[es.length];
		for(int i=0;i<es.length;i++){
			ges[i] = edges.get(es[i].getId());
		}
		return ges;
	}

	public GNode[] getPredecessors(GNode n) {
		Node[] ns = ((DirectedGraph) graph).getPredecessors(n.getNode()).toArray();
		GNode[] gns = new GNode[ns.length];
		for(int i=0; i < ns.length; i++)
			gns[i] = nodes.get(ns[i].getNodeData().getId());
		return gns;
	}

	public GEdge[] getOutEdges(GNode s) {
		Edge[] es = ((DirectedGraph) graph).getOutEdges(s.getNode()).toArray();
		GEdge[] ges = new GEdge[es.length];
		for(int i=0;i<es.length;i++){
			ges[i] = edges.get(es[i].getId());
		}
		return ges;
	}
	
	public boolean isAdjacent(String sid, String tid){
		return graph.isAdjacent(graph.getNode(sid), graph.getNode(tid));
	}
	
	public float[][] getAdjMat(){
		int N = getNodeCount();
		float[][] adj = new float[N][N];
		HashMap<String, Integer> ids = new HashMap<String, Integer>();
		int index = 0;
		for(Node n : graph.getNodes()){
			adj[index][index] = 1f;
			ids.put(n.getNodeData().getId(), index++);
		}
		String sid, tid;
		for(Edge e : graph.getEdges()){
			sid = e.getSource().getNodeData().getId();
			tid = e.getTarget().getNodeData().getId();
			adj[ids.get(sid)][ids.get(tid)] = e.getWeight();
			if(!isdirected)
				adj[ids.get(tid)][ids.get(sid)] = e.getWeight();
		}
		return adj;
	}
	
	public void setDirected(boolean directed){
		isdirected = directed;
		if(directed){
			graph = graphModel.getDirectedGraph();
		}
		else{
			graph = graphModel.getUndirectedGraph();
		}
	}
	
	public Graph getDirectedGraph(){
		return graphModel.getDirectedGraph();
	}
	
	public Graph getUnDirectedGraph(){
		return graphModel.getUndirectedGraph();
	}
	
	public GraphModel getGraphModel(){
		return graphModel;
	}
	
	public void clear(){
		graph.clear();
		nodes.clear();
		clearEdges();
	}
	
	public void clearEdges(){
		graph.clearEdges();
		edges.clear();
	}
	
	public String[] getNodeAttrLabels(){
		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		int N = attributeModel.getNodeTable().countColumns();
		String[] attrlabels = new String[N];
		int i = 0;
		for(AttributeColumn ac : attributeModel.getNodeTable().getColumns())
			attrlabels[i++] = ac.getId();
		return attrlabels;
	}
	
	public Object[] getNodeAttrs(String label){
		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		AttributeColumn col = attributeModel.getNodeTable().getColumn(label);
		if(col == null)
			return new Object[0];
		int N = graph.getNodeCount();
		Object[] values = new Object[N];
		int i = 0;
		for (Node n : graphModel.getGraph().getNodes()) {
			values[i++] = n.getNodeData().getAttributes().getValue(col.getIndex());
		}
		return values;
	}
	
	public void partNodeColor(String collabel){
		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		AttributeColumn modColumn = attributeModel.getNodeTable().getColumn(collabel);
		partitionController = Lookup.getDefault().lookup(PartitionController.class);
		Partition p1 = partitionController.buildPartition(modColumn, graph);
        NodeColorTransformer nodeColorTransformer1 = new NodeColorTransformer();
        nodeColorTransformer1.randomizeColors(p1);
        partitionController.transform(p1, nodeColorTransformer1);
        refresh();
	}
	
	public void preview(){
		previewController = Lookup.getDefault().lookup(PreviewController.class);
		model = previewController.getModel();
		
		model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, true);
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
		model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.WHITE));
		model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
		model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
        model.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, new Font("ËÎÌו", Font.PLAIN, 8));
		previewController.refreshPreview();
		refresh();
	}
	
	public void preview(String key, Object value){
		previewController = Lookup.getDefault().lookup(PreviewController.class);
		model = previewController.getModel();
		
		model.getProperties().putValue(key, value);
		previewController.refreshPreview();
		refresh();
	}
	
	public void show(boolean block){
		if(frame != null && frame.isVisible()){
			refresh(true);
			setFrameCloseAction(block);
		}
		else if(frame != null){
			refresh(true);
			frame.setVisible(true);
			setFrameCloseAction(block);
		}
		else if(frame == null){
			initJFrame();
			setFrameCloseAction(block);
		}
		
		
		if(block){
			try {
				Thread.sleep(Integer.MAX_VALUE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void show(){
		if(frame != null && frame.isVisible()){
			refresh(true);
			return ;
		}
		else if(frame != null){
			refresh(true);
			frame.setVisible(true);
			return ;
		}
		
		initJFrame();
		setFrameCloseAction(false);
	}
	
	private void initJFrame(){
		previewController = Lookup.getDefault().lookup(PreviewController.class);
		preview();
		
		target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET); 
		PApplet applet = target.getApplet();
		applet.init();
		
		previewController.render(target);
		refresh(true);
		
		frame = new JFrame("Network Preview");
		frame.setLayout(new BorderLayout());
		frame.add(applet, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
	public JFrame getFrame(){
		return frame;
	}
	
	private void setFrameCloseAction(boolean block){
		if(frame != null){
			if(block)
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			else
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		}
	}
	
	public void refresh(boolean zoom){
		if(previewController != null){
			previewController.refreshPreview();
		}
		if(target != null){
			target.refresh();
			if(zoom)
				target.resetZoom();
		}
	}
	
	public void refresh(){
		refresh(false);
	}
	
	public GLongTaskExecutor execute(final GLongTask task, boolean doInBackground, 
			String taskName, float timeout) throws Exception{
		if(longTasks.containsKey(taskName))
			throw new Exception("task exists");
		GLongTaskExecutor executor = new GLongTaskExecutor(doInBackground, taskName, timeout);
		longTasks.put(taskName, executor);
		executor.setLongTaskPool(longTasks);
		executor.execute(task);	
		return executor;
	}
	
	public GLongTaskExecutor getLongTask(String key){
		return longTasks.get(key);
	}
	
	public GLongTaskExecutor[] getLongTasks(){
		GLongTaskExecutor[] glt = new GLongTaskExecutor[longTasks.size()];
		return longTasks.values().toArray(glt);
	}
	
	public boolean removeLongTask(String key){
		GLongTaskExecutor t = getLongTask(key);
		if(t != null){
			if(t.isRunning()){
				t.cancel();
			}
			longTasks.remove(key);
		}
		return true;
	}
		
	public GLongTaskExecutor layout(final GLayout layout, String taskName) throws Exception{
		layout.setGraph(this);
		GLongTask lt = new LayoutTask(layout);
		return execute(lt, true, taskName, 0);
	}
	
	public GLongTaskExecutor layout(final GLayout layout, float timeout, String taskName) throws Exception{
		layout.setGraph(this);
		GLongTask lt = new LayoutTask(layout);
		return execute(lt, false, taskName, timeout);
	}
	
	public GLongTaskExecutor layout(final GLayout layout) throws Exception{
		return layout(layout, "LayoutTask");
	}
	
	public GLongTaskExecutor layout(final GLayout layout, float timeout) throws Exception{
		return layout(layout, timeout, "LayoutTask");
	}
	
	public void calStat(Stat s){
		s.setGraph(this);
		s.execute();
	}	
	
	public void load(String fname){
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);

		Container container;
		try {
		    File file = new File(fname);
		    container = importController.importFile(file);
		    
		    //Append imported data to GraphAPI
			importController.process(container, new DefaultProcessor(), workspace);
			refreshGraphModel();
			syncNodeMap();
			syncEdgeMap();
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}

	public boolean export(String fname){
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
		   ec.exportFile(new File(fname));
		} catch (IOException ex) {
		   ex.printStackTrace();
		   return false;
		}
		return true;
	}
	
	public void save(String fname) throws Exception{
		if(!fname.endsWith(".gephi")){
			throw new Exception();
		}
		pc = Lookup.getDefault().lookup(ProjectController.class);
		Runnable savetask = pc.saveProject(pc.getCurrentProject(), new File(fname));
		savetask.run();
	}
	
	public static PyGraph open(String fname) throws Exception{
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		if (pc.getCurrentProject() != null) {
			pc.closeCurrentProject();
		}
		Runnable loadtask = pc.openProject(new File(fname));
		loadtask.run();
		return new PyGraph();
	}
	
	public void readLock(){
		graph.readLock();
	}
	
	public void readUnlock(){
		graph.readUnlock();
	}
	
	public void writeLock(){
		graph.writeLock();
	}
	
	public void writeUnlock(){
		graph.writeUnlock();
	}
	
}
