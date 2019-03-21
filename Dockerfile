FROM ubuntu:16.04

RUN apt-get update
RUN apt-get install -y maven openjdk-8-jdk git cmake ninja-build
RUN apt-get install -y apt-transport-https rpm wget lsb-release

# Install irods-dev.
RUN wget -qO - https://packages.irods.org/irods-signing-key.asc | apt-key add -; \
    echo "deb [arch=amd64] https://packages.irods.org/apt/ $(lsb_release -sc) main" | tee /etc/apt/sources.list.d/renci-irods.list; \
    apt-get update && \
    apt-get install -y irods-dev

# Build NFSRODS.
RUN git clone https://github.com/korydraughn/irods_client_nfsrods && \
    cd irods_client_nfsrods && \
    git checkout packaged && \
    mvn clean install -Dmaven.test.skip=true

# Create DEB and RPM packages.
RUN mkdir _build_nfsrods && \
    cd _build_nfsrods && \
    cmake /irods_client_nfsrods && \
    cpack -G "DEB;RPM"; \
    tail -f /dev/null
