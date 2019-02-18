package io.sqlite4web.api.impl;

import io.sqlite4web.api.Constants;
import io.sqlite4web.api.TableManipulationApi;
import io.sqlite4web.api.util.TableManipulationUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class TableManipulationApiImpl implements TableManipulationApi {

    @Override
    public ResponseEntity handleCellManipulation(String body) throws SQLException {


        JSONObject jsonBody = new JSONObject(body);

        JSONArray columnNames = jsonBody.getJSONArray("columnNames");
        JSONArray columnValues = jsonBody.getJSONArray("columnValues");

        String dbToken = jsonBody.getString("dbToken");
        String primaryKey = jsonBody.getString("primaryKey");
        String primaryKeyValue = "";
        String tableName = jsonBody.getString("tableName");
        String columnName = jsonBody.getString("columnName");
        String newValue = jsonBody.getString("newValue");
        String condition = TableManipulationUtil.buildSqlCondition(columnNames, columnValues);

        Connection mConnection = DriverManager.getConnection("jdbc:sqlite:" + Constants.DATABASE_DIR + File.separator + dbToken);


        if (primaryKey != null && !primaryKey.equalsIgnoreCase("null")) {
            System.out.println("Primary Key is not null! Updating by primary key...");
            mConnection.createStatement().execute("UPDATE " + tableName +
                    " SET '" + columnName + "'=" + newValue +
                    " WHERE " + primaryKey + " IS " + primaryKeyValue);
        } else {
            // primary key is null
            System.out.println("Primary Key is null! Updating by row...");

            String updateQuery = "UPDATE " + tableName +
                    " SET \"" + columnName + "\"=" + "'" + newValue + "'" +
                    " WHERE " + condition +
                    ";";

            mConnection.createStatement().execute(updateQuery);
        }
        mConnection.close();
        return ResponseEntity.status(200).body("Good Job! :)");
    }
}
