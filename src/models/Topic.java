package models;

public class Topic {
	public int id;
	public String name;
	public String owner;
	public int users;
	
	public Topic(){
		
	}
	
	public Topic(int id, String name, String owner, int users){
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.users = users;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
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
	public void setUsers(int users) {
		this.users = users;
	}
	
	
}
