package at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.helper;

import java.util.ArrayList;
import java.util.List;

public class Customers {
	private static List<String> custs = new ArrayList<>();

	/**
	 * Registers a new customer.
	 * @param name the name of the customer
	 * @return true if the customer was registered, false if an entry for this
	 * name already exists
	 */
	public static boolean register(String name) {
		if(exists(name)) {
			return false;
		} else {
			custs.add(name);
			return true;
		}
	}

	/**
	 * @param name a name
	 * @return true if the name is registered, false otherwise
	 */
	public static boolean exists(String name) {
		return custs.contains(name);
	}
}
