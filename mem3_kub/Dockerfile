FROM debian:jessie

MAINTAINER Adam Kocoloski adam@kocolosk.net

RUN groupadd -r couchdb && useradd -d /opt/couchdb -g couchdb couchdb

# Download dependencies
RUN apt-get update -y -qq && apt-get install -y --no-install-recommends \
    python python-pip \
 && pip install requests

COPY mem3_kube.py /opt/mem3_kube/

USER couchdb
WORKDIR /opt/mem3_kube

CMD ls && cat mem3_kube.py && python mem3_kube.py
ENTRYPOINT ["/usr/bin/python"]
