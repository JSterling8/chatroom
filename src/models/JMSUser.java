package models;

import java.util.UUID;

import net.jini.core.entry.Entry;

/**
 * The model representing a user in the chatroom application.
 * 
 * @author Jonathan Sterling
 *
 */
@SuppressWarnings("serial")
public class JMSUser implements Entry {
	public String name;			// The user's name
	public String baseName;		// The user's name in all uppercase, with non-alphanumeric chars removed
								// Base names are used to prevent impersonation.
	public String password;		// The user's hashed password
	public UUID id;				// The user's unique ID

	public JMSUser() {
		// Empty constructor for JavaSpaces
	}

	public JMSUser(String name, String password) {
		this.name = name;
		this.password = password;
		this.id = UUID.randomUUID();
		
		setBaseName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		setBaseName(name);
	}

	public String getBaseName() {
		return baseName;
	}

	/**
	 * Converts the user's name to all uppercase, with non-alphanumeric characters removed.
	 * 
	 * This is to prevent impersonation.
	 * 
	 * @param name The user's name.
	 */
	public void setBaseName(String name) {
		baseName = name;
		baseName = baseName.replaceAll("[^A-Za-z0-9]", "");
		baseName = baseName.toUpperCase();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * Checks if two users are the same.
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof JMSUser) {
			JMSUser otherUser = (JMSUser) o;
			
			if (otherUser.getBaseName().equals(this.getBaseName())
					&& otherUser.getId().equals(this.getId())) {
				return true;
			}
		}

		return false;
	}
}
