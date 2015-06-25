# -*- coding: utf-8 -*-

import random
from PyGephi.generate import randomGraph
from PyGephi.stat import KKernel
from PyGephi.layout import FRLayout
# from PyGephi.ext import degreeDistri

random.seed(111111)
g = randomGraph(200, 1000)
g.show(False)
g.calStat(KKernel())
g.layout(FRLayout())
g.partNodeColor('kkernel')
# g.save('simple.gephi')
# degreeDistri(g)
g.show()
