ARG BASE_IMAGE=robsyme/docker-sge

FROM $BASE_IMAGE

ARG JAVA_DISTRO_URL="http://ppa.launchpad.net/openjdk-r/ppa/ubuntu/pool/main/o/openjdk-lts/openjdk-11-jre-headless_11.0.11+9-0ubuntu2~16.04_amd64.deb"

RUN apt-get -y update && \
    curl $JAVA_DISTRO_URL -o /tmp/java_distr.deb && \
    apt-get install -y /tmp/java_distr.deb && \
    rm -rf /tmp/java_distr.deb
COPY ./build/libs /opt

EXPOSE 8080
EXPOSE 5005
WORKDIR /opt

ENV PATH="/opt/sge/bin/lx-amd64:$PATH" \
    SGE_ROOT=/opt/sge

CMD qconf -am sgeuser && \
    runuser -l sgeuser -c "java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 /opt/grid-engine-api-1.0-SNAPSHOT.jar"
