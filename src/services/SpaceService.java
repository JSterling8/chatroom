package services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.lookup.entry.Name;
import net.jini.space.JavaSpace;

/**
 * This is Dr. Gary Allen's SpaceUtils class, which I've renamed and made a few
 * tweaks to.
 * 
 * @author Gary Allen
 *
 */
public class SpaceService {
	public static JavaSpace getSpace(String hostname) {
		JavaSpace js = null;
		try {
			LookupLocator l = new LookupLocator("jini://" + hostname);

			ServiceRegistrar sr = l.getRegistrar();

			Class c = Class.forName("net.jini.space.JavaSpace");
			Class[] classTemplate = { c };

			js = (JavaSpace) sr.lookup(new ServiceTemplate(null, classTemplate, null));

		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
		return js;
	}

	public static JavaSpace getSpace() {
		return getSpace("127.0.0.1");
	}

	public static TransactionManager getManager(String hostname) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		// Creating service template to find transaction manager service by
		// matching fields.
		Class[] classes = new Class[] { net.jini.core.transaction.server.TransactionManager.class };
		Name sn = new Name("*");
		ServiceTemplate tmpl = new ServiceTemplate(null, classes, new Entry[] {});

		// Creating a lookup locator.
		LookupLocator locator = null;
		try {
			locator = new LookupLocator("jini://" + hostname);
		} catch (MalformedURLException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}

		ServiceRegistrar sr = null;
		try {
			sr = locator.getRegistrar();
		} catch (ClassNotFoundException ex1) {
			ex1.printStackTrace();
		} catch (IOException ex1) {
			ex1.printStackTrace();
		}

		TransactionManager tm = null;
		try {
			tm = (TransactionManager) sr.lookup(tmpl);
		} catch (RemoteException ex2) {
			ex2.printStackTrace();
		}
		return tm;
	}

	public static TransactionManager getManager() {
		return getManager("127.0.0.1");
	}
}
