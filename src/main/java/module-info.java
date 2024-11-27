module rcp.rest {
	exports dev.tim9h.rcp.rest;

	requires transitive rcp.api;
	requires com.google.guice;
	requires org.apache.logging.log4j;
	requires transitive javafx.controls;
	requires io.javalin;

}