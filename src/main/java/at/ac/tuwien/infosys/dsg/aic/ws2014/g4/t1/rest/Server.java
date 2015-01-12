package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.rest;

import javax.ws.rs.ext.RuntimeDelegate;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.SentimentAnalyzer;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.http_jetty.JettyHTTPDestination;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngine;
import org.apache.cxf.transport.http_jetty.ServerEngine;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class Server {

	public static void main(String args[]) throws Exception {
		if(!SentimentAnalyzer.start()) {
			System.err.println("failed to start analyzer");
			System.exit(1);
		}

		JAXRSServerFactoryBean jaxrsServerFactory = RuntimeDelegate.getInstance().createEndpoint(new SentimentApp(), JAXRSServerFactoryBean.class);
		jaxrsServerFactory.setAddress("http://localhost:9000");
		org.apache.cxf.endpoint.Server server = jaxrsServerFactory.create();
		server.start();

		Destination dest = server.getDestination();
		JettyHTTPDestination jettyDest = JettyHTTPDestination.class.cast(dest);
		ServerEngine engine = jettyDest.getEngine();
		JettyHTTPServerEngine serverEngine = JettyHTTPServerEngine.class.cast(engine);
		org.eclipse.jetty.server.Server httpServer = serverEngine.getServer();

		// server must be stopped before adding the new handler
		httpServer.stop();
		httpServer.join();

		Handler[] existingHandlers = httpServer.getHandlers();

		// add the static resources as a new handler
		ResourceHandler handler = new ResourceHandler();
		handler.setWelcomeFiles(new String[]{"index.html"});
		String filedir = Server.class.getClassLoader().getResource("web/").toExternalForm();
		handler.setResourceBase(filedir);

		HandlerList handlers = new HandlerList();
		handlers.addHandler(handler);
		if (existingHandlers != null) {
			for (Handler h : existingHandlers) {
				handlers.addHandler(h);
			}
		}
		httpServer.setHandler(handlers);

		httpServer.start();
		httpServer.join();
		System.out.println("server started");

		try {
			while (true) {
				Thread.sleep(1000);
			}
		} catch(InterruptedException e) {
			server.stop();
			System.exit(0);
		}
	}
}
