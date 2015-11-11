package controllers;

import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import models.JMSTopic;
import models.JMSUser;
import views.MainMenuFrame;

public class MainMenuController {
	private MainMenuFrame frame;
	private DefaultTableModel tableModel;
	private JMSUser user;
	
	public MainMenuController(MainMenuFrame frame, DefaultTableModel tableModel, JMSUser user){
		this.frame = frame;
		this.tableModel = tableModel;
		this.user = user;
	}
	
	
	public void handleCreateButtonPressed(){		
		String topicName = JOptionPane.showInputDialog(frame, "Enter a topic name: ");
		if(StringUtils.isNotBlank(topicName)){
			createTopic(topicName);
		}
	}
	
	public void createTopic(String name){
		JMSTopic topic = new JMSTopic(name, user.name, 1);

		//TODO Add topic to JavaSpace...
		boolean success = true;
		
		if(success){
			Object[] rowData = {topic.getName(), topic.getOwner(), topic.getUsers()};
			tableModel.addRow(rowData);
		} else {
			JOptionPane.showMessageDialog(frame, "Failed to create topic.  Topic name already exists");
		}

	}
	
	public void deleteTopic(JMSTopic topic){
		if(user.getName().equals(topic.getOwner())){
			//TODO Remove topic from JavaSpace
			
			for(int i = 0; i < tableModel.getRowCount(); i++){
				if((UUID)tableModel.getValueAt(i, 0) == topic.getId()){
					tableModel.removeRow(i);
					break;
				}
			}
		}
	}
	
	public void joinTopic(JMSTopic topic){
		//TODO implement method...
	}
	
	public void logout(){
		//TODO implement method...
	}
}
