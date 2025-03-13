package com.brainchain.career_counselling.Career_Counselling.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

	private String token;
	private String role;
	private String email;
	
	public AuthResponse(String token,String role,String email) {
		this.token = token;
		this.role = role;
		this.email = email;
		
		
	}
}
