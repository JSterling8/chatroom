package models;

import net.jini.core.entry.Entry;

/**
 * This method is for notifications to latch on to and remove topicUsers when they 
 * have left a Topic.
 * 
 * @author anon
 *
 */
public class JMSTopicUserRemoved implements Entry {
	public JMSTopicUser topicUser;
	
	public JMSTopicUserRemoved(){}

	public JMSTopicUser getTopicUser() {
		return topicUser;
	}

	public void setTopicUser(JMSTopicUser topicUser) {
		this.topicUser = topicUser;
	}
}