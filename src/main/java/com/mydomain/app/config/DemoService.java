package com.mydomain.app.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

	@Autowired
	private DemoData demoData;
	
	@Value("${message}")
	private String message;
	
	public String sayMessage() {
		return message;
	}

	public Map<String, String> getLanguages() {
		return demoData.getMap();
	}
}
