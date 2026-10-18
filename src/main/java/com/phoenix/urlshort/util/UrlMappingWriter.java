
package com.phoenix.urlshort.util;

import com.phoenix.urlshort.Repository.UrlMappingRepo;
import com.phoenix.urlshort.models.UrlMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlMappingWriter {

    private final UrlMappingRepo urlMappingRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveNewMapping(UrlMapping mapping) {
        urlMappingRepo.saveAndFlush(mapping);
    }
}
