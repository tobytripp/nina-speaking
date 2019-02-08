.PHONY: test
DC   := docker-compose
LEIN := $(DC) run nina lein

test:
	$(LEIN) $@

rebuild:
	$(DC) rm -fsv nina			\
     && $(DC) up -d --build nina		\
     && $(DC) exec nina is_ready
