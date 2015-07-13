# -*- coding: utf-8 -*-

import jpype
import os, sys

reload(sys)
sys.setdefaultencoding('utf-8')

jvm_path = 'C:/Program Files/Java/jre7/bin/server/jvm.dll'

class_path = os.path.join(os.path.dirname(__file__), 'jarlib/gephi-toolkit.jar') + ";" + \
	os.path.join(os.path.dirname(__file__), 'jarlib/pygephi.jar')

jpype.startJVM(jvm_path, '-Djava.class.path='+class_path)
