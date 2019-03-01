package io.sqlite4web.api.v2;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface BaseApi {

    @RequestMapping(value = "/db/{dbToken}", method = RequestMethod.GET)
    String v2HandleJsonRepresentation(@PathVariable("dbToken") String dbToken);
}
