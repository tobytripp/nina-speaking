# -*- mode: dockerfile -*-
FROM openjdk:8
LABEL maintainer="Toby Tripp <toby@tripp.net>"

ENV URI http://mirrors.ocf.berkeley.edu/apache/directory/studio/2.0.0.v20170904-M13/ApacheDirectoryStudio-2.0.0.v20170904-M13-linux.gtk.x86_64.tar.gz
ENV MD5 182c1affe5040e9549e025375ef1913f
ENV BINPATH /usr/local/bin

RUN mkdir -p $BINPATH

RUN cd /tmp                                   \
 &&   curl -fLsS $URI > ADS.tar.gz            \
 &&   md5sum ADS.tar.gz                       \
 &&   tar xfz ADS.tar.gz                      \
 &&   mv ApacheDirectoryStudio $BINPATH/ \
 &&   rm ADS.tar.gz                           \
 && cd -                                      \
 && rm -rf /tmp/*

RUN apt-get update -qqy                    \
 && apt-get install -y x11vnc xvfb fluxbox \
 && rm -rf /var/lib/apt/lists/* /var/cache/apt/*
RUN mkdir ~/.vnc \
 && x11vnc -storepasswd develop ~/.vnc/passwd

ENV SCREEN_WIDTH  1360
ENV SCREEN_HEIGHT 1020
ENV SCREEN_DEPTH  24
ENV DISPLAY :99.0

COPY docker-entrypoint.sh $BINPATH/

ENV PATH $PATH:$BINPATH

EXPOSE 5900

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["serve"]
