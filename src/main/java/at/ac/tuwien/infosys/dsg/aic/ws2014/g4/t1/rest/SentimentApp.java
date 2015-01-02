package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class SentimentApp extends Application {

	public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(SentimentImpl.class);

        return classes;
    }
}
