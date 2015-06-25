# -*- coding: utf-8 -*-

from graph import Graph
import random

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