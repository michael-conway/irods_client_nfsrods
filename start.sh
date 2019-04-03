#! /bin/bash

cd irods_client_nfsrods
git checkout $GIT_BRANCH
mvn clean install -Dmaven.test.skip=true

mkdir _package
cd _package
cmake -GNinja /irods_client_nfsrods
cpack -G "DEB"
dpkg -i irods*.deb

# Create users.
newusers /nfsrods_ext/users.txt

# Externally mounted (-v) config files.
export NFSRODS_HOME=/nfsrods_ext

# Run NFS server.
java -jar /opt/irods-clients/nfsrods-1.0.0-SNAPSHOT-jar-with-dependencies.jar
