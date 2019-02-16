package io.sqlite4web;

import io.sqlite4web.api.Constants;
import io.swagger.annotations.Api;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;
import java.util.Random;




@RestController
@Api(value = "Upload Database", description = "Upload an existing Database file to the Server", tags = { "Upload Database" })
public class UploadController {


    /** Used for generating unique tokens */
    private final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890".toCharArray();
    private Random random = new Random();


    /** The JDBC connection to the database file. Different for each client */
    private Connection mConnection;



    /**
     * Returns the JSON representation of the associated file's contents.
     * The token must be provided as it is the file's name in the server's
     * file sysem.
     *
     * @param dbToken The database file's unique token
     * @return The database file's contents, represented in JSON
     */
    @RequestMapping(value = "/api", method = RequestMethod.GET)
    public String ui(@RequestParam String dbToken) {
        dbToken = dbToken.replace("dbToken=", "");
        return Objects.requireNonNull(constructJSON(dbToken)).toString();
    }



    /**
     * Generates a unique token for the database file and saves it locally.
     *
     * @param file The uploaded file as Multipart file parameter in the
     * HTTP request. The RequestParam name must be the same of the attribute
     * "name" in the input tag with type file.
     *
     * @return The JSON parsed from the database file
     */
    @RequestMapping(value = "/api/upload", method = RequestMethod.POST)
    @ResponseBody

    public String uploadFile (@RequestParam("file") MultipartFile file) {
        byte[] tokenBytes = new byte[8];

        String filename = file.getOriginalFilename();
        String dbToken;
        String path;
        String fileExtension;

        File dbFile;

        try {
            // Fill array with random bytes and create (unique) token from it
            random.nextBytes(tokenBytes);
            dbToken = DatatypeConverter.printHexBinary(tokenBytes);

            // Add the database's file extension to the token
            fileExtension = filename.substring(filename.lastIndexOf("."));
            dbToken += fileExtension;


            // Create new file inside the database directory with the token as its name
            path = Constants.DATABASE_DIR + File.separator + dbToken;
            dbFile = new File(path);
            dbFile.createNewFile();

            // Save the contents to the file
            BufferedOutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(dbFile));
            bufferedOut.write(file.getBytes());
            bufferedOut.flush();
            bufferedOut.close();
            System.out.println("Finished saving the file");

            // Return the token to the User
            JSONObject response = new JSONObject();
            response.put("dbToken", dbToken);

            return response.toString();


        } catch (IOException e) {
            e.printStackTrace();
            return "Bad Request" + File.separator + e.toString();
        }
    }



    /**
     * Constructs a JSON Object from
     *
     * @return The JSON containing metadata about and all data of the database file.
     */
    private JSONObject constructJSON(String dbToken) {
        JSONObject container = new JSONObject();
        JSONObject metaData = new JSONObject();
        JSONArray columnNames = new JSONArray();
        JSONArray columnDataTypes = new JSONArray();
        JSONArray table = new JSONArray();
        JSONArray row;

        ResultSet tableData;
        DatabaseMetaData dbMetaData;
        ResultSetMetaData tableMetaData;

        String path = Constants.DATABASE_DIR + File.separator + dbToken;
        String jdbcURL = "jdbc:sqlite:" + path;

        try {
            mConnection = DriverManager.getConnection(jdbcURL);

            tableData = mConnection.createStatement().executeQuery("SELECT * FROM " + getDatabaseTableName());

            dbMetaData = mConnection.getMetaData();
            tableMetaData = tableData.getMetaData();


            for (int i = 1; i <= tableMetaData.getColumnCount(); i++) {
                columnNames.put(tableMetaData.getColumnName(i));
                columnDataTypes.put(tableMetaData.getColumnTypeName(i));
            }

            while (tableData.next()) {
                row = convertResultSetToJSON(tableData, tableMetaData);
                table.put(row);
            }

            ResultSet primaryKeys = dbMetaData.getPrimaryKeys(null, null, getDatabaseTableName());
            String primaryKey = "null";
            if (primaryKeys != null && primaryKeys.next()) primaryKey = primaryKeys.getString("COLUMN_NAME");

            metaData.put("columnNames", columnNames);
            metaData.put("columnDataTypes", columnDataTypes);
            metaData.put("primaryKey", primaryKey);
            metaData.put("dbToken", dbToken);
            metaData.put("tableName", getDatabaseTableName());

            container.put("metadata", metaData);
            container.put("data", table);

            mConnection.close();

            return container;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * Converts the passed ResultSet into a JSON Object
     *
     *
     * @param rs The ResultSet containing the database file's content
     * @param md The ResultSetMetaData, used for identifying the datatype of any given column
     * @return The JSON representation of the database's table
     */
    private JSONArray convertResultSetToJSON(ResultSet rs, ResultSetMetaData md) {
        JSONArray row = new JSONArray();

        try {
            for (int i = 1; i <= md.getColumnCount(); i++) {

                if (md.getColumnType(i) == java.sql.Types.ARRAY) {
                    row.put(rs.getArray(i));
                } else if (md.getColumnType(i) == java.sql.Types.BIGINT) {
                    row.put(rs.getInt(i));
                } else if (md.getColumnType(i) == java.sql.Types.BOOLEAN) {
                    row.put(rs.getBoolean(i));
                } else if (md.getColumnType(i) == java.sql.Types.BLOB) {
                    row.put(rs.getBlob(i));
                } else if (md.getColumnType(i) == java.sql.Types.DOUBLE) {
                    row.put(rs.getDouble(i));
                } else if (md.getColumnType(i) == java.sql.Types.FLOAT) {
                    row.put(rs.getFloat(i));
                } else if (md.getColumnType(i) == java.sql.Types.INTEGER) {
                    row.put(rs.getInt(i));
                } else if (md.getColumnType(i) == java.sql.Types.NVARCHAR) {
                    row.put(rs.getNString(i));
                } else if (md.getColumnType(i) == java.sql.Types.VARCHAR) {
                    row.put(rs.getString(i));
                } else if (md.getColumnType(i) == java.sql.Types.TINYINT) {
                    row.put(rs.getInt(i));
                } else if (md.getColumnType(i) == java.sql.Types.SMALLINT) {
                    row.put(rs.getInt(i));
                } else if (md.getColumnType(i) == java.sql.Types.DATE) {
                    row.put(rs.getDate(i));
                } else if (md.getColumnType(i) == java.sql.Types.TIMESTAMP) {
                    row.put(rs.getTimestamp(i));
                } else {
                    row.put(rs.getObject(i));
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return row;
    }



    /**
     * Returns the database's (first(?)) table name
     * @return The database file's first table name
     */
    private String getDatabaseTableName() {
        try {
            DatabaseMetaData md = mConnection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            return rs.getString(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
