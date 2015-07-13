# -*- coding: utf-8 -*-

import os, sys
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from PyGephi.graph import Graph
import random

# define a Undirect Graph
g = Graph()

#define a Direct Graph
g = Graph(Graph.DIRECTED)

# add Nodes
for i in range(100):
	g.addNode(i)

# show graph without block
g.show(False)

# add Edges
for _ in range(500):
	i, j = random.randint(0, 100), random.randint(0, 100)
	g.addEdge(i, j)

# export this graph with gexf(csv) format
g.export('randomgraph.gexf')
# g.export('randomgraph.csv')

# show and block until graph view window is closed
g.show()