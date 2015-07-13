# -*- coding: utf-8 -*-

import os, sys
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

from PyGephi.generate import randomGraph
from PyGephi.layout import FRLayout

g = randomGraph(100, 500)
g.show(False)
t = g.layout(FRLayout(), 10.)
t.start()
g.show()