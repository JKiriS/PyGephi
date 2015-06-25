# -*- coding: utf-8 -*-

import jpype
import sys

reload(sys)
sys.setdefaultencoding('utf-8')

jpype.startJVM(jpype.getDefaultJVMPath(),
	'-Djava.class.path=./PyGephi/jarlib/gephi-toolkit.jar;./PyGephi/jarlib/pygephi.jar')
