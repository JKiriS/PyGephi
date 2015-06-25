# -*- coding: utf-8 -*-

from jpype import JPackage, JProxy

Modularity = JPackage('org').gephi.statistics.plugin.Modularity
PageRank = JPackage('org').gephi.statistics.plugin.PageRank


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