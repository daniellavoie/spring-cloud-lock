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
 */package org.springframework.cloud.lock.server.lock;

import java.util.Collections;
import java.util.Map;

import org.springframework.cloud.lock.commons.Lock;
import org.springframework.cloud.lock.commons.LockErrorType;
import org.springframework.cloud.lock.server.lock.exception.LockExistsException;
import org.springframework.cloud.lock.server.lock.exception.LockNotHeldException;
import org.springframework.cloud.lock.server.lock.exception.NoSuchLockException;
import org.springframework.cloud.lock.server.lock.service.LockService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Dave Syer
 * @author Daniel Lavoie
 */
@RestController
@RequestMapping(method = RequestMethod.GET, path = "${spring.cloud.lock.server.prefix:}/lock")
public class LockController {
	private final LockService service;

	public LockController(LockService service) {
		this.service = service;
	}

	@RequestMapping(method = RequestMethod.GET)
	public Iterable<Lock> locks() {
		return service.findAll();
	}

	@RequestMapping(value = "{name}", method = RequestMethod.POST)
	public Lock create(@PathVariable String name) {
		return service.create(name);
	}

	@RequestMapping(value = "{name}/{value}", method = RequestMethod.DELETE)
	public Map<String, Object> release(@PathVariable String name,
			@PathVariable String value) {
		if (!service.release(name, value)) {
			throw new NoSuchLockException();
		}
		return Collections.singletonMap("status", (Object) "OK");
	}

	@RequestMapping(value = "{name}/{value}", method = RequestMethod.PUT)
	public Lock refresh(@PathVariable String name, @PathVariable String value) {
		return service.refresh(name, value);
	}

	@ExceptionHandler(LockExistsException.class)
	@ResponseBody
	public ResponseEntity<Void> lockExists() {
		// TODO Write a proper MVC test.
		HttpHeaders headers = new HttpHeaders();
		headers.add("error-type", LockErrorType.LOCK_ALREADY_EXISTS.toString());
		return new ResponseEntity<Void>(headers, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoSuchLockException.class)
	@ResponseBody
	public ResponseEntity<Void> noSuchLock() {
		// TODO Write a proper MVC test.
		HttpHeaders headers = new HttpHeaders();
		headers.add("error-type", LockErrorType.LOCK_NOT_FOUND.toString());
		return new ResponseEntity<Void>(headers, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(LockNotHeldException.class)
	@ResponseBody
	public ResponseEntity<Void> lockNotHeld() {
		// TODO Write a proper MVC test.
		HttpHeaders headers = new HttpHeaders();
		headers.add("error-type", LockErrorType.LOCK_NOT_HELD.toString());
		return new ResponseEntity<Void>(headers, HttpStatus.NOT_FOUND);
	}
}
