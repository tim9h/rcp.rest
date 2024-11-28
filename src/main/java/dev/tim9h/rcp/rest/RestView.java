package dev.tim9h.rcp.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.tim9h.rcp.event.EventManager;
import dev.tim9h.rcp.logging.InjectLogger;
import dev.tim9h.rcp.rest.controller.RestController;
import dev.tim9h.rcp.spi.CCard;
import dev.tim9h.rcp.spi.Mode;

public class RestView implements CCard {
	
	@InjectLogger
	private Logger logger;

	@Inject
	private EventManager eventManager;

	@Override
	public String getName() {
		return "rest";
	}
	
	@Inject
	private RestController controller;
	
	@Override
	public Optional<List<Mode>> getModes() {
		return Optional.of(Arrays.asList(new Mode() {
			
			@Override
			public void onEnable() {
				eventManager.showWaitingIndicatorAsync();
				controller.start();
			}
			
			@Override
			public void onDisable() {
				eventManager.showWaitingIndicatorAsync();
				controller.stop();
			}
			
			@Override
			public String getName() {
				return "rest";
			}
		}));
	}

}
