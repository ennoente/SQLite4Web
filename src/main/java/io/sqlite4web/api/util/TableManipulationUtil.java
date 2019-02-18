package io.sqlite4web.api.util;

import org.json.JSONArray;

public class TableManipulationUtil {


    /**
     * Builds the SQL condition. Applies for cases when no primary key for the
     * table is available. Then a condition is built that (hopefully) mathes the
     * row exactly, and only that row.
     *
     * @param columnNames The JSONArray containing the column names
     * @param columnValues The JSONArray containing the row's specific values for each column
     * @return The neccessary SQL condition for mathing this row exactly
     */
    public static String buildSqlCondition(JSONArray columnNames, JSONArray columnValues) {
        StringBuilder commandBuilder = new StringBuilder();

        for (int i = 0; i < columnNames.length(); i++) {
            commandBuilder.append("\"");
            commandBuilder.append(columnNames.get(i).toString());
            commandBuilder.append("\"");

            commandBuilder.append(" IS ");
            commandBuilder.append("'");
            commandBuilder.append(columnValues.get(i).toString());
            commandBuilder.append("'");

            if (i < columnNames.length() - 1) commandBuilder.append(" AND ");
        }

        return commandBuilder.toString();
    }
}
