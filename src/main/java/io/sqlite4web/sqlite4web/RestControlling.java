package io.sqlite4web.sqlite4web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.*;
import java.net.ConnectException;
import java.nio.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

@CrossOrigin(origins = "*")
@RestController
public class RestControlling {
    /**
     * POST /uploadFile -> receive and locally save a file, return JSON
     *
     * @param file The uploaded file as Multipart file parameter in the
     * HTTP request. The RequestParam name must be the same of the attribute
     * "name" in the input tag with type file.
     *
     * @return The JSON parsed from the database file
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile(
            @RequestParam("file") MultipartFile file) {
        System.out.println(file.getName());
        System.out.println(file.getOriginalFilename());
        try {
            System.out.println(file.getBytes().length);

            String filename = file.getOriginalFilename();
            String path = System.getProperty("user.home") + File.separator + "SpringProjects" + File.separator + "sqlite4web" + File.separator + filename;

            BufferedOutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(new File(path)));
            bufferedOut.write(file.getBytes());
            bufferedOut.flush();
            bufferedOut.close();
            System.out.println("Finished saving the file");

            System.out.println(constructJSON(path).toString(4));
            return constructJSON(path).toString();

        } catch (IOException e) {
            e.printStackTrace();
        }


        JSONObject object = new JSONObject();
        object.put("key1", "Schlüssel 1");
        object.put("Greeting", "Formal Greeting with friendly handshake");

        return object.toString();
    }

    private JSONObject constructJSON(String path) {
        JSONObject container = new JSONObject();
        JSONArray metaData = new JSONArray();
        JSONArray table = new JSONArray();
        Connection c = null;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:" + path);

            ResultSet rs = c.createStatement().executeQuery("SELECT * FROM " + getDatabaseTableName(c));
            ResultSetMetaData md = rs.getMetaData();

            // Construct meta data JSONArray
            for (int i = 1; i <= md.getColumnCount(); i++) {
                metaData.put(md.getColumnName(i));
            }

            JSONArray row;
            do {
                row = new JSONArray();
                System.out.println(rs.getString(1));

                for (int i = 1; i <= md.getColumnCount(); i++) {

                    if(md.getColumnType(i)==java.sql.Types.ARRAY) {
                        row.put(rs.getArray(i));
                    } else if(md.getColumnType(i) == java.sql.Types.BIGINT) {
                        row.put(rs.getInt(i));
                    } else if(md.getColumnType(i) == java.sql.Types.BOOLEAN) {
                        row.put(rs.getBoolean(i));
                    } else if(md.getColumnType(i) == java.sql.Types.BLOB) {
                        row.put(rs.getBlob(i));
                    } else if(md.getColumnType(i) == java.sql.Types.DOUBLE) {
                        row.put(rs.getDouble(i));
                    } else if(md.getColumnType(i) == java.sql.Types.FLOAT) {
                        row.put(rs.getFloat(i));
                    } else if(md.getColumnType(i) == java.sql.Types.INTEGER) {
                        row.put(rs.getInt(i));
                    } else if(md.getColumnType(i) == java.sql.Types.NVARCHAR) {
                        row.put(rs.getNString(i));
                    } else if(md.getColumnType(i) == java.sql.Types.VARCHAR) {
                        row.put(rs.getString(i));
                    } else if(md.getColumnType(i) == java.sql.Types.TINYINT) {
                        row.put(rs.getInt(i));
                    } else if(md.getColumnType(i) == java.sql.Types.SMALLINT) {
                        row.put(rs.getInt(i));
                    } else if(md.getColumnType(i) == java.sql.Types.DATE) {
                        row.put(rs.getDate(i));
                    } else if(md.getColumnType(i) == java.sql.Types.TIMESTAMP) {
                        row.put(rs.getTimestamp(i));
                    } else{
                        row.put(rs.getObject(i));
                    }
                }
                table.put(row);
            } while(rs.next());
            container.put("metadata", metaData);
            container.put("data", table);
            return container;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONArray convert(ResultSet rs) throws SQLException, JSONException {
        JSONArray array = new JSONArray();
        JSONArray columnNames = new JSONArray();
        ResultSetMetaData md = rs.getMetaData();

        for (int i = 0; i < md.getColumnCount(); i++) {
            columnNames.put(md.getColumnName(i));
        }
        return array;
    }

    public static String getDatabaseTableName(Connection c) {
        try {
            DatabaseMetaData databaseMetaData = c.getMetaData();

            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            return rs.getString(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    public JSONArray convert(ResultSet rs ) throws SQLException, JSONException {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for (int i=1; i<numColumns+1; i++) {
                String column_name = rsmd.getColumnName(i);

                if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
                    obj.put(column_name, rs.getArray(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                    obj.put(column_name, rs.getBoolean(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
                    obj.put(column_name, rs.getBlob(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                    obj.put(column_name, rs.getDouble(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                    obj.put(column_name, rs.getFloat(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                    obj.put(column_name, rs.getNString(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                    obj.put(column_name, rs.getString(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                    obj.put(column_name, rs.getDate(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                    obj.put(column_name, rs.getTimestamp(column_name));
                }
                else{
                    obj.put(column_name, rs.getObject(column_name));
                }
            }

            json.put(obj);
        }

        return json;
    }
    */
}
