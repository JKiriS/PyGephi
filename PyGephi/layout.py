# -*- coding: utf-8 -*-

from jpype import JPackage, JProxy
import random

FRLayout = JPackage('org').pygephi.layout.FRLayout
FALayout = JPackage('org').pygephi.layout.FALayout
SLayout = JPackage('org').pygephi.layout.SLayout


class Layout(object):
    
    def __new__(cls, *args, **kwargs):
        sub = object.__new__(cls)
        if hasattr(sub, '__init__'):
            sub.__init__( *args, **kwargs)
        for method in dir(Layout):
            if not method.startswith('__') and not hasattr(sub, method):
                try:
                    setattr(sub, getattr(sub, method))
                except:
                    pass
        return JProxy("org.pygephi.layout.GLayout", inst=sub)

    def __init__(self):
        pass

    def initAlgo(self):
        pass

    def canAlgo(self):
        return False

    def goAlgo(self):
        pass 

    def endAlgo(self):
        pass

    def setGraph(self, g):
        self.graph = g

    def getGraph(self):
        return self.graph

    def resetPropertiesValues(self):
        pass


class CircleLayout(Layout):

    def __init__(self):
        self.resetPropertiesValues()
        self.converged = False

    def canAlgo(self):
        return not self.converged

    def goAlgo(self):
        node_count = self.graph.getNodeCount()
        if node_count <= 0:
            self.converged = True
            return
        r = node_count * 5.
        import math
        each = 360. / node_count 
        for i, n in enumerate(self.graph.getNodes()):
            n.setX(r * math.cos(math.radians(each * i)))
            n.setY(r * math.sin(math.radians(each * i)))
        self.converged = True

    def endAlgo(self):
        from graph import Property
        self.graph.preview(Property.EDGE_CURVED, Property.newBool(True))
        self.graph.preview(Property.EDGE_RADIUS, 10.0)

