# **********************************************************************
# 
# <copyright>
# 
#  BBN Technologies, a Verizon Company
#  10 Moulton Street
#  Cambridge, MA 02138
#  (617) 873-8000
# 
#  Copyright (C) BBNT Solutions LLC. All rights reserved.
# 
# </copyright>
# **********************************************************************
# 
# $Source: /cvs/distapps/openmap/Attic/Makefile,v $
# $RCSfile: Makefile,v $
# $Revision: 1.5 $
# $Date: 2003/02/15 04:53:28 $
# $Author: dietrick $
# 
# **********************************************************************


default: all

all:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

classes:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

clean:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

distclean:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

docs:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

clean_docs:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

jar:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

svg:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

j3d:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

visibroker:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

todo:
	cd ${OPENMAP_HOME} && bin/ant -emacs $@

help:
	cd ${OPENMAP_HOME} && bin/ant -emacs -projecthelp


