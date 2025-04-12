package dev.tim9h.rcp.rest.controller;

import static dev.tim9h.rcp.rest.controller.AuthManager.Role.OPERATOR;

import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import dev.tim9h.rcp.event.CcEvent;
import dev.tim9h.rcp.event.EventManager;
import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.rest.RestViewFactory;
import dev.tim9h.rcp.settings.Settings;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
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

	private String title;

	private String artist;

	private String album;

	private boolean isPlaying;

	private record Track(String song, String artist, String album, boolean isPlaying) {
	}

	@Inject
	private AuthManager authManager;

	@Inject
	public RestController(Injector injector) {
		injector.injectMembers(this);
		subscribeToNp();
	}

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

			createGetMapping("np", this::returnCurrentTrack);

		}, "RestController");
		thread.setDaemon(true);
		thread.start();
	}

	private void createPostMapping(String path, Runnable runnable) {
		createPostMapping(path, "", _ -> runnable.run(), null);
	}

	private void createPostMapping(String path, String param, Consumer<String> consumer) {
		createPostMapping(path, param, consumer, null);
	}

	private void createPostMapping(String path, String param, Consumer<String> consumer, Consumer<Context> response) {
		server.post(path, ctx -> {
			try {
				var value = ctx.queryParam(param);
				logger.debug(() -> String.format("Handling post  request for %s%s", path,
						param.equals("") ? "" : " (" + param + ": " + value + ")"));
				Platform.runLater(() -> consumer.accept(value));
				if (response != null) {
					response.accept(ctx);
				}
			} catch (IllegalArgumentException e) {
				logger.warn(() -> String.format("Path parameter %s for post mapping %s not found", param, path));
			}
		}, OPERATOR);
		logger.info(() -> "Post mapping created: " + path);
	}

	private void createGetMapping(String path, Consumer<Context> response) {
		server.get(path, response::accept, OPERATOR);
		logger.info(() -> "Get mapping created: " + path);
	}

	private void subscribeToNp() {
		em.listen("np", currentTrack -> {
			if (currentTrack.length < 4) {
				logger.warn(() -> "Not enough parameters for Now Playing event");
				return;
			}
			this.title = (String) currentTrack[0];
			this.artist = (String) currentTrack[1];
			this.album = (String) currentTrack[2];
			this.isPlaying = (boolean) currentTrack[3];
		});
	}

	private void returnCurrentTrack(Context ctx) {
		if (title == null || artist == null || album == null) {
			ctx.status(HttpStatus.NOT_FOUND);
			return;
		}
		var currentTrack = new Track(title, artist, album, isPlaying);
		logger.debug(() -> "Returning current track: " + currentTrack);
		ctx.json(currentTrack);
		ctx.status(HttpStatus.OK);
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
