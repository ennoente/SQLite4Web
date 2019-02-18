package io.sqlite4web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@Api(value = "API v1", description = "Manipulate the database table", tags = "Table Manipulation API")
public interface TableManipulationApi {

    @RequestMapping(value = "api/update/cell", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Manipulates/Updates an individual table cell")
    ResponseEntity handleCellManipulation(
            @ApiParam(value = "The JSON Object containing all data and meta data.", required = true)
            @RequestBody String body)
            throws SQLException;
}
