package models;

import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
//TODO Add to class diagram
public class JMSTopicDeleted implements Entry {
	public JMSTopic topic;
	
	public JMSTopicDeleted(){
		
	}
	
	public JMSTopicDeleted(JMSTopic topic) {
		this.topic = topic;
	}

	public JMSTopic getTopic() {
		return topic;
	}

	public void setTopic(JMSTopic topic) {
		this.topic = topic;
	}
}
