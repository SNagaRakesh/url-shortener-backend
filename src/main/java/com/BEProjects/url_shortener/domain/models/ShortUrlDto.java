package com.BEProjects.url_shortener.domain.models;

import java.io.Serializable;
import java.time.Instant;

public record ShortUrlDto(Long id,
                          String shortKey,
                          String originalUrl,
                          boolean isPrivate,
                          Instant expiresAt,
                          UserDto createdBy,
                          Long clickCount,
                          Instant createdAt) implements Serializable {
}
