# -*- coding: utf-8 -*-

from jpype import JPackage, JProxy
from util import isPyInteractive
import time

PyGraph = JPackage('org').pygephi.core.PyGraph
Property = JPackage('org').pygephi.core.GPreviewProperty


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
                    pass

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

    def show(self, block=None):
        if isPyInteractive:
            self.g.show()
        elif block is None or block == True:
            self.g.show(True)
        else:
            self.g.show()


class LongTask(object):
    
    def __new__(cls, *args, **kwargs):
        sub = object.__new__(cls)
        if hasattr(sub, '__init__'):
            sub.__init__( *args, **kwargs)
        for method in dir(LongTask):
            if not method.startswith('__') and not hasattr(sub, method):
                try:
                    setattr(sub, getattr(sub, method))
                except:
                    pass
        return JProxy("org.pygephi.core.GLongTask", inst=sub)

    def __init__(self):
        pass

    def init(self):
        pass

    def canGo(self):
        return False

    def go(self):
        pass 

    def end(self):
        pass

    def setGraph(self, g):
        self.graph = g
