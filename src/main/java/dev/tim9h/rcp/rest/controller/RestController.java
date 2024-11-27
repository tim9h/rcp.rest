package dev.tim9h.rcp.rest.controller;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dev.tim9h.rcp.event.EventManager;
import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.rest.RestViewFactory;
import dev.tim9h.rcp.settings.Settings;
import io.javalin.Javalin;
import io.javalin.http.Handler;

@Singleton
public class RestController {

	@InjectLogger
	private Logger logger;

	@Inject
	private Settings settings;
	
	@Inject
	private EventManager em;

	private Javalin server;

	public void start() {
		logger.info(() -> "Starting Rest controller");
		var port = settings.getInt(RestViewFactory.SETTING_PORT);
		server = Javalin.create().start(port);
		server.get("hello", (Handler) ctx -> ctx.result("hello world"));
		logger.info(() -> "Rest controller started");
		em.echo("Rest controller started");
	}

	public void stop() {
		if (server != null) {
			server.stop();
			logger.info(() -> "Stopping Rest Controller");
			em.echo("Rest controller stopped");
		} else {
			em.echo("Rest controller not running");
		}
	}

}
