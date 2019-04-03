#! /bin/bash

# Create users.
newusers /nfsrods_ext/users.txt

# Externally mounted (-v) config files.
export NFSRODS_HOME=/nfsrods_ext

# Run NFS server.
java -jar /opt/irods-clients/nfsrods-1.0.0-SNAPSHOT-jar-with-dependencies.jar
