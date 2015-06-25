# -*- coding: utf-8 -*-

from jpype import *
import time
import random
import sys

reload(sys)
sys.setdefaultencoding('utf-8')

startJVM(getDefaultJVMPath(), '-Djava.class.path=./gephi-toolkit.jar')
PyGraph = JPackage('org').pygephi.core.PyGraph
FRLayout = JPackage('org').pygephi.layout.FRLayout
Modularity = JPackage('org').gephi.statistics.plugin.Modularity


def init(self, target):
	for key in dir(target):
		if key not in dir(self):
			try:
				setattr(self, key, getattr(target, key))
			except:
				pass

class Graph(object):
	def __init__(self, t=PyGraph.UNDIRECTED, autorefresh=False):
		self.g = PyGraph(t)
		self.autorefresh = autorefresh
		def _autorefreshdeco(func):
			def _wrap(*args, **kwargs):
				print self.autorefresh
				func(*args, **kwargs)
				if self.autorefresh:
					print self.autorefresh
					# self.g.refresh()
			return _wrap
		for key in dir(self.g):
			if key not in dir(self):
				try:
					attr = getattr(self.g, key)
					setattr(self, key, attr)
				except Exception, e:
					print e
	def addNode(self, nid, size=10.):
		node = self.g.addNode(str(nid), float(size))
		return node
	def addEdge(self, sid, tid, weight=1.):
		edge = self.g.addEdge(str(sid), str(tid), weight)
		return edge
	def getEdge(self, sid, tid):
		return self.g.getEdge(str(sid), str(tid))
	def getAdjMat(self):
		ja = self.g.getAdjMat()
		return list(map(list, ja))
	def getNodeAttrs(self, label):
		return list(self.g.getNodeAttrs(label))
	def setAutoRefresh(self, autorefresh):
		self.autorefresh = autorefresh

# g = Graph()
# g.addNode('高洋波')
# g.addNode(u'高洋')
# g.addEdge('高洋波', u'高洋')
# g.show()
# time.sleep(1000)

def randomGraph(nodes, edges):
	g = Graph()
	if nodes * (nodes - 1) < edges * 2:
		raise Exception('error input')
	for i in range(nodes):
		g.addNode(i)
	while edges > 0:
		j, k = random.randint(0, nodes-1), random.randint(0, nodes-1)
		if not g.getEdge(j, k) and j != k:
			g.addEdge(j, k)
			edges -= 1
	return g

def teststr():
	g = Graph()
	g.addNode('高洋波').setValue('age', 25.2)
	g.addNode(u'高洋').setValue('age', 25.1)
	g.addEdge('高洋波', u'高洋')
	# g.show()
	print g.getAdjMat()
	print g.getNodeAttrs('age')
	# time.sleep(1000)
# g = genGraph()
# print g.adj()
# teststr()
# print genGraph().getAdjMat()
# print sys.getdefaultencoding()
# '高洋波'.decode('utf-8')

class Stat(object):
	def __new__(cls, *args, **kwargs):
		sub = object.__new__(cls)
		if hasattr(sub, '__init__'):
			sub.__init__( *args, **kwargs)
		return JProxy("org.pygephi.statistics.Stat", inst=sub)


class Degree(Stat):
	def __init__(self):
		pass
	def cancel(self):
		pass
	def setGraph(self, g):
		self.graph = g
	def execute(self):
		import numpy as np
		adj = np.array(self.graph.getAdjMat())
		degrees = np.sum(adj, axis=1) - 1
		for n, d in zip(self.graph.getNodes(), degrees):
			n.setValue('degrees', d)


class GLayout(object):
	def __new__(cls, *args, **kwargs):
		sub = object.__new__(cls)
		if hasattr(sub, '__init__'):
			sub.__init__( *args, **kwargs)
		return JProxy("org.pygephi.layout.GLayout", inst=sub)


class TestLayout(GLayout):
	def __init__(self):
		self.resetPropertiesValues()
		self.converged = False
	def initAlgo(self):
		pass
	def canAlgo(self):
		return not self.converged
	def goAlgo(self):
		node_count = self.graph.getNodeCount()
		if node_count <= 0:
			return
		r = node_count * 5.
		import math
		each = 360. / node_count 
		for i, n in enumerate(self.graph.getNodes()):
			n.setX(r * math.cos(math.radians(each * i)))
			n.setY(r * math.sin(math.radians(each * i)))
		self.converged = True			
	def endAlgo(self):
		pass
	def setGraph(self, g):
		self.graph = g
	def resetPropertiesValues(self):
		self.dx = self.dy = 10


class KKernel(Stat):
	def __init__(self):
		pass
	def cancel(self):
		pass
	def setGraph(self, g):
		self.graph = g
	def execute(self):
		import numpy as np
		adj = np.array(self.graph.getAdjMat())
		adj[adj > 1] = 1
		N = adj.shape[1]
		adj -= np.eye(N)
		kernel = np.zeros(N)
		for k in range(1, N+1):
			while True:
				degree = np.sum(adj, axis=1)
				if not np.any(np.logical_and(degree <= k, kernel == 0)):
					break
				kernel[np.logical_and(degree <= k, kernel == 0)] = k
				adj[:, degree <= k] = 0
			if not np.any(kernel == 0):
				break
		for n, d in zip(self.graph.getNodes(), kernel):
			n.setValue('kkernel', d)


random.seed(111111)
# tl = TestLayout()
g = randomGraph(200, 10)
# g.show()
g.setAutoRefresh(True)
# print g.getAdjMat()
# g.calStat(KKernel())
# g.layout(FRLayout())
for i in range(10):
	j, k = random.randint(0, 199), random.randint(0, 199)
	if j != k:
		g.addEdge(j, k)
		time.sleep(.5)
# g.partNodeColor('kkernel')
# g.save('simple.gephi')
# g.show()
# time.sleep(100)
def degreeDistribution(g, log_log=False):
	import numpy as np
	dgs = {}
	for n in g.getNodes():
		d = g.getDegree(n)
		dgs[d] = dgs.get(d, 0) + 1
	xy = sorted(dgs.items(), key=lambda a:a[0])
	x = np.array([i[0] for i in xy])
	y = np.array([i[1] for i in xy])
	if log_log:
		x = np.log10(x)
		y = np.log10(y)
	import matplotlib.pyplot as plt
	plt.plot(x, y, 'o')
	plt.show()
# g = Graph()
# g.load('wm.gexf')
# g.calStat(KKernel())
# g.save('wm.gephi')
# degreeDistribution(g, True)