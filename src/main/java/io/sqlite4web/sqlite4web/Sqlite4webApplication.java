package io.sqlite4web.sqlite4web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.sql.*;

@SpringBootApplication
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

