# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt
from util import isPyInteractive

def degreeDistri(g, log_log=False, block=True):
    dgs = {}
    for n in g.getNodes():
        d = g.getDegree(n)
        dgs[d] = dgs.get(d, 0) + 1
    xy = sorted(dgs.items(), key=lambda a:a[0])
    x = np.array([i[0] for i in xy])
    y = np.array([i[1] for i in xy])
    if log_log:
        x = np.log10(x)
        y = np.log10(y)
    plt.plot(x, y, 'o')

    if isPyInteractive:
        plt.show(block=False)
    else:
        plt.show(block=block)
