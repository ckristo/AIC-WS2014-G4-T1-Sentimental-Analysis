package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.webservice.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Starts the REST service using an embedded Jetty instance.
 */
public class JettyTwitterSentimentRestService {

	/**
	 * Logger instance.
	 */
	private static final Logger logger = LogManager.getLogger(JettyTwitterSentimentRestService.class);

	/**
	 * The server port to use.
	 */
	private static final int SERVER_PORT = 9000;

	/**
	 * main()
	 *
	 * @param args command line args
	 */
	public static void main(String[] args) {
		ServletContainer servletContainer = new ServletContainer(new TwitterSentimentServiceResourceConfig());
		ServletHolder sh = new ServletHolder(servletContainer);
		Server server = new Server(SERVER_PORT);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(sh, "/*");
		server.setHandler(context);

		try {
			server.start();
			server.join();
		} catch (Exception ex) {
			logger.error("Couldn't start embedded Jetty Server for REST service.", ex);
		} finally {
			server.destroy();
		}
	}
}
