package models;

import net.jini.core.entry.Entry;

/**
 * This model is for notifications to latch on to and remove TopicUsers when
 * they have left a Topic.
 * 
 * @author Jonathan Sterling
 *
 */
@SuppressWarnings("serial")
public class JMSTopicUserRemoved implements Entry {
	public JMSTopic topic;
	public JMSUser user;

	public JMSTopicUserRemoved() {
		// Empty constructor for JavaSpaces
	}

	public JMSTopicUserRemoved(JMSTopic topic) {
		this.topic = topic;
	}

	public JMSTopicUserRemoved(JMSTopic topic, JMSUser user) {
		this.topic = topic;
		this.user = user;
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