# -*- coding: utf-8 -*-

import random
from PyGephi.generate import NWGraph
# from PyGephi.stat import KKernel
# from PyGephi.layout import CircleLayout
from PyGephi.ext import degreeDistri

# random.seed(111111)
# g = randomGraph(200, 1000)
# g.show(False)
# g.calStat(KKernel())
# g.layout(FRLayout())
# g.partNodeColor('kkernel')
# # g.save('simple.gephi')
# # degreeDistri(g)
# g.show()

g = NWGraph(1000, k=10, p=.2)
degreeDistri(g)
# g.layout(CircleLayout())
# g.show()