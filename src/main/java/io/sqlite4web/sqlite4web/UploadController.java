package io.sqlite4web.sqlite4web;

import io.sqlite4web.sqlite4web.api.Constants;
import org.json.JSONArray;
import org.json.JSONException;
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

@CrossOrigin(origins = "*")
@RestController
public class UploadController {
    private final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890".toCharArray();

    private Random random = new Random();
    private Connection mConnection;


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/api", method = RequestMethod.GET)
    public String ui(@RequestParam String dbToken) {
        dbToken = dbToken.replace("dbToken=", "");
        System.out.println("Received API call for '/api' with the dbToken '" + dbToken + "'");

        String path = Constants.DATABASE_DIR + File.separator + dbToken;
        return Objects.requireNonNull(constructJSON(path, dbToken)).toString();
    }


    /**
     * POST /uploadFile -> receive and locally save a file, return JSON
     *
     * @param file The uploaded file as Multipart file parameter in the
     * HTTP request. The RequestParam name must be the same of the attribute
     * "name" in the input tag with type file.
     *
     * @return The JSON parsed from the database file
     */
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/api/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile (
            @RequestParam("file") MultipartFile file) {
        System.out.println(file.getName());
        System.out.println(file.getOriginalFilename());
        String filename = file.getOriginalFilename();
        String dbToken;
        try {
            System.out.println(file.getBytes().length);

            byte[] tokenBytes = new byte[8];
            random.nextBytes(tokenBytes);
            dbToken = DatatypeConverter.printHexBinary(tokenBytes);
            System.out.println("New Token: '" + dbToken + "'");

            String fileExtension = filename.substring(filename.lastIndexOf("."));

            dbToken += fileExtension;

            String path = Constants.DATABASE_DIR + File.separator + dbToken;

            System.out.println(path);
            File f = new File(path);
            f.createNewFile();

            BufferedOutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(f));
            bufferedOut.write(file.getBytes());
            bufferedOut.flush();
            bufferedOut.close();
            System.out.println("Finished saving the file");

            JSONObject response = new JSONObject();
            response.put("dbToken", dbToken);

            return response.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "Bad Request" + File.separator + e.toString();
        }
    }


    /**
     * {
     *     "metadata": {
     *         "columnNames": [
     *              "cname1",
     *              "cname2"
     *         ],
     *         "primaryKey" : "key_value"
     *     },
     *     "data": [
     *         [
     *              "column1value",
     *              "column2value"
     *         ],
     *         [
     *              "column1value2",
     *              "column2value2"
     *         ]
     *     ]
     * }
     *
     *
     * @param path The DB file's path
     * @return The JSON containing metadata about and all data of the database file.
     */
    private JSONObject constructJSON(String path, String dbToken) {
        JSONObject container = new JSONObject();
        JSONObject metaData = new JSONObject();
        JSONArray columnNames = new JSONArray();
        JSONArray columnDataTypes = new JSONArray();
        JSONArray table = new JSONArray();
        JSONArray row;

        ResultSet tableData;
        DatabaseMetaData dbMetaData;
        ResultSetMetaData tableMetaData;

        try {
            mConnection = DriverManager.getConnection("jdbc:sqlite:" + path);

            tableData = mConnection.createStatement().executeQuery("SELECT * FROM " + getDatabaseTableName());

            dbMetaData = mConnection.getMetaData();
            tableMetaData = tableData.getMetaData();


            for (int i = 1; i <= tableMetaData.getColumnCount(); i++) {
                columnNames.put(tableMetaData.getColumnName(i));
                columnDataTypes.put(tableMetaData.getColumnTypeName(i));
            }

            while (tableData.next()) {
                row = convertResultSetToJSON(tableData, tableMetaData);
                //System.out.println(tableData.getString(1));
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


    public String getDatabaseTableName() {
        try {
            DatabaseMetaData databaseMetaData = mConnection.getMetaData();

            DatabaseMetaData md = mConnection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            return rs.getString(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
