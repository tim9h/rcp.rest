package dev.tim9h.rcp.rest.controller;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.settings.Settings;

@Singleton
public class RestController {

	@InjectLogger
	private Logger logger;

	@Inject
	private Settings settings;

	public void start() {
		logger.info(() -> "Starting Rest Controller");
	}

	public void stop() {
		logger.info(() -> "Stopping Rest Controller");
	}

}
