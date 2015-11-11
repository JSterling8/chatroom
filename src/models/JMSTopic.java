package models;

import java.util.UUID;

public class JMSTopic {
	public UUID id;
	public String name;
	public String baseName;
	public JMSUser owner;
	public Integer users;

	public JMSTopic() {

	}

	public JMSTopic(String name, JMSUser owner, Integer users) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.owner = owner;
		this.users = users;

		this.baseName = name.replaceAll("[^A-Za-z0-9]", "");
		this.baseName = baseName.toUpperCase();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JMSUser getOwner() {
		return owner;
	}

	public void setOwner(JMSUser owner) {
		this.owner = owner;
	}

	public int getUsers() {
		return users;
	}

	public void setUsers(Integer users) {
		this.users = users;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		baseName = baseName.replaceAll("[^A-Za-z0-9]", "");
		baseName = baseName.toUpperCase();

		this.baseName = baseName;
	}
}
