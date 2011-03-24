package uk.ac.ebi.mydas.writeback.datasource.model;

public class Users {
	private Long id;
	private String login;
	private String password;
	
	public Users(){}
	 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
