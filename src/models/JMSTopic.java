package models;

import java.util.UUID;

import net.jini.core.entry.Entry;

/**
 * The model representing a topic in the application
 * 
 * @author Jonathan Sterling
 *
 */
@SuppressWarnings("serial")
public class JMSTopic implements Entry {
	public UUID id; // The unique ID of the topic
	public String name; // The name of the topic
	public String baseName; // The name of the topic, in all upper case with all
							// non-alphanumerics removed
	public JMSUser owner; // The user who created the topic

	public JMSTopic() {
		// Empty constructor for JavaSpaces
	}

	public JMSTopic(String name, JMSUser owner) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.owner = owner;

		// Base name is used to ensure that users can't create similar-looking
		// topic names that trick other users into joining the wrong topic
		setBaseName(name);
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
		setBaseName(name);
	}

	public JMSUser getOwner() {
		return owner;
	}

	public void setOwner(JMSUser owner) {
		this.owner = owner;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String name) {
		baseName = name;
		baseName = baseName.replaceAll("[^A-Za-z0-9]", "");
		baseName = baseName.toUpperCase();
	}
}
