package services;

import org.apache.commons.lang3.StringUtils;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace05;

/**
 * This is Dr. Gary Allen's SpaceUtils class, which I've renamed and made a few
 * tweaks to.
 * 
 * @author Gary Allen
 *
 */
public class SpaceService {
	private static final String proxyHost = System.getProperties().getProperty("http.proxyHost");

	private static JavaSpace05 space;

	/**
	 * Gets the JavaSpace for a given hostname
	 * 
	 * @param hostname
	 *            The hostname to look for a JavaSpace on
	 * 
	 * @return A JavaSpace for a given hostname
	 */
	@SuppressWarnings("rawtypes")
	public static JavaSpace05 getSpace(String hostname) {
		// Only get the space once...
		if (space == null) {
			try {
				LookupLocator l = new LookupLocator("jini://" + hostname);

				ServiceRegistrar sr = l.getRegistrar();

				Class c = Class.forName("net.jini.space.JavaSpace");
				Class[] classTemplate = { c };

				space = (JavaSpace05) sr.lookup(new ServiceTemplate(null, classTemplate, null));

			} catch (Exception e) {
				System.err.println("Failed to get space");
				e.printStackTrace();
			}
		}

		return space;
	}

	/**
	 * Determines if the application is being run at university of locally, then
	 * gets the space accordingly
	 * 
	 * @return A JavaSpace
	 */
	public static JavaSpace05 getSpace() {
		if (StringUtils.equals("wwwproxy.hud.ac.uk", proxyHost)) {
			return getSpace("waterloo");
		} else {
			return getSpace("localhost");
		}
	}

	/**
	 * Gets a TransactionManager for a given hostname.
	 * 
	 * @param hostname
	 *            The hostname to get a TransactionManager from.
	 * 
	 * @return A TransactionManager for a given hostname.
	 */
	@SuppressWarnings("rawtypes")
	public static TransactionManager getManager(String hostname) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		TransactionManager tm = null;
		try {
			LookupLocator l = new LookupLocator("jini://" + hostname);

			ServiceRegistrar sr = l.getRegistrar();

			Class c = Class.forName("net.jini.core.transaction.server.TransactionManager");
			Class[] classTemplate = { c };

			tm = (TransactionManager) sr.lookup(new ServiceTemplate(null, classTemplate, null));

		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
		return tm;
	}

	/**
	 * Dynamically determines if the application is being run at university of
	 * locally, and gets the TransactionManager accordingly
	 * 
	 * @return A TransactionManager
	 */
	public static TransactionManager getManager() {
		if (StringUtils.equals("wwwproxy.hud.ac.uk", proxyHost)) {
			return getManager("waterloo");
		} else {
			return getManager("localhost");
		}
	}
}
