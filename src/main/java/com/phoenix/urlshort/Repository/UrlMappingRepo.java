package com.phoenix.urlshort.Repository;

import com.phoenix.urlshort.models.UrlMapping;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UrlMappingRepo extends JpaRepository<UrlMapping,UUID> {
    @Query("SELECT u.url FROM UrlMapping u WHERE u.urlHash=:urlHash")
    Optional<String> findByUrlHash(@Param("urlHash") String urlHash)throws Exception;

    @Query("SELECT u.mapping FROM UrlMapping u WHERE u.urlHash=:urlSha")
    Optional<String> getKeyByURLSha(@Param("urlSha") String urlSha);

    Optional<UrlMapping> findByMapping(String mapping);

}
