package com.example.customauthorination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
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
		registry.addInterceptor(new MyInterceptor(getAuthorizationService()));
	}

	@Bean(name="auth")
	public Map<CompositeKey, Set<String>> authorinzationConfigData() {
		Map<CompositeKey, Set<String>> map = new HashMap<>();
		Set<String>  set = Stream.of("112288", "332299").collect(Collectors.toCollection(HashSet::new));
		map.put(new CompositeKey("foo", CompositeKey.Method.GET), set);
		set = Stream.of("112288", "332299", "442288", "552299").collect(Collectors.toCollection(HashSet::new));
		map.put(new CompositeKey("foo", CompositeKey.Method.PATCH), set);
		return map;
	}

	@Bean
	public AuthorizationService getAuthorizationService() {
		return new AuthorizationService(authorinzationConfigData());
	}
}

class CompositeKey {

	enum Method { POST, GET, PUT, PATCH, DELETE }

	private String url;
	private Method method;

	public CompositeKey(String url, Method method) {
		this.url = url;
		this.method = method;
	}

	public CompositeKey(String url, String method) {
		this.url = url;
		this.method = Method.valueOf(method);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompositeKey that = (CompositeKey) o;
		return Objects.equals(url, that.url) &&
				method == that.method;
	}

	@Override
	public int hashCode() {

		return Objects.hash(url, method);
	}
}

//@Service
class AuthorizationService {

	private Map<CompositeKey, Set<String>> accessRestriction;

	public AuthorizationService(Map<CompositeKey, Set<String>> accessRestriction) {
		this.accessRestriction = accessRestriction;
	}

	public boolean isAuthorized(String url, String method, String clientID) {

		Set<String> accessGroup = accessRestriction.get(new CompositeKey(url, method));
		if(accessGroup == null) {
		    System.out.println("Key not found");
		    return false;
        }
        return accessGroup.contains(clientID) ? true : false;
	}
}

@RestController
class MyController {

	@GetMapping("/foo")
	public String getHappyData() {
		return "Having a happy day!";
	}

	@GetMapping("/boo")
	public String getMadData() {
		return "Having a mad day!";
	}
}

class MyInterceptor implements HandlerInterceptor {

	private AuthorizationService authorizationService;

	public MyInterceptor(AuthorizationService authorizationService) {
		this.authorizationService = authorizationService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String cid = request.getHeader("clientID");
		if(cid == null) {
			System.err.println("Client ID missing in the header");
			return false;
		}
		System.out.println("The request: " + request.getRequestURI() + ", and method: " + request.getMethod());

		if(authorizationService.isAuthorized(request.getRequestURI().substring(1), request.getMethod(), cid)){
			System.err.println(":::Authorized:::");
			return true;
		}else {
			System.out.println(":::Unauthorized:::");
			return false;
		}
	}
}
