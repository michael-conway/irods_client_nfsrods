#! /bin/bash

# Create users.
./nfsrods_ext/add_users.sh

# Externally mounted (-v) config files.
export NFSRODS_HOME=/etc/nfsrods-ext

# Run NFS server.
java -jar /opt/irods-clients/nfsrods-1.0.0-SNAPSHOT-jar-with-dependencies.jar
