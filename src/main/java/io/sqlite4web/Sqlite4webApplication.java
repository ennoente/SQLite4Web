package io.sqlite4web;

import io.sqlite4web.api.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("io.sqlite4web.api")
@ComponentScan("io.sqlite4web.config")
@ComponentScan("io.sqlite4web")
public class Sqlite4webApplication {

	public static void main(String[] args) {
		setEnvVariables();

		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		SpringApplication.run(Sqlite4webApplication.class, args);
	}

	private static void setEnvVariables() {
		System.setProperty("sqlite4web.dir", Constants.BASE_DIR);
	}

}

