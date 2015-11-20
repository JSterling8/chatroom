package services;

import java.util.Properties;

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
	private static JavaSpace05 space;
	private static final String proxyHost = new Properties().getProperty("http.proxyHost");

	public static JavaSpace05 getSpace(String hostname) {
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

	public static JavaSpace05 getSpace() {
		if (StringUtils.equals("wwwproxy.hud.ac.uk", proxyHost)) {
			return getSpace("waterloo");
		} else {
			return getSpace("localhost");
		}
	}

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

	public static TransactionManager getManager() {
		if (StringUtils.equals("wwwproxy.hud.ac.uk", proxyHost)) {
			return getManager("waterloo");
		} else {
			return getManager("localhost");
		}
	}
}
