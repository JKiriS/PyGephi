# -*- coding: utf-8 -*-

from graph import Graph
import random
import numpy as np

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

def NWGraph(nodes, k=4, p=.1):
    N = nodes
    if N <= 2*k or k % 2 != 0:
        raise Exception('error input')
    adj = np.zeros((N, N))
    for i in range(N):
        if i < k/2:
            adj[i, (N+i-k/2)%N:] = 1
            adj[i, :(i+1+k/2)%N] = 1
        elif i > N-k/2-1:
            adj[i, (N+i-k/2)%N:] = 1
            adj[i, :(i+1+k/2)%N] = 1
        else:
            adj[i, (N+i-k/2)%N:(i+1+k/2)%N] = 1
        adj[i,i] = 0
    
    edges = N * k / 2
    def _selectNodes(edges):
        while edges > 0:
            j, k = random.randint(0, N-1), random.randint(0, N-1)
            if j != k:
                yield j, k
                edges -= 1
    for j, k in _selectNodes(edges):
        if random.random() <= p:
            adj[j,k] = adj[k,j] = 1
            
    g = Graph()
    for i in range(N):
        g.addNode(i)
    for i in range(N):
        for j in range(N):
            if adj[i, j] == 1:
                g.addEdge(i, j)
    return g