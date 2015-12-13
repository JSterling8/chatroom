package models;

import net.jini.core.entry.Entry;

/**
 * The model representing a user in a topic.
 * 
 * @author Jonathan Sterling
 *
 */
@SuppressWarnings("serial")
public class JMSTopicUser implements Entry {
	public JMSTopic topic;			// The topic the user is in
	public JMSUser user;			// The user in the topic
	
	public JMSTopicUser(){
		// Empty constructor for JavaSpaces
	}
	
	public JMSTopicUser(JMSTopic topic, JMSUser user){
		this.topic = topic;
		this.user = user;
	}

	public JMSTopicUser(JMSTopic topic) {
		this.topic = topic;
	}

	public JMSTopic getTopic() {
		return topic;
	}

	public void setTopic(JMSTopic topic) {
		this.topic = topic;
	}

	public JMSUser getUser() {
		return user;
	}

	public void setUser(JMSUser user) {
		this.user = user;
	}
}
