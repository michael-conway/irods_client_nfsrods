FROM ubuntu:16.04

RUN apt-get update && \
    apt-get install -y apt-transport-https maven git openjdk-8-jdk \
                       ninja-build less vim wget lsb-release gcc g++ python

RUN wget -qO - https://packages.irods.org/irods-signing-key.asc | apt-key add -; \
    echo "deb [arch=amd64] https://packages.irods.org/apt/ $(lsb_release -sc) main" | tee /etc/apt/sources.list.d/renci-irods.list; \
    apt-get update && \
    apt-get install -y irods-dev irods-externals-cmake3.11.4-0

ENV PATH=/opt/irods-externals/cmake3.11.4-0/bin:$PATH

ARG _github_acct="korydraughn"
ENV GITHUB_ACCT ${_github_acct}

ARG _git_branch="no_krb5_os"
ENV GIT_BRANCH ${_git_branch}

ADD start.sh /
RUN chmod +x start.sh

ENTRYPOINT ["./start.sh"]
