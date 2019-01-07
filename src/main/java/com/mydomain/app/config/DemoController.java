package com.mydomain.app.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
public class DemoController {
	
	@Autowired
	private DemoService demoService;

	@GetMapping("/v1/say")
	public ResponseEntity<String> sayMessage(WebRequest request) {
		String say = demoService.sayMessage();
		return new ResponseEntity<String>(say, HttpStatus.OK);
	}

	@GetMapping("/v1/languages")
	public ResponseEntity<Map<String, String>> getLanguages(WebRequest request) {
		Map<String, String> map = demoService.getLanguages();
		return new ResponseEntity<Map<String, String>>(map, HttpStatus.OK);
	}
}
