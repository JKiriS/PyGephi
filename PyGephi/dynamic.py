# -*- coding: utf-8 -*-

import random
from PyGephi.graph import LongTask


class SIR(LongTask):

    def init(self):
        self.cango = True
        for n in self.graph.getNodes():
            self.suscepte(n)
        for n in random.sample(self.graph.getNodes(), 3):
            self.infect(n)
        self.graph.refresh()

    def canGo(self):
        return self.cango

    def go(self):
        for n in self.graph.getNodes():
            if n.getValue('SIR') == 1:
                for nn in self.graph.getNeighbors(n):
                    if nn.getValue('SIR') == 0 and random.random() < .1:
                        self.infect(nn)
            if n.getValue('SIR') == 1 and random.random() < .1:
                self.recover(n)
            elif n.getValue('SIR') == 0 and random.random() < .05:
                self.infect(n)
        self.graph.refresh()
        if len(filter(lambda n:n.getValue('SIR')==0, self.graph.getNodes())) == 0:
            self.cango = False

    def infect(self, n):
        n.setValue('SIR', 1)
        n.setColor(1., 0., 0.)

    def suscepte(self, n):
        n.setValue('SIR', 0)
        n.setColor(0., 1., 0.)

    def recover(self, n):
        n.setValue('SIR', 2)
        n.setColor(.5, .5, .5)

# g = NWGraph(100, k=6, p=.1)
# g.show(False)
# t = g.execute(SIR(), True, 'SIR', 0.)
# t.slow(10)
# t.start()
# g.show()