package org.springframework.cloud.test.lock.client;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.lock.client.EnableLockClient;
import org.springframework.cloud.lock.client.LockClient;
import org.springframework.cloud.test.lock.server.LockServerTestApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@EnableLockClient
@SpringBootApplication
@PropertySource("classpath:leader-event-publisher-test.properties")
public class RestLockClientTest {
	protected Log logger = LogFactory.getLog(getClass());

	private ConfigurableApplicationContext lockServer;
	private ConfigurableApplicationContext firstClient;
	private ConfigurableApplicationContext secondClient;

	@Before
	public void init() {
		// Running an embedded Lock Server.
		lockServer = SpringApplication.run(LockServerTestApplication.class,
				"--server.port=8666");

		// Launching a first lock client.
		firstClient = SpringApplication.run(RestLockClientTest.class);

		// Launching a second lock client.
		secondClient = SpringApplication.run(RestLockClientTest.class);
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void testLeaderElection() throws InterruptedException {
		boolean hasSingleLeader = firstClient
				.getBean(TestLockClientService.class).executed
						.get() != secondClient
								.getBean(TestLockClientService.class).executed.get();

		Assert.assertTrue("A single leader should have been assigned.", hasSingleLeader);

	}

	@After
	public void shutdown() {
		lockServer.close();
		firstClient.close();
		secondClient.close();
	}

	@Component
	public class TestLockClientService {
		private AtomicBoolean executed = new AtomicBoolean(false);

		public TestLockClientService(LockClient lockClient) {
			lockClient.executeIfOwned(new Runnable() {
				@Override
				public void run() {
					executed.set(true);
				}
			});
		}
	}
}
