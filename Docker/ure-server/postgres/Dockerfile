FROM postgres:11.5

MAINTAINER Toshiyuki_YAMAJI<nexus02221toshiyuki@gmail.com>

USER root
RUN apt update -y && apt upgrade -y
RUN apt install -y\
    sudo \
    wget \
    postgis \
    osm2pgsql

RUN useradd -m ure
RUN gpasswd -a ure sudo
RUN echo "ure:procon30" | chpasswd

WORKDIR /home/ure
RUN mkdir Download

EXPOSE 5432