# -*- mode: dockerfile -*-
FROM openjdk:8
LABEL maintainer="Toby Tripp <toby@tripp.net>"

ENV URI http://mirrors.ocf.berkeley.edu/apache/directory/studio/2.0.0.v20170904-M13/ApacheDirectoryStudio-2.0.0.v20170904-M13-linux.gtk.x86_64.tar.gz
ENV MD5 182c1affe5040e9549e025375ef1913f
ENV BINPATH /usr/local/bin

RUN mkdir -p $BINPATH

# Install ApacheDirectoryStudio
RUN cd /tmp                              \
 &&   curl -fLsS $URI > ADS.tar.gz       \
 &&   md5sum ADS.tar.gz                  \
 &&   tar xfz ADS.tar.gz                 \
 &&   mv ApacheDirectoryStudio $BINPATH/ \
 &&   rm ADS.tar.gz                      \
 && cd -                                 \
 && rm -rf /tmp/*


RUN apt-get update -qqy                    \
 && apt-get install -y sudo xvfb fluxbox
 # && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

# Build x11vnc from source to avoid the stack smash crash on ubuntu
RUN cd /tmp                                                                    \
 && apt-get install -y build-essential libxtst-dev libssl-dev libjpeg-dev      \
 && curl -fLsS http://x11vnc.sourceforge.net/dev/x11vnc-0.9.14-dev.tar.gz >    \
      x11vnc-0.9.14-dev.tar.gz                                                 \
 && gzip -dc x11vnc-0.9.14-dev.tar.gz | tar -xvf -                             \
 && cd x11vnc-0.9.14/                                                          \
 && ./configure --prefix=/usr/local CFLAGS='-g -O2 -fno-stack-protector -Wall' \
 && make && make install

RUN useradd ldap \
         --shell /bin/bash  \
         --create-home \
  && usermod -a -G sudo ldap \
  && echo 'ALL ALL = (ALL) NOPASSWD: ALL' >> /etc/sudoers \
  && echo 'ldap:secret' | chpasswd \
  && chown -R ldap $BINPATH

USER ldap
WORKDIR /home/ldap

RUN mkdir ~/.vnc \
 && x11vnc -storepasswd develop ~/.vnc/passwd

ENV SCREEN_WIDTH  1360
ENV SCREEN_HEIGHT 1020
ENV SCREEN_DEPTH  24
ENV DISPLAY :99.0

COPY docker-entrypoint.sh $BINPATH/

ENV PATH $PATH:$BINPATH

EXPOSE 5900
VOLUME /home/ldap/.ApacheDirectoryStudio

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["serve"]
