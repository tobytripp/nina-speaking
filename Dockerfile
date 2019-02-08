# -*- mode: dockerfile -*-
FROM clojure:openjdk-11-lein

ENV LEIN=/usr/local/bin/lein
ENV M2_REPO=/usr/local/lib/m2
RUN mkdir -p $M2_REPO /etc/leiningen/ && \
    echo "{:system { :local-repo \"${M2_REPO}\" }}" > /etc/leiningen/profiles.clj

ARG APP_HOME=/usr/src/app
ENV APP_HOME $APP_HOME
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME

RUN apt-get update &&  \
  apt-get install -y netcat && \
  rm -rf /var/lib/apt/lists/*
ENV BIN_PATH /usr/local/bin
COPY ./is_ready $BIN_PATH/

COPY . .

VOLUME \
  $APP_HOME/bin       \
  $APP_HOME/src       \
  $APP_HOME/test      \
  $APP_HOME/resources \
  $APP_HOME/log

EXPOSE 5888
EXPOSE 80

CMD ["lein", "repl", ":headless", ":host", "0.0.0.0", ":port", "5888"]
