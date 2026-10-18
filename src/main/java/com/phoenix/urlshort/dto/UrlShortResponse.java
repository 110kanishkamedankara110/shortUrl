package com.phoenix.urlshort.dto;

public record UrlShortResponse(String shortUrl,Boolean isError,String message) {
}
