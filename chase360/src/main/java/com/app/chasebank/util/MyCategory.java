package com.app.chasebank.util;

import java.io.Serializable;

public class MyCategory implements Serializable {
	private static final long serialVersionUID = -8635760467822856225L;
	private String id, name;
	
	/**
	 * Constructor
	 * @param id The category ID
	 * @param name The category Name
	 */
	public MyCategory(String id, String name) {
		// TODO Instantiate
		this.setId(id);
		this.setName(name);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "MyCategory{id: "+this.id+", name:"+this.name+"}";
	}
}
