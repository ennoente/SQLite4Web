package io.sqlite4web.sqlite4web.api;

import java.io.File;

public class Constants {
    public static final String BASE_DIR = System.getProperty("user.home") + File.separator + "SQLite4Web";

    public static final String DATABASE_DIR = BASE_DIR + File.separator + "databases";
}
