
package com.phoenix.urlshort.service;

import com.phoenix.urlshort.Repository.UrlMappingRepo;
import com.phoenix.urlshort.models.UrlMapping;
import com.phoenix.urlshort.util.Base62Util;
import com.phoenix.urlshort.util.SHA256Helper;
import com.phoenix.urlshort.util.UrlMappingWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortService {

    private static final int MAX_ATTEMPTS = 10;

    private static final String KEY_BY_HASH = "keyByUrlHash";
    private static final String URL_BY_KEY = "urlByKey";

    private final UrlMappingRepo urlMappingRepo;
    private final UrlMappingWriter urlMappingWriter;
    private final CacheManager cacheManager;

    public Map<String, Object> urlShort(String url) {
        try {
            String urlHash = SHA256Helper.generateSHA(url);

            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                String cachedKey = getCached(KEY_BY_HASH, urlHash);
                if (cachedKey != null) {
                    return success(cachedKey);
                }
                Optional<String> existingKey =urlMappingRepo.getKeyByURLSha(urlHash);
                if (existingKey.isPresent()) {
                    String key = existingKey.get();
                    putCached(KEY_BY_HASH, urlHash, key);
                    putCached(URL_BY_KEY, key, url);
                    return success(key);
                }
                String key = Base62Util.generateKey();
                UrlMapping mapping = UrlMapping
                        .builder()
                        .url(url)
                        .urlHash(urlHash)
                        .mapping(key)
                        .build();

                try {
                    urlMappingWriter.saveNewMapping(mapping);
                    putCached(KEY_BY_HASH, urlHash, key);
                    putCached(URL_BY_KEY, key, url);
                    return success(key);
                } catch (DataIntegrityViolationException exception) {
                    if (!isDuplicateKeyViolation(exception)) {
                        throw exception;
                    }
                    log.debug(
                            "Duplicate-key conflict on attempt {} of {}.",
                            attempt,
                            MAX_ATTEMPTS
                    );
                }
            }
            return error("Unable to create a unique short URL. Please try again.");

        } catch (Exception exception) {
            log.error("Failed to shorten URL.", exception);
            return error("Failed to shorten URL.");
        }
    }

    public Optional<String> getOriginalUrl(String key) {
        try {
            String cachedUrl = getCached(URL_BY_KEY, key);
            if (cachedUrl != null) {
                return Optional.of(cachedUrl);
            }
            Optional<UrlMapping> result = urlMappingRepo.findByMapping(key);
            if (result.isEmpty()) {
                return Optional.empty();
            }
            UrlMapping mapping = result.get();
            putCached(
                    URL_BY_KEY,
                    mapping.getMapping(),
                    mapping.getUrl()
            );
            putCached(
                    KEY_BY_HASH,
                    mapping.getUrlHash(),
                    mapping.getMapping()
            );
            return Optional.of(mapping.getUrl());
        } catch (Exception exception) {
            log.error("Failed to resolve short key: {}", key, exception);
            throw new IllegalStateException(
                    "Failed to resolve short URL.",
                    exception
            );
        }
    }

    private String getCached(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return null;
            }
            return cache.get(key, String.class);
        } catch (RuntimeException exception) {
            log.warn(
                    "Cache read failed for '{}'. Falling back to MySQL.",
                    cacheName
            );
            log.debug("Cache read failure details.", exception);
            return null;
        }
    }

    private void putCached(
            String cacheName,
            String key,
            String value
    ) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }
        } catch (RuntimeException exception) {
            log.warn(
                    "Cache write failed for '{}'.",
                    cacheName
            );
            log.debug("Cache write failure details.", exception);
        }
    }

    private boolean isDuplicateKeyViolation(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof DuplicateKeyException) {
                return true;
            }
            if (current instanceof SQLException sqlException
                    && sqlException.getErrorCode() == 1062) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Map<String, Object> success(String key) {
        return Map.of(
                "isError", false,
                "message", key
        );
    }

    private Map<String, Object> error(String message) {
        return Map.of(
                "isError", true,
                "message", message
        );
    }
}
