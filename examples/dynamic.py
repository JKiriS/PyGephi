# -*- coding: utf-8 -*-

import os, sys
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

import time
import random
from PyGephi.graph import LongTask
from PyGephi.generate import NWGraph


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

def test():
    # generate NW network
    g = NWGraph(100, k=6, p=.1)
    g.show(False)

    # init the model running executer
    t = g.execute(SIR(), True, 'SIR', 0.)

    # slow the model run speed
    t.slow(10)

    # start the model
    t.start()
    time.sleep(3)

    # pause the model
    t.pause()
    time.sleep(3)

    # resume the model running
    t.resume()

    # show and block forever
    g.show()

if __name__ == '__main__':
    test()