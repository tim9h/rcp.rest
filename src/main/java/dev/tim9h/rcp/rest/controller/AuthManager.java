package dev.tim9h.rcp.rest.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dev.tim9h.rcp.rest.RestViewFactory;
import dev.tim9h.rcp.settings.Settings;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

@Singleton
public class AuthManager {

	@Inject
	private Settings settings;

	public enum Role implements RouteRole {
		ANYONE, OPERATOR
	}

	private record Pair(String user, String password) {
	}

	public void handleAccess(Context ctx) {
		var permittedRoles = ctx.routeRoles();
		if (permittedRoles.contains(Role.ANYONE)) {
			return;
		}
		if (getUserRoles(ctx).stream().anyMatch(permittedRoles::contains)) {
			return;
		}
		ctx.header(Header.WWW_AUTHENTICATE, "Basic");
		throw new UnauthorizedResponse();
	}

	private List<Role> getUserRoles(Context ctx) {
		var user = settings.getString(RestViewFactory.SETTING_USER);
		var pass = settings.getString(RestViewFactory.SETTING_PASS);
		var userRolesMap = Map.of(new Pair(user, pass), List.of(Role.OPERATOR)); 
		return Optional.ofNullable(ctx.basicAuthCredentials())
				.map(credentials -> userRolesMap
						.getOrDefault(new Pair(credentials.getUsername(), credentials.getPassword()), List.of()))
				.orElse(List.of());
	}

}
