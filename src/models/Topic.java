package models;

import java.util.UUID;

public class Topic {
	public UUID id;
	public String name;
	public String owner;
	public Integer users;
	
	public Topic(){
		
	}
	
	public Topic(String name, String owner, Integer users){
		this.id = UUID.randomUUID();
		this.name = name;
		this.owner = owner;
		this.users = users;
	}
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public int getUsers() {
		return users;
	}
	public void setUsers(Integer users) {
		this.users = users;
	}
	
	
}
