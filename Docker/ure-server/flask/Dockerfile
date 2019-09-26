FROM python:3

# MAINTAINER Toshiyuki_YAMAJI<nexus02221toshiyuki@gmail.com>

USER root

# 使用するパッケージをインストール
RUN apt update -y && apt upgrade -y
RUN apt install -y vim postgresql-client
RUN pip install --upgrade pip
RUN pip install --upgrade setuptools
RUN pip install flask
RUN pip install psycopg2
RUN pip install psycopg2-binary

# docker外部に向けて5000番ポートを開放
EXPOSE 5000