package dev.tim9h.rcp.rest.controller;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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

	private Javalin server;

	public void start() {
		logger.info(() -> "Starting Rest Controller");
		server = Javalin.create().start(settings.getInt(RestViewFactory.SETTING_PORT));
		server.get("hello", (Handler) ctx -> ctx.result("hello world"));
		logger.info(() -> "Rest Controller started");
	}

	public void stop() {
		logger.info(() -> "Stopping Rest Controller");
		server.stop();
	}

}
