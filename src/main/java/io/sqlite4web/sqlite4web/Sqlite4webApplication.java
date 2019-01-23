package io.sqlite4web.sqlite4web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("config")
@ComponentScan("io.sqlite4web")
public class Sqlite4webApplication {

	public static void main(String[] args) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		SpringApplication.run(Sqlite4webApplication.class, args);
	}

}

