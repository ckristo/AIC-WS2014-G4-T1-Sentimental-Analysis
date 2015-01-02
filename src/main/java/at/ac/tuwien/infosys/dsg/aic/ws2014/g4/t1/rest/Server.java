package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.rest;

import javax.ws.rs.ext.RuntimeDelegate;

import at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper.SentimentAnalyzer;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;

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
