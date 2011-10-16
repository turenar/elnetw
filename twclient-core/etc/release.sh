#!/bin/sh
ReleaseDistParent=target/tmp/release
ProjectName=tbotsys
ReleaseDist=${ReleaseDistParent}/${ProjectName}
VersionResourceFile=target/maven-archiver/pom.properties



echo Running maven...
mvn clean assembly:assembly

echo -n "Getting version... "
version=$(grep "version" ${VersionResourceFile} | cut -d"=" -f2-)
echo ${version}
archiveFile=${ProjectName}-${version}-release.zip

if [ ! -e target/tbotsys-dist.jar ];then
	echo Not found target/tbotsys-dist.jar: Build failured? >&2
	exit 1
fi

echo Preparing release package...
mkdir -p ${ReleaseDist}
echo Copying files...
cp -r LICENSE.txt README.txt tbotsys.cfg.tmpl data ${ReleaseDist}
cp target/tbotsys-dist.jar ${ReleaseDist}/tbotsys.jar
workingDir=$(pwd)
cd ${ReleaseDistParent}
find . -type d -name ".svn" | xargs rm -rf

echo Making release package... target/${archiveFile}
zip -rDX ${workingDir}/target/${archiveFile} ${ProjectName}
cd ${workingDir}

echo Cleaning files...
#rm -r ${ReleaseDist}
echo Done.
