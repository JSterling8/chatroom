package controllers;

import javax.swing.table.DefaultTableModel;

import models.Topic;
import models.User;

public class MainMenuController {
	private DefaultTableModel tableModel;
	private User user;
	
	public MainMenuController(DefaultTableModel tableModel, User user){
		this.tableModel = tableModel;
		this.user = user;
	}
	
	
	public void handleCreateButtonPressed(){
		//TODO Add topic to JavaSpace...
		
	}
	
	public void createTopic(Topic topic){
		//TODO Add topic to JavaSpace...
		
		Object[] rowData = {topic.getId(), topic.getName(), topic.getOwner(), topic.getUsers()};
		tableModel.addRow(rowData);
	}
	
	public void deleteTopic(Topic topic){
		if(user.getName().equals(topic.getOwner())){
			//TODO Remove topic from JavaSpace
			
			for(int i = 0; i < tableModel.getRowCount(); i++){
				if((int)tableModel.getValueAt(i, 0) == topic.getId()){
					tableModel.removeRow(i);
					break;
				}
			}
		}
	}
	
	public void joinTopic(Topic topic){
		//TODO implement method...
	}
	
	public void logout(){
		//TODO implement method...
	}
}
