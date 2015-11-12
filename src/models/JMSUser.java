package models;

import java.util.UUID;

import net.jini.core.entry.Entry;

public class JMSUser implements Entry {
	public String name;
	public String baseName;
	public String password;
	public UUID id;

	public JMSUser() {

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
