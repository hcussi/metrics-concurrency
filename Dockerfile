FROM hcussi/metrics-concurrency:0.0.1

USER root
RUN apt-get update
RUN apt-get install -y gcc
RUN apt-get install -y curl
