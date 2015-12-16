package models;

import java.util.Date;
import java.util.UUID;

import net.jini.core.entry.Entry;

/**
 * The model representing a message in the application
 * 
 * @author Jonathan Sterling
 *
 */
@SuppressWarnings("serial")
public class JMSMessage implements Entry {
	public JMSTopic topic;				// The topic the message was sent in.
	public Date sentDate;				// The date/time the message was sent.
	public JMSUser from;				// The user the message was from
	public JMSUser to;					// The user the message was to
	public UUID id;						// The message's unique ID
	public String message;				// The message text sent

	public JMSMessage() {
		// Empty constructor for JavaSpaces
	}

	public JMSMessage(JMSTopic topic, Date sentDate, JMSUser from, JMSUser to, UUID id, String message) {
		this.topic = topic;
		this.sentDate = sentDate;
		this.from = from;
		this.to = to;
		this.message = message;
		this.id = id;
	}
	
	/**
	 * Constructor for easier template creation to get all messages belonging to topic
	 */
	public JMSMessage(JMSTopic topic){
		this.topic = topic;
	}

	public JMSTopic getTopic() {
		return topic;
	}

	public void setTopic(JMSTopic topic) {
		this.topic = topic;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public JMSUser getFrom() {
		return from;
	}

	public void setFrom(JMSUser from) {
		this.from = from;
	}

	public JMSUser getTo() {
		return to;
	}

	public void setTo(JMSUser to) {
		this.to = to;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
