FROM ubuntu:16.04

RUN apt-get update && \
    apt-get install -y apt-transport-https maven git openjdk-8-jdk \
                       cmake ninja-build less vim wget lsb-release \
                       gcc g++ python

RUN wget -qO - https://packages.irods.org/irods-signing-key.asc | apt-key add -; \
    echo "deb [arch=amd64] https://packages.irods.org/apt/ $(lsb_release -sc) main" | tee /etc/apt/sources.list.d/renci-irods.list; \
    apt-get update && \
    apt-get install -y irods-dev irods-externals-cmake3.11.4-0
RUN export PATH=/opt/irods-externals/cmake3.11.4-0/bin:$PATH

ARG _github_acct="korydraughn"
RUN git clone https://github.com/${_github_acct}/irods_client_nfsrods

ARG _branch="no_krb5_os"
RUN cd irods_client_nfsrods && \
    git checkout ${_branch} && \
    mvn clean install -Dmaven.test.skip=true

RUN mkdir _package && \
    cd _package && \
    cmake /irods_client_nfsrods && \
    cpack -G "DEB" && \
    dpkg -i irods*.deb

ADD start.sh /
RUN chmod +x start.sh

ENTRYPOINT ["./start.sh"]
