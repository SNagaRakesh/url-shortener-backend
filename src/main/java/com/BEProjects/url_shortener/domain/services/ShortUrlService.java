package com.BEProjects.url_shortener.domain.services;

import com.BEProjects.url_shortener.ApplicationProperties;
import com.BEProjects.url_shortener.domain.entities.ShortUrl;
import com.BEProjects.url_shortener.domain.models.CreateShortUrlCmd;
import com.BEProjects.url_shortener.domain.models.ShortUrlDto;
import com.BEProjects.url_shortener.domain.repositories.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ShortUrlService {
    private final ShortUrlRepository shortUrlRepository;
    private final EntityMapper entityMapper;
    private final ApplicationProperties applicationProperties;

    public ShortUrlService(ShortUrlRepository shortUrlRepository,
                           EntityMapper entityMapper,
                           ApplicationProperties applicationProperties) {
        this.shortUrlRepository = shortUrlRepository;
        this.entityMapper = entityMapper;
        this.applicationProperties = applicationProperties;
    }

    public List<ShortUrlDto> findAllPublicShortUrls() {
        return shortUrlRepository.findPublicShortUrls().stream().map(entityMapper::toShortUrlDto).toList();
    }
    public ShortUrlDto createShortUrl(CreateShortUrlCmd cmd) {
        if(applicationProperties.validateOriginalUrl()) {
            boolean urlExists = UrlExistanceValidator.isUrlExists(cmd.originalUrl());
            if(!urlExists) {
                throw new RuntimeException("Invalid URL " + cmd.originalUrl());
            }
        }
        var shortUrl = new ShortUrl();
        var shortKey = generateUniqueShortKey();
        shortUrl.setShortKey(shortKey);
        shortUrl.setCreatedBy(null);
        shortUrl.setOriginalUrl(cmd.originalUrl());
        shortUrl.setCreatedAt(Instant.now());
        shortUrl.setExpiresAt(Instant.now().plus(applicationProperties.defaultExpiryInDays(), ChronoUnit.DAYS));
        shortUrl.setPrivate(false);
        shortUrlRepository.save(shortUrl);
        return entityMapper.toShortUrlDto(shortUrl);
    }

    public String generateUniqueShortKey() {
        String shortKey;
        do {
            shortKey = generateShortKey();
        }
        while (shortUrlRepository.existsByShortKey(shortKey));
        return shortKey;
    }

    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_KEY_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public String generateShortKey() {
        StringBuilder sb = new StringBuilder(SHORT_KEY_LENGTH);
        for (int i = 0; i < SHORT_KEY_LENGTH; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
