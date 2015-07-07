# -*- coding: utf-8 -*-

import pymongo
from PyGephi.graph import Graph
from PyGephi.stat import Modularity
from PyGephi.layout import FRLayout

try:
	client = pymongo.MongoClient("localhost", 27017)
	db = client.renren
	db.authenticate('JKiriS', '910813gyb')
except:
	raise Exception('MongoDB Connection Error')

def genGraph(user_id):
	g = Graph()
	user = db.user.find_one({'_id':user_id})
	nodes = {}
	edges = []
	if user is None:
		raise Exception()
	nodes[user['_id']] = user['name']
	for friend in user.get('friends', []):
		edges.append((user['_id'], friend))
		nodes[friend] = 1
	for u in db.user.find({'_id':{'$in':user.get('friends', [])}}):
		nodes[u['_id']] = u['name']
		for friend in u.get('friends', []):
			if friend in nodes:
				edges.append((u['_id'], friend))
	for s, t in edges:
		g.addEdge(nodes[s], nodes[t])
	return g
g = genGraph('313688805')
g.calStat(Modularity())
g.partNodeColor('modularity')
lt = g.layout(FRLayout(), 3.)
lt.start()
g.show()
# g = Graph()
# g.load('graph.csv')
# g.calStat(Modularity())
# g.calStat(KKernel())
# g.partNodeColor('kkernel')
# g.show()