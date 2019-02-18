package io.sqlite4web.api.impl;

import io.sqlite4web.api.BaseApi;
import io.sqlite4web.api.Constants;
import io.sqlite4web.api.util.UploadUtil;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;
import java.util.Random;

@Component
public class BaseApiImpl implements BaseApi {

    private Connection mConnection;

    @Override
    public String handleJsonRepresentation(String dbToken) {
        dbToken = dbToken.replace("dbToken=", "");
        return Objects.requireNonNull(constructJSON(dbToken)).toString();
    }

    @Override
    public ResponseEntity handleDbFileUpload(MultipartFile file) throws IOException {
        boolean fileSuccessfullySaved;

        String dbToken = generateToken();
        dbToken = UploadUtil.addFileExtension(file.getOriginalFilename(), dbToken);

        fileSuccessfullySaved = UploadUtil.saveDbFile(dbToken, file.getBytes());

        if (fileSuccessfullySaved) {
            // Return the token to the User
            JSONObject response = new JSONObject();
            response.put("dbToken", dbToken);

            return ResponseEntity.status(200).body(response.toString());
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<byte[]> handleDbFileDownload(String dbToken) {
        File f;
        FileInputStream fis;
        byte[] fileBytes;

        try {
            dbToken = dbToken.replace("dbToken=", "");

            f = new File(Constants.DATABASE_DIR + File.separator + dbToken);
            fis = new FileInputStream(f);
            fileBytes = new byte[(int) f.length()];

            IOUtils.readFully(fis, fileBytes);
        } catch (IOException ioException) {
            return ResponseEntity.badRequest().body("No database with this token found".getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbToken + "\"")
                .body(fileBytes);
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
     * Generates a random token that is going to become the newly uploaded
     * database file's name.
     *
     * @return The generated, random token
     */
    private String generateToken() {
        byte[] tokenBytes = new byte[8];

        // Fill array with random bytes and create (unique) token from it
        new Random().nextBytes(tokenBytes);
        return DatatypeConverter.printHexBinary(tokenBytes);
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
