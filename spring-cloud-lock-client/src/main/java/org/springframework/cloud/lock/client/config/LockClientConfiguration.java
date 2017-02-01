package org.springframework.cloud.lock.client.config;

import org.springframework.cloud.lock.client.LockClient;
import org.springframework.cloud.lock.client.RestLockClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@Configuration
@Import(LockClientProperties.class)
public class LockClientConfiguration {

	@Bean
	public LockClient lockClient(LockClientProperties lockClientProperties,
			Environment environment) {
		return new RestLockClient(lockClientProperties, environment);
	}
}
