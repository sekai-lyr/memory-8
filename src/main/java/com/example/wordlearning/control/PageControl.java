package com.example.wordlearning.control;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageControl {
    @GetMapping(value = {
            "/",
            "/login",
            "/register",
            "/dashboard",
            "/guide",
            "/daily-plan",
            "/public-words",
            "/practice",
            "/wrong-words",
            "/favorite-words",
            "/user-words",
            "/study-records",
            "/pro-center",
            "/pro-review",
            "/pro-reading",
            "/pro-goal",
            "/pro-dictation",
            "/pro-exam",
            "/membership",
            "/admin-monetization"
    }, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String page() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
