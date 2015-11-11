package models;

public class JMSUser {
	public String name;
	public String baseName;
	public String password;

	public JMSUser() {

	}

	public JMSUser(String name, String baseName, String password) {
		this.name = name;
		this.baseName = baseName;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
