package io.sqlite4web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Api(value = "API v1", description = "Upload, download and get JSON representation of database file", tags = { "Base API" })
public interface BaseApi {

    @ApiOperation(value = "Sends back the JSON operation of the table", notes = "Assumes only one table")
    @RequestMapping(value = "/api", method = RequestMethod.GET)
    String handleJsonRepresentation(
            @ApiParam(value = "The database file's unique identifier", required = true, example = "0CDBD356D78F9066.txt")
            @RequestParam String dbToken);


    @RequestMapping(value = "/api/upload", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Saves the database file and sends back its unique database token as JSON")
    ResponseEntity handleDbFileUpload(
            @ApiParam(value = "The database file, as MultipartFile / FormData", required = true) @RequestParam("file")
                    MultipartFile file) throws IOException;



    @RequestMapping(method = RequestMethod.POST, value = "/api/download")
    @ApiOperation(value = "Returns the database file")
    ResponseEntity<byte[]> handleDbFileDownload(
            @ApiParam(value = "The unique database token", required = true, example = "0CDBD356D78F9066.txt")
            @RequestParam String dbToken);
}
