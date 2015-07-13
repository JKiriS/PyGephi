# -*- coding: utf-8 -*-

import os, sys
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from PyGephi.generate import randomGraph
from PyGephi.layout import Layout
import random
import math


class DyCircleLayout(Layout):

    def __init__(self, ratio=50):
        self.resetPropertiesValues()
        self.converged = False
        self.ratio = ratio

    def initAlgo(self):
        from PyGephi.graph import Property
        self.graph.preview(Property.EDGE_CURVED, Property.newBool(True))
        self.graph.preview(Property.EDGE_RADIUS, 10.0)

        for i, n in enumerate(self.graph.getNodes()):
            n.setX(100 * random.random())
            n.setY(100 * random.random())

    def canAlgo(self):
        return not self.converged

    def goAlgo(self):
        node_count = self.graph.getNodeCount()
        if node_count <= 0:
            self.converged = True
            return
        r = node_count * 5.
        
        each = 360. / node_count
        for i, n in enumerate(self.graph.getNodes()):
            targetX = r * math.cos(math.radians(each * i))
            targetY = r * math.sin(math.radians(each * i))
            n.setX(n.x() + (targetX-n.x())/self.ratio)
            n.setY(n.y() + (targetY-n.y())/self.ratio)
            if abs(n.x()-targetX) <= 0.001:
            	self.converged = True

def test():
    g = randomGraph(100, 500)    
    g.show(False)
    t = g.layout(DyCircleLayout())
    t.start()
    g.show()

if __name__ == '__main__':
	test()