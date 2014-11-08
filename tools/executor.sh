#!/bin/sh
cpcachef=elnetw-core/target/.classpath.cache
if [[ $OSTYPE == "cygwin" || $OSTYPE == "msys" ]]; then
	PATHSEP=";"
else
	PATHSEP=":"
fi

test -d elnetw-core/target/bin || mvn install
test -f ${cpcachef} || ( \
	cd elnetw-core; \
	mvn dependency:build-classpath -Dmdep.outputFile=../${cpcachef}; )

java -Delnetw.home=tools -cp elnetw-core/target/test${PATHSEP}elnetw-core/target/bin${PATHSEP}$(cat ${cpcachef}) $@
