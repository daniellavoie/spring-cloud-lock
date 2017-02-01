package org.springframework.cloud.lock.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.springframework.cloud.lock.client.config.LockClientProperties;
import org.springframework.cloud.lock.commons.Lock;
import org.springframework.cloud.lock.server.lock.exception.LockExistsException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @author Dave Syer
 * @author Daniel Lavoie
 *
 */
public class RestLockClient extends AbstractLockClient {
	private LockClientProperties defaultProperties;
	private Environment environment;

	private RestTemplate restTemplate;

	public RestLockClient(LockClientProperties lockClientProperties,
			Environment environment) {
		super(environment.resolveRequiredPlaceholders("${spring.application.name:application}"));

		this.defaultProperties = lockClientProperties;
		this.environment = environment;
	}

	private RestTemplate getRestTemplate(LockClientProperties properties) {
		return this.restTemplate == null ? getSecureRestTemplate(properties)
				: this.restTemplate;
	}

	private RestTemplate getSecureRestTemplate(LockClientProperties client) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setReadTimeout((60 * 1000 * 3) + 5000); // TODO 3m5s, make
																// configurable?
		RestTemplate template = new RestTemplate(requestFactory);
		String password = client.getPassword();
		String authorization = client.getAuthorization();

		if (password != null && authorization != null) {
			throw new IllegalStateException(
					"You must set either 'password' or 'authorization'");
		}

		if (password != null) {
			template.setInterceptors(Arrays.<ClientHttpRequestInterceptor> asList(
					new BasicAuthorizationInterceptor(client.getUsername(), password)));
		}
		else if (authorization != null) {
			template.setInterceptors(Arrays.<ClientHttpRequestInterceptor> asList(
					new GenericAuthorization(authorization)));
		}

		return template;
	}

	@Override
	public Iterable<Lock> locks() {
		LockClientProperties properties = getProperties();

		return getRestTemplate(properties).exchange(properties.getUri() + "/lock",
				HttpMethod.GET, null, new ParameterizedTypeReference<Iterable<Lock>>() {
				}).getBody();
	}

	@Override
	public Lock create(String name) throws LockExistsException {
		LockClientProperties properties = getProperties();

		return getRestTemplate(properties).exchange(properties.getUri() + "/lock/" + name,
				HttpMethod.POST, null, Lock.class).getBody();
	}

	private LockClientProperties getProperties() {
		return this.defaultProperties.override(environment);
	}

	/*
	 * 
	 * @RequestMapping(value = "{name}/{value}", method = RequestMethod.DELETE) public
	 * Map<String, Object> release(@PathVariable String name,
	 * 
	 * @PathVariable String value) {
	 */
	@Override
	public Map<String, Object> release(String name, String value) {
		LockClientProperties properties = getProperties();

		return getRestTemplate(properties).exchange(
				properties.getUri() + "/lock/" + name + "/" + value, HttpMethod.DELETE,
				null, new ParameterizedTypeReference<Map<String, Object>>() {
				}).getBody();
	}

	@Override
	public Lock refresh(String name, String value) {
		LockClientProperties properties = getProperties();

		return getRestTemplate(properties)
				.exchange(properties.getUri() + "/lock/" + name + "/" + value,
						HttpMethod.PUT, null, new ParameterizedTypeReference<Lock>() {
						})
				.getBody();
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	private static class GenericAuthorization implements ClientHttpRequestInterceptor {
		private final String authorizationToken;

		public GenericAuthorization(String authorizationToken) {
			this.authorizationToken = (authorizationToken == null ? ""
					: authorizationToken);
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body,
				ClientHttpRequestExecution execution) throws IOException {
			request.getHeaders().add("Authorization", authorizationToken);
			return execution.execute(request, body);
		}
	}
}
