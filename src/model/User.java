package model;


public class User {
	
	private String userId;
	private String name;
	private String email;
	private String role;
	
	public User(String userId, String name, String email, String role) {
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.role = role;
	}
	
//	get Method
	public String getUserId() {
		return userId;
	}
	public String getNamw() {
		return name;
	}
	public String getEmail() {
		return email;
	}
	public String getRole() {
		return role;
	}

	public String getName() {
		return name;
	}

//	set Method
	public void setName(String name) {
		this.name = name;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setRole(String role) {
		this.role = role;
	}
}
