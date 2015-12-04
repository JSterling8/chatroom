package models;

import net.jini.core.entry.Entry;

/**
 * This method is for notifications to latch on to and remove topicUsers when
 * they have left a Topic.
 * 
 * @author anon
 *
 */
@SuppressWarnings("serial")
public class JMSTopicUserRemoved implements Entry {
	public JMSTopic topic;
	public JMSUser user;

	public JMSTopicUserRemoved() {
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