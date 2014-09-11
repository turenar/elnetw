#!/bin/sh
cpcachef=elnetw-core/target/.classpath.cache

test -d elnetw-core/target/bin || mvn install
test -f ${cpcachef} || ( \
	cd elnetw-core; \
	mvn dependency:build-classpath -Dmdep.outputFile=../${cpcachef}; )

java -Delnetw.home=tools -cp elnetw-core/target/test:elnetw-core/target/bin:$(cat ${cpcachef}) $@
