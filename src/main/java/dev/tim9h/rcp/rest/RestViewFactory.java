package dev.tim9h.rcp.rest;

import java.util.Map;

import com.google.inject.Inject;

import dev.tim9h.rcp.spi.CCard;
import dev.tim9h.rcp.spi.CCardFactory;

public class RestViewFactory implements CCardFactory  {

	public static final String SETTING_PORT = "rest.port";
	
	public static final String SETTING_USER = "rest.user";
	
	public static final String SETTING_PASS = "rest.pass";

	@Inject 
	private RestView view;
	
	@Override
	public String getId() {
		return "rest";
	}

	@Override
	public CCard createCCard() {
		return view;
	}

	@Override
	public Map<String, String> getSettingsContributions() {
		 return Map.of(SETTING_PORT, "8080", SETTING_USER, "", SETTING_PASS, "");
	}

}
