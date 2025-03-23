package dev.tim9h.rcp.rest.controller;

import static dev.tim9h.rcp.rest.controller.AuthManager.Role.OPERATOR;

import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dev.tim9h.rcp.event.CcEvent;
import dev.tim9h.rcp.event.EventManager;
import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.rest.RestViewFactory;
import dev.tim9h.rcp.settings.Settings;
import io.javalin.Javalin;
import javafx.application.Platform;

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

	@Inject
	private AuthManager authManager;

	public void start() {
		logger.info(() -> "Starting Rest controller");
		var port = settings.getInt(RestViewFactory.SETTING_PORT);
		thread = new Thread(() -> {
			server = Javalin
					.create(config -> config.router.mount(router -> router.beforeMatched(authManager::handleAccess)))
					.start(port);

			logger.info(() -> "Rest controller started on port " + port);
			em.echo("Rest controller started");

			createPostMapping("logiled", "color", color -> em.post(new CcEvent("LOGILED", color)));

			createPostMapping("next", () -> em.post(new CcEvent("next")));
			createPostMapping("previous", () -> em.post(new CcEvent("previous")));
			createPostMapping("play", () -> em.post(new CcEvent("play")));
			createPostMapping("pause", () -> em.post(new CcEvent("pause")));
			createPostMapping("stop", () -> em.post(new CcEvent("stop")));

			createPostMapping("lock", () -> em.post(new CcEvent("lock")));
			createPostMapping("shutdown", "time", time -> em.post(new CcEvent("shutdown", time)));

		}, "RestController");
		thread.setDaemon(true);
		thread.start();
	}

	private void createPostMapping(String path, Runnable runnable) {
		createPostMapping(path, "", _ -> runnable.run());
	}

	private void createPostMapping(String path, String param, Consumer<String> consumer) {
		server.post(path, ctx -> {
			try {
				var value = ctx.queryParam(param);
				logger.debug(() -> String.format("Handling post  request for %s%s", path,
						param.equals("") ? "" : " (" + param + ": " + value + ")"));
				Platform.runLater(() -> consumer.accept(value));
			} catch (IllegalArgumentException e) {
				logger.warn(() -> String.format("Path parameter %s for post mapping %s not found", param, path));
			}
		}, OPERATOR);
		logger.info(() -> "Post mapping created: " + path);
	}

	public void stop() {
		if (thread != null && server != null) {
			server.stop();
			server = null;
			logger.info(() -> "Stopping Rest Controller");
			em.echo("Rest controller stopped");
			thread.interrupt();
			thread = null;
			logger.debug(() -> "Rest thread stopped");
		} else {
			em.echo("Rest controller not running");
		}
	}

}
