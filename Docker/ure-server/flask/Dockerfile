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
RUN pip install pillow
RUN pip install jupyterlab

RUN jupyter serverextension enable --py jupyterlab
CMD jupyter lab --NotebookApp.password='sha1:e10592e1c8fa:757ed12252c06a6348fd2f4715e877d19eb7ea30' --port 8888 --ip=0.0.0.0 --allow-root

# docker外部に向けて5000番ポートを開放
EXPOSE 5000