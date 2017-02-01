/*
 * Copyright 2017 the original author or authors.
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
 *
 */
package org.springframework.cloud.lock.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.lock.server.lock.LockController;
import org.springframework.cloud.lock.server.lock.service.LockService;
import org.springframework.cloud.lock.server.lock.service.RedisLockService;
import org.springframework.cloud.lock.server.lock.service.SimpleLockService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author Daniel Lavoie
 */
@Configuration
public class LockServerConfiguration {
	class Marker {
	}

	@Bean
	public Marker enableLockServerMarker() {
		return new Marker();
	}

	@Bean
	public LockController locksController(LockService lockService) {
		return new LockController(lockService);
	}

	@ConditionalOnClass(RedisConnectionFactory.class)
	@ConditionalOnBean(RedisConnectionFactory.class)
	@Configuration
	protected static class RedisLockServiceConfiguration {
		@Bean
		@ConditionalOnMissingBean(LockService.class)
		public RedisLockService lockService(RedisConnectionFactory connectionFactory) {
			return new RedisLockService(connectionFactory);
		}
	}

	@ConditionalOnClass(RedisConnectionFactory.class)
	@ConditionalOnMissingBean(RedisConnectionFactory.class)
	@Configuration
	protected static class FallbackSimpleLockServiceConfiguration {
		@Bean
		@ConditionalOnMissingBean(LockService.class)
		public SimpleLockService lockService() {
			return new SimpleLockService();
		}
	}

	@ConditionalOnMissingClass("org.springframework.data.redis.connection.RedisConnectionFactory")
	@Configuration
	protected static class SimpleLockServiceConfiguration {
		@Bean
		@ConditionalOnMissingBean(LockService.class)
		public SimpleLockService lockService() {
			return new SimpleLockService();
		}
	}

}
