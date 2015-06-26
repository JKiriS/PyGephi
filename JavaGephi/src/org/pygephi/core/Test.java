package org.pygephi.core;

import java.util.Random;

import org.pygephi.layout.FALayout;
import org.pygephi.layout.FRLayout;
import org.pygephi.layout.LayoutTask;
import org.pygephi.layout.SLayout;
import org.pygephi.statistics.ClusteringCoefficient;
import org.pygephi.statistics.Modularity;
import org.pygephi.statistics.PageRank;

public class Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		PyGraph g = new PyGraph(PyGraph.UNDIRECTED);
//		g.show();
		Random random = new Random(1111);
		for(int i=0; i<100; i++){
			GNode gn = g.addNode(Integer.toString(i));
			gn.setSize(10.0f);
			gn.setColor(random.nextFloat(), random.nextFloat(), random.nextFloat());
			g.refresh();
		}
		for(int i=0; i<500; i++){
			int s = random.nextInt(100);
			int t = random.nextInt(100);
			g.addEdge(Integer.toString(s), Integer.toString(t));
			g.refresh();
		}
		g.show();
		
		GLongTaskExecutor glte = g.layout(new SLayout(2));
		g.preview(GPreviewProperty.EDGE_CURVED, GPreviewProperty.newBool(true));
//		GLongTaskExecutor glte1 = g.layout(new FALayout(), "FALayout");
//		Thread.sleep(5000);
//		print(glte.pause());
//		Thread.sleep(5000);
//		print(glte.resume());
//		Thread.sleep(5000);
//		glte.cancel();
//		print(glte.slow(2));
//		Thread.sleep(5000);
//		print(glte.slow(0));
//		glte.cancel();
		g.show(true);
//		g.save("e:/test.gephi");
//		g.load("e:/pygephi/Graph.csv");
//		for(GNode gn : g.getNodes()){
//			System.out.println(gn.getId());
//		}
//		g.removeEdge("0", "48");
//		PageRank st = new PageRank();
//		ClusteringCoefficient st = new ClusteringCoefficient();
//		st.setDirected(false);
//		g.calStat(st);
//		for(Object s : g.getNodeAttrs("clustering"))
//			print(s);
//		g.partNodeColor("pageranks");
//		g.show();
//		for(GNode gn : g.getNodes())
//			print(gn.getId());
//		for(GEdge ge : g.getEdges())
//			print(ge.getId());
//		g.addNode("222");
//		g.addEdge("99", "100");
//		g.addEdge("0", "100");
//		g.removeEdge("0", "100");
//		g.removeEdge("0", "1111");
//		print(g.getEdgeCount());
//		g.removeNode("4");
//		print(g.getEdgeCount());
//		g.getAdjMat();
//		print(g.getEdge("1", "10").getWeight());
//		print(g.getEdge("0", "100").getWeight());
//		g.modularity();
//		g.partNode("modularity");
//		g.yFHLayout(1);
//		g.modularity();
//		g.getNode("2").setValue("age", 21);
//		print(g.getNode("2").getValue("x"));
//		g.partNode("modularity");
//		g.scaleLayout(3, 2);
//		g.preview();
//		for(int i=100; i<200; i++){
//			GNode gn = g.addNode(Integer.toString(i));
////			gn.setColor(random.nextFloat(), random.nextFloat(), random.nextFloat());
//			g.refresh();
//		}
//		for(int i=0; i<500; i++){
//			int s = random.nextInt(100);
//			int t = random.nextInt(100);
//			if(s != t){
//				g.addEdge(Integer.toString(s+100), Integer.toString(t+100));
//				g.refresh();
//			}
//		}
//		ClusteringCoefficient st = new ClusteringCoefficient();
////		PageRank st = new PageRank();
//		st.setDirected(true);
//		g.calStat(st);
////		g.partNodeColor("clustering");
//		for(String s : g.getNodeAttrLabels())
//			print(s);
//		for(Object o : g.getNodeAttrs("clustering"))
//			print(o);
//		g.show();
//		g.export("simple.gexf");
//		g.layout(new FRLayout(), 20);
//		Thread.sleep(5000);
//		g.cancelLayout();
//		g.layout(new SLayout(2));
//		SLayout slayout = new SLayout(2);
//		g.layout(slayout);
//		g.show();
//		g.layout(new FRLayout(), 5);
//		g.fRLayout(3);
//		g.save("simple.gephi");
//		PyGraph g1 = PyGraph.open("E:/test.gephi");
	}
	
	public static void print(Object o){
		System.out.println(o);
	}

}
