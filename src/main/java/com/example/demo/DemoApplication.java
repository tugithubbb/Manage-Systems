package com.example.demo;

import com.example.demo.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableCaching
@SpringBootApplication
@RequiredArgsConstructor
public class DemoApplication implements CommandLineRunner {
	private final OrderService orderService;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("🚀 START RACE TEST");

		ExecutorService executor = Executors.newFixedThreadPool(4);

		executor.submit(() -> orderService.orderBeef("Restaurant A"));
		executor.submit(() -> orderService.orderBeef("Restaurant B"));
		executor.submit(() -> orderService.orderBeef("Restaurant C"));
		executor.submit(() -> orderService.orderBeef("Restaurant D"));
		executor.shutdown();
	}
}
