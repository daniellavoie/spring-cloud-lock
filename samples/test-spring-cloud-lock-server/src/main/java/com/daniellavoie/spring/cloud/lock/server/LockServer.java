package com.daniellavoie.spring.cloud.lock.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.lock.server.EnableLockServer;

@EnableLockServer
@SpringBootApplication
public class LockServer {
	public static void main(String[] args) {
		SpringApplication.run(LockServer.class, args);
	}
}