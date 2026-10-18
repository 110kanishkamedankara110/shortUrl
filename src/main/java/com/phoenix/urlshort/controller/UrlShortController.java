package com.phoenix.urlshort.controller;

import com.phoenix.urlshort.dto.UrlShortRequest;
import com.phoenix.urlshort.dto.UrlShortResponse;
import com.phoenix.urlshort.service.UrlShortService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UrlShortController {

    final UrlShortService urlShortService;

    @PostMapping("/api/v1/createShortUrl")
    public ResponseEntity<UrlShortResponse> createShortUrl(
            @Valid
            @RequestBody
            UrlShortRequest urlShortRequest
    ) {
        Map<String,Object> resp=urlShortService.urlShort(urlShortRequest.url());
        if((Boolean)resp.get("isError")==true)return ResponseEntity.badRequest().body(new UrlShortResponse("",true,(String) resp.get("message")));

        String shortUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/{key}")
                .buildAndExpand(resp.get("message"))
                .toUriString();

        return ResponseEntity.ok(new UrlShortResponse(shortUrl,false,"success"));
    }



    @GetMapping("/{key}")
    public ResponseEntity<Void> redirect(@PathVariable String key) {
        Optional<String> resp = urlShortService.getOriginalUrl(key);

        return resp.<ResponseEntity<Void>>map(s -> ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(s))
                .build()).orElseGet(() -> ResponseEntity.notFound().build());

    }

}
