package com.example.customauthorination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class CustomAuthorinationApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomAuthorinationApplication.class, args);
	}
}

@EnableWebMvc
@Configuration
class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new MyInterceptor( authorinzationConfigData()));
	}

	@Bean(name="auth")
	public Map<String, Set<String>> authorinzationConfigData() {
		Map<String, Set<String>> map = new HashMap<>();
		Set<String>  set = Stream.of("112288", "332299").collect(Collectors.toCollection(HashSet::new));
		map.put("me", set);
		set = Stream.of("442288", "552299").collect(Collectors.toCollection(HashSet::new));
		map.put("you", set);
		return map;
	}
}

@RestController
class MyController {

	@GetMapping("/me")
	public String getData() {

		return "Having a happy day!";
	}
}

class MyInterceptor implements HandlerInterceptor {

	private Map<String, Set<String>> authorinzation;

	public MyInterceptor(Map<String, Set<String>> authorinzation) {
		this.authorinzation = authorinzation;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		System.out.println("The request: " + request.getRequestURI() + ", and method: " + request.getMethod() + ", set size: " + authorinzation.size() + ", and the request " + (authorinzation.containsKey(request.getRequestURI().substring(1)) ? "is" : "is not"));
		return true;
	}
}
