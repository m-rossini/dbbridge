#! /bin/sh 

echo
echo \#\#
echo \#\# Copyright 2004-2005 Auster Solutions do Brasil
echo \#\#
echo \#\#  --- Rules Repository Import Tool v1.0.0 ---
echo \#\#
echo

function checkAnt {
	if [[ -z "${ANT_HOME}" || ! ( -r ${ANT_HOME}/lib/ant.jar ) ]]; then
		echo ANT_HOME is set incorrectly or Ant could not be located. Please set ANT_HOME.
		exit 1
	fi
}

function checkJava {
	if [[ -z "${JAVA_HOME}" || ! ( -r ${JAVA_HOME}/bin/java ) ]]; then
		echo JAVA_HOME is set incorrectly or java could not be located. Please set JAVA_HOME.
		exit 1
	fi
}


checkAnt
checkJava


MY_PATH=$( dirname $0 )
cd $MY_PATH
BASEDIR=${PWD%%"/bin"}


$ANT_HOME/bin/ant -Dbasedir=$BASEDIR -Dcmdline.args="$*" -f import-run.xml

echo
echo [ Finished ]
echo
