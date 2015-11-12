package models;

import net.jini.core.entry.Entry;

public class JMSTopicUser implements Entry {
	public JMSTopic topic;
	public JMSUser user;
	
	public JMSTopicUser(){}
	
	public JMSTopicUser(JMSTopic topic, JMSUser user){
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
