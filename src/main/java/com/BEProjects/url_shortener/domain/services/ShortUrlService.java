package com.BEProjects.url_shortener.domain.services;

import com.BEProjects.url_shortener.ApplicationProperties;
import com.BEProjects.url_shortener.domain.entities.ShortUrl;
import com.BEProjects.url_shortener.domain.models.CreateShortUrlCmd;
import com.BEProjects.url_shortener.domain.models.ShortUrlDto;
import com.BEProjects.url_shortener.domain.repositories.ShortUrlRepository;
import com.BEProjects.url_shortener.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {
    private final ShortUrlRepository shortUrlRepository;
    private final EntityMapper entityMapper;
    private final ApplicationProperties applicationProperties;
    private final UserRepository userRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository,
                           EntityMapper entityMapper,
                           ApplicationProperties applicationProperties,
                           UserRepository userRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.entityMapper = entityMapper;
        this.applicationProperties = applicationProperties;
        this.userRepository = userRepository;
    }

    public List<ShortUrlDto> findAllPublicShortUrls() {
        return shortUrlRepository.findPublicShortUrls().stream().map(entityMapper::toShortUrlDto).toList();
    }

    @Transactional
    public ShortUrlDto createShortUrl(CreateShortUrlCmd cmd) {
        if(applicationProperties.validateOriginalUrl()) {
            boolean urlExists = UrlExistenceValidator.isUrlExists(cmd.originalUrl());
            if(!urlExists) {
                throw new RuntimeException("Invalid URL " + cmd.originalUrl());
            }
        }
        var shortUrl = new ShortUrl();
        var shortKey = generateUniqueShortKey();
        shortUrl.setShortKey(shortKey);
        if(cmd.userId() == null) {
            shortUrl.setCreatedBy(null);
            shortUrl.setPrivate(false);
            shortUrl.setExpiresAt(Instant.now().plus(applicationProperties.defaultExpiryInDays(), ChronoUnit.DAYS));
        }
        else {
            shortUrl.setCreatedBy(userRepository.findById(cmd.userId()).orElseThrow());
            shortUrl.setPrivate(cmd.isPrivate() != null && cmd.isPrivate());
            shortUrl.setExpiresAt(cmd.expirationInDays() != null ? Instant.now().plus(cmd.expirationInDays(), ChronoUnit.DAYS)
                                                                 : null);
        }
        shortUrl.setOriginalUrl(cmd.originalUrl());
        shortUrl.setCreatedAt(Instant.now());
        shortUrl.setClickCount(0L);
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

    @Transactional
    public Optional<ShortUrlDto> accessShortUrl(String shortKey, Long  userId) {
        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.findByShortKey(shortKey);
        if(shortUrlOptional.isEmpty()) {
            return Optional.empty();
        }
        ShortUrl shortUrl = shortUrlOptional.get();
        if(shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }

        if(shortUrl.getIsPrivate() &&
            shortUrl.getCreatedBy() != null &&
                !Objects.equals(shortUrl.getCreatedBy().getId(), userId)
        ) {
            return Optional.empty();
        }

        shortUrl.setClickCount(shortUrl.getClickCount()+1);
        shortUrlRepository.save(shortUrl);
        return shortUrlOptional.map(entityMapper::toShortUrlDto);
    }
}
