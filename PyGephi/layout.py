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
        return JProxy("org.pygephi.layout.GLayout", inst=sub)


class CircleLayout(Layout):

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
        pass