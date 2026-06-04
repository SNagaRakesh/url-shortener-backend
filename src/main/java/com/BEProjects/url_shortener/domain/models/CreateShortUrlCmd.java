package com.BEProjects.url_shortener.domain.models;

public record CreateShortUrlCmd(String originalUrl,
                                Boolean isPrivate,
                                Integer expirationInDays,
                                Long userId
                                ) {}
