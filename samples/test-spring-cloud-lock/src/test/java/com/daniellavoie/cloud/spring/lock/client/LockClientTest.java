package com.daniellavoie.cloud.spring.lock.client;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.lock.client.LockClient;
import org.springframework.cloud.lock.commons.Lock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LockClientApplication.class)
public class LockClientTest {
	@Autowired
	private LockClient lockClient;

	@Before
	public void init() {
		Iterator<Lock> iterator = lockClient.locks().iterator();
		while (iterator.hasNext()) {
			Lock lock = iterator.next();
			lockClient.release(lock.getName(), lock.getValue());
		}
	}

	@Test
	public void testLockClient() {
		Callable<Lock> callable = new Callable<Lock>() {
			@Override
			public Lock call() throws Exception {
				// TODO Auto-generated method stub
				return lockClient.create("test");
			}
		};

		try {
			List<Future<Lock>> futures = Executors.newFixedThreadPool(2)
					.invokeAll(Arrays.asList(callable, callable));

			int lockCount = 0;
			for (Future<Lock> future : futures) {
				try {
					future.get();

					lockCount++;
				}
				catch (ExecutionException executionEx) {
					// Do nothing.
				}
			}

			Assert.assertEquals("A single lock should have been acquired.", 1, lockCount);
		}
		catch (InterruptedException interruptedEx) {
			throw new RuntimeException(interruptedEx);
		}

	}
}
