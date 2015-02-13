package com.app.chasebank.entity;

public class LoginUser {
	private String userId, userEmail, userRole;
	private boolean loginStatus;
	private boolean setup;
	
	/**
	 * Constructor, mainly for successfull login requests
	 * 
	 * @param id User id
	 * @param email User email address
	 * @param role User Role
	 * @param status User Login status
	 * 
	 * @return this
	 */
	public LoginUser(String id, String email, String role, boolean status) {
		this.userId =  id;
		this.userEmail = email;
		this.userRole = role;
		this.loginStatus = status;
	}
	
	/**
	 * Constructor, mainly for failed login requests
	 * 
	 * @param loginStatus User login status
	 * @return this
	 */
	public LoginUser(boolean loginStatus) {
		this.userEmail = null;
		this.userId = null;
		this.userRole = null;
		this.loginStatus = loginStatus;
	}
	
	/**
	 * 
	 * @param user_id
	 * @param user_email
	 * @param user_role
	 * @param login
	 * @param setup
	 */
	public LoginUser(String id, String email, String role, boolean status, boolean setup) {
		this.userId =  id;
		this.userEmail = email;
		this.userRole = role;
		this.loginStatus = status;
		this.setSetup(setup);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public boolean isLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(boolean loginStatus) {
		this.loginStatus = loginStatus;
	}

	public boolean getSetup() {
		return setup;
	}
	
	public void setSetup(boolean setup2) {
		this.setup = setup2;
	}
	
}
