package models;

import java.util.Date;
import java.util.UUID;

import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
public class JMSMessage implements Entry {
	public JMSTopic topic;
	public Date sentDate;
	public JMSUser from;
	public JMSUser to;
	public UUID id;
	public String message;

	public JMSMessage() {
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
	 * 
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
