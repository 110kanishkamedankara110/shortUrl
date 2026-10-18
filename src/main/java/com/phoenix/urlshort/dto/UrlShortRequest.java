package com.phoenix.urlshort.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UrlShortRequest(
        @NotBlank(message = "URL cannot be blank")
        @URL(message = "Please Provide a valid URL")
        String url) {
}
