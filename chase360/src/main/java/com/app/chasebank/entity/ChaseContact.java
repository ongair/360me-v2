package com.app.chasebank.entity;

import java.io.Serializable;

import android.graphics.Bitmap;


public class ChaseContact implements Serializable{
	private static final long serialVersionUID = 8195706554682222586L;
	private String name, phone;
	private Bitmap bitmap;
	
	/**
	 * Class consttructor
	 * @param name The name of the Contact
	 * @param phone The phone Number associated with the contact
	 */
	public ChaseContact(String name, String phone) {
		this.name = name;
		this.phone = phone;
	}
	
	public ChaseContact(MyContact contact, Bitmap bitmap) {
		this.name = contact.getName();
		this.phone = contact.getPhone();
		this.bitmap = bitmap;
	}
	
	/**
	 * Set the name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Return the name of the contact
	 * @return {@link #name(String) }
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the phone number
	 * @param phone
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	/**
	 * Return the phone Number of the contact
	 * @return {@link #phone(String)}
	 */
	public String getPhone() {
		return phone;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	@Override
	public String toString() {
		return "Contact:{name="+name+", phone="+phone+"}";
	}
	
}
