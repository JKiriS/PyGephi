# -*- coding: utf-8 -*-

from PyGephi.generate import NWGraph, randomGraph
from PyGephi.stat import Degree, KKernel
from PyGephi.graph import Graph

g = Graph()
g.load('graph.csv')
g.calStat(Degree())
g.calStat(KKernel())
g.partNodeColor('kkernel')
g.show()