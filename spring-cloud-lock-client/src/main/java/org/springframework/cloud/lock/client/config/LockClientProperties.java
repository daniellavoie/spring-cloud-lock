/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.lock.client.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Dave Syer
 * @author Daniel Lavoie
 *
 */
@ConfigurationProperties(LockClientProperties.PREFIX)
public class LockClientProperties {
	public static final String PREFIX = "spring.cloud.lock";

	/**
	 * Flag to say that lock client is enabled. Default true;
	 */
	private boolean enabled = true;

	/**
	 * The username to use (HTTP Basic) when contacting the remote server.
	 */
	private String username;

	/**
	 * The password to use (HTTP Basic) when contacting the remote server.
	 */
	private String password;

	/**
	 * The URI of the remote server (default http://localhost:8666).
	 */
	private String uri = "http://localhost:8666";

	/**
	 * Discovery properties.
	 */
	private Discovery discovery = new Discovery();

	/**
	 * Flag to indicate that failure to connect to the server is fatal (default false).
	 */
	private boolean failFast = false;

	/**
	 * Security Token passed thru to underlying environment repository.
	 */
	private String token;

	/**
	 * Authorization token used by the client to connect to the server.
	 */
	private String authorization;

	public LockClientProperties() {
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getRawUri() {
		return extractCredentials()[2];
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String url) {
		this.uri = url;
	}

	public String getUsername() {
		return extractCredentials()[0];
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return extractCredentials()[1];
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Discovery getDiscovery() {
		return this.discovery;
	}

	public void setDiscovery(Discovery discovery) {
		this.discovery = discovery;
	}

	public boolean isFailFast() {
		return this.failFast;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAuthorization() {
		return this.authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	private String[] extractCredentials() {
		String[] result = new String[3];
		String uri = this.uri;
		result[2] = uri;
		String[] creds = getUsernamePassword();
		result[0] = creds[0];
		result[1] = creds[1];
		try {
			URL url = new URL(uri);
			String userInfo = url.getUserInfo();
			if (StringUtils.isEmpty(userInfo) || ":".equals(userInfo)) {
				return result;
			}
			String bare = UriComponentsBuilder.fromHttpUrl(uri).userInfo(null).build()
					.toUriString();
			result[2] = bare;
			if (!userInfo.contains(":")) {
				userInfo = userInfo + ":";
			}
			String[] split = userInfo.split(":");
			result[0] = split[0];
			result[1] = split[1];
			if (creds[1] != null) {
				// Explicit username / password takes precedence
				result[1] = creds[1];
				if ("user".equals(creds[0])) {
					// But the username can be overridden
					result[0] = split[0];
				}
			}
			return result;
		}
		catch (MalformedURLException e) {
			throw new IllegalStateException("Invalid URL: " + uri);
		}
	}

	private String[] getUsernamePassword() {
		if (StringUtils.hasText(this.password)) {
			return new String[] {
					StringUtils.hasText(this.username) ? this.username.trim() : "user",
					this.password.trim() };
		}
		return new String[2];
	}

	public static class Discovery {
		public static final String DEFAULT_CONFIG_SERVER = "lockserver";

		/**
		 * Flag to indicate that config server discovery is enabled (config server URL
		 * will be looked up via discovery).
		 */
		private boolean enabled;
		/**
		 * Service id to locate config server.
		 */
		private String serviceId = DEFAULT_CONFIG_SERVER;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getServiceId() {
			return this.serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

	}

	public LockClientProperties override(
			org.springframework.core.env.Environment environment) {
		LockClientProperties override = new LockClientProperties();
		BeanUtils.copyProperties(this, override);

		return override;
	}

	@Override
	public String toString() {
		return "LockClientProperties [enabled=" + enabled + ", username=" + username
				+ ", password=" + password + ", uri=" + uri + ", discovery=" + discovery
				+ ", failFast=" + failFast + ", token=" + token + ", authorization="
				+ authorization + "]";
	}
}
