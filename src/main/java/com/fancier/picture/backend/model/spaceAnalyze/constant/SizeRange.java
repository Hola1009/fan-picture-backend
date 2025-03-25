package com.fancier.picture.backend.model.spaceAnalyze.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Predicate;
@AllArgsConstructor
@Getter
public enum SizeRange {
    LESS_THAN_100KB("<100kb", size -> size < 100 * 1024),
    BETWEEN_100KB_500KB("100kb-500kb", size -> size >= 100 * 1024 && size < 500 * 1024),
    BETWEEN_500KB_1MB("500kb-1mb", size -> size >= 500 * 1024 && size < 1024 * 1024),
    GREATER_THAN_1MB(">1mb", size -> size >= 1024 * 1024);

    final String rangeName;
    final Predicate<Long> matcher;
}
