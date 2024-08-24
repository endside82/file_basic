package com.endside.file.main;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class MainController {

    @GetMapping("/file/mng/hello")
    @ResponseBody
    public ResponseEntity<?> hello(){
        return ResponseEntity.ok("HELLO!");
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public void ping() {}

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void main() {}

    // auth token refresh
    @RequestMapping(value = {"/file/mng/jwt/hello"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<?> jwtConfirm(@RequestHeader(name = "Authorization") String authToken,
            Authentication authentication ) throws Exception {
        return ResponseEntity.ok().body("hello jwt");
    }
}
