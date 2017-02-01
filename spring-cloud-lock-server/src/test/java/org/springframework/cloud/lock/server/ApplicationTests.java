package org.springframework.cloud.lock.server;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LockServerApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void locksLoad() {
		@SuppressWarnings("rawtypes")
		ResponseEntity<List> entity = testRestTemplate.getForEntity("/lock", List.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
	}

	@Test
	public void createLock() {
		@SuppressWarnings("rawtypes")
		ResponseEntity<Map> entity = testRestTemplate.postForEntity("/lock/foo", "bar",
				Map.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
	}

}
