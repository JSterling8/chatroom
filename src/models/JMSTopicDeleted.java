package models;

import net.jini.core.entry.Entry;

/**
 * This model is for notifications to latch on to and remove Topics when they
 * are deleted.
 * 
 * @author Jonathan Sterling
 *
 */
@SuppressWarnings("serial")
public class JMSTopicDeleted implements Entry {
	public JMSTopic topic; // The topic that was deleted

	public JMSTopicDeleted() {
		// Empty constructor for JavaSpaces
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
