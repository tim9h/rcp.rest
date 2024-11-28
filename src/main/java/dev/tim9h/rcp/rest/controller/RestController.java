package dev.tim9h.rcp.rest.controller;

import java.time.LocalTime;

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

	private Thread thread;

	public void start() {
		logger.info(() -> "Starting Rest controller");
		var port = settings.getInt(RestViewFactory.SETTING_PORT);
		thread = new Thread(() -> {
			server = Javalin.create().start(port);
			server.get("hello", (Handler) ctx -> ctx.result("hello world at " + LocalTime.now().toString()));
			logger.info(() -> "Rest controller started");
			em.echoAsync("Rest controller started");
		}, "RestController");
		thread.setDaemon(true);
		thread.start();
	}

	public void stop() {
		if (thread != null && server != null) {
			server.stop();
			server = null;
			logger.info(() -> "Stopping Rest Controller");
			em.echoAsync("Rest controller stopped");
			thread.interrupt();
			thread = null;
			logger.debug(() -> "Rest thread stopped");
		} else {
			em.echo("Rest controller not running");
		}
	}

}
