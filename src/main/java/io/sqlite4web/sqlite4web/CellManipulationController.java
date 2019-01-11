package io.sqlite4web.sqlite4web;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.sql.*;

@CrossOrigin(origins = "*")
@RestController
public class CellManipulationController {
    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + "SpringProjects" + File.separator + "databases";

    private Connection mConnection;



    // Order:
    // 1. Primary key
    // 2. Complete row
    // 3. Take the n'th row the client chose (later version, not yet)
    @RequestMapping(value = "api/update/cell", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateCell(@RequestBody String body) throws SQLException {
        boolean success = false;

        JSONObject jsonBody = new JSONObject(body);

        String dbToken = jsonBody.getString("dbToken");
        String primaryKey = jsonBody.getString("primaryKey");
        String primaryKeyValue = "";
        String tableName = jsonBody.getString("tableName");
        String columnName = jsonBody.getString("columnName");
        String newValue = jsonBody.getString("newValue");

        JSONArray columnNames = jsonBody.getJSONArray("columnNames");
        JSONArray columnValues = jsonBody.getJSONArray("columnValues");
        JSONArray columnDataTypes = jsonBody.getJSONArray("columnDataTypes");

        mConnection = DriverManager.getConnection("jdbc:sqlite:" + BASE_DIR + File.separator + dbToken);

        System.out.println(jsonBody.toString(4));

        StringBuilder valuesCommandBuilder = new StringBuilder();

        if (columnNames.length() != columnValues.length()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Column names and Column values are of different lengths");



        for (int i = 0; i < columnNames.length(); i++) {
            valuesCommandBuilder.append("'");
            valuesCommandBuilder.append(columnNames.get(i).toString());
            valuesCommandBuilder.append("'");

            valuesCommandBuilder.append(" IS ");

            if (isStringBasedDataType(columnDataTypes.get(i).toString()))
                valuesCommandBuilder.append("'");

            valuesCommandBuilder.append(columnValues.get(i).toString());

            if (isStringBasedDataType(columnDataTypes.get(i).toString()))
                valuesCommandBuilder.append("'");

            if (i < columnNames.length() - 1) valuesCommandBuilder.append(" AND ");
        }

        /*
        for (int i = 0; i < row.length(); i++) {
            valuesCommandBuilder.append(row.get(i));

            if (i < row.length()-1) valuesCommandBuilder.append(",");
        }
        */


        if (primaryKey != null && !primaryKey.equalsIgnoreCase("null")) {
            System.out.println("Primary Key is NOT null! Updating by primary key...");
            //mConnection.createStatement().execute("UPDATE " + tableName +
            //        " VALUES (" + valuesCommandBuilder + ")" +
            //        " WHERE " + primaryKey + " IS " + primaryKeyValue);
            mConnection.createStatement().execute("UPDATE " + tableName +
                    " SET '" + columnName + "'=" + newValue +
                    " WHERE " + primaryKey + " IS " + primaryKeyValue);
        } else {
            // primary key is null
            String condition = valuesCommandBuilder.toString();

            String updateQuery = "UPDATE " + tableName +
                    " SET '" + columnName + "'=" + "'" + newValue + "'" +
                    " WHERE " + condition +
                    ";";

            System.out.println("Update query: " + updateQuery);

            success = mConnection.createStatement().execute(updateQuery);


        }
        mConnection.close();
        return new ResponseEntity(HttpStatus.OK);
        //if (success) {
        //    System.out.println("Closed connection. Returning HTTP OK");
        //    return new ResponseEntity(HttpStatus.OK);
        //} else {
        //    System.out.println("Closed connection. Returning HTTP FAIL");
        //    return ResponseEntity.status(HttpStatus.CONFLICT).body("Request itself was good, but the SQL query failed.");
        //}
        //return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
    }

    private boolean isStringBasedDataType(String datatype) {
        return datatype.equalsIgnoreCase("NVARCHAR")
                || datatype.equalsIgnoreCase("VARCHAR")
                || datatype.equalsIgnoreCase("LONGNVARCHAR")
                || datatype.equalsIgnoreCase("TEXT");
    }
}
