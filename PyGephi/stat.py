# -*- coding: utf-8 -*-

from jpype import JPackage, JProxy

Modularity = JPackage('org').pygephi.statistics.Modularity
PageRank = JPackage('org').pygephi.statistics.PageRank


class Stat(object):

    def __new__(cls, *args, **kwargs):
        sub = object.__new__(cls)
        if hasattr(sub, '__init__'):
            sub.__init__( *args, **kwargs)
        for method in dir(Stat):
            if not method.startswith('__') and not hasattr(sub, method):
                try:
                    setattr(sub, getattr(sub, method))
                except:
                    pass
        return JProxy("org.pygephi.statistics.Stat", inst=sub)

    def cancel(self):
        return False

    def setGraph(self, g):
        self.graph = g

    def execute(self):
        pass


class Degree(Stat):

    def execute(self):
        import numpy as np
        adj = np.array(self.graph.getAdjMat())
        degrees = np.sum(adj, axis=1) - 1
        for n, d in zip(self.graph.getNodes(), degrees):
            n.setValue('degrees', d)


class KKernel(Stat):
        
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