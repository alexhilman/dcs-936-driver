package com.alexhilman.dlink.dcs936.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 */
public enum DcsFileType {
    File('f'),
    Directory('d');

    private final char typeCharacter;

    private static final Map<Character, DcsFileType> fileTypeByCharacter;

    static {
        fileTypeByCharacter =
                ImmutableMap.copyOf(Stream.of(values())
                                          .collect(toMap(DcsFileType::getTypeCharacter, ft -> ft)));
    }

    public static DcsFileType fromCharacter(final Character character) {
        final DcsFileType dcsFileType = fileTypeByCharacter.get(character);
        if (dcsFileType == null) {
            throw new IllegalArgumentException("Invalid character, expected one of " +
                                                       fileTypeByCharacter.keySet()
                                                                          .stream()
                                                                          .map(String::valueOf)
                                                                          .collect(joining(", ")));
        }

        return dcsFileType;
    }

    DcsFileType(final char typeCharacter) {
        this.typeCharacter = typeCharacter;
    }

    public char getTypeCharacter() {
        return typeCharacter;
    }
}
