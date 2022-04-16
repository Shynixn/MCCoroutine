FROM ubuntu:22.04
WORKDIR tmp
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt install nodejs npm -y
RUN npm install --global http-server && apt-get install -y mkdocs
RUN apt-get install python3-pip -y && pip install mkdocs-material && pip install Pygments

COPY . /tmp
RUN mkdocs build

CMD ["sh","-c","http-server /tmp/site"]


