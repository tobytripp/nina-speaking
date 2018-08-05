# -*- mode: dockerfile -*-
FROM tobytripp/clj-builder:latest

ARG APP_HOME=/usr/src/app
ENV APP_HOME $APP_HOME
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME

COPY . .

VOLUME \
  $APP_HOME/bin       \
  $APP_HOME/src       \
  $APP_HOME/test      \
  $APP_HOME/resources \
  $APP_HOME/log

EXPOSE 80
