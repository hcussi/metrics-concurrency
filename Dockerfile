# Dockerfile used in docker-compose only

# Getting image from docker hub
FROM hernancussi/metrics-concurrency:latest

# install curl to check container healthy locally, no needed in k8s
USER root
RUN apt-get update
RUN apt-get install -y gcc
RUN apt-get install -y curl
