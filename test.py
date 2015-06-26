# -*- coding: utf-8 -*-

import random
from PyGephi.generate import NWGraph
from PyGephi.graph import Property
# from PyGephi.stat import KKernel
from PyGephi.layout import CircleLayout, FRLayout
# from PyGephi.ext import degreeDistri

random.seed(111111)
# g = randomGraph(200, 1000)
# g.show(False)
# g.calStat(KKernel())
# g.layout(CircleLayout())
# g.partNodeColor('kkernel')
# # g.save('simple.gephi')
# # degreeDistri(g)
# g.show()

g = NWGraph(100, k=6, p=.5)
g.show(False)
g.layout(CircleLayout())
g.refresh()
g.show()
