package fr.eseo.tauri.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/")
public class IndexController {

    @GetMapping("")
    public String index() {
        try {
            Resource resource = new ClassPathResource("static/index.html");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error loading index.html";
        }
    }

    @GetMapping("/assets/{fileName}")
    public ResponseEntity<byte[]> getAsset(@PathVariable String fileName) {
        try {
            Resource resource = new ClassPathResource("static/assets/" + fileName);
            byte[] data = StreamUtils.copyToByteArray(resource.getInputStream());

            // Determine the media type based on the file extension
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (fileName.endsWith(".js")) {
                mediaType = MediaType.valueOf("application/javascript");
            } else if (fileName.endsWith(".css")) {
                mediaType = MediaType.valueOf("text/css");
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(data);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/{fileName:.+}", produces = "image/svg+xml")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) throws IOException {
        if (fileName.endsWith(".svg")) {
            Resource resource = new ClassPathResource("static/" + fileName);
            byte[] data = StreamUtils.copyToByteArray(resource.getInputStream());
            return ResponseEntity.ok().contentType(MediaType.valueOf("image/svg+xml")).body(data);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}