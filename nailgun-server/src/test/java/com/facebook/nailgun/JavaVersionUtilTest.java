package com.facebook.nailgun;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaVersionUtilTest {

    private static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of("9", 9),
                Arguments.of("17", 17),
                Arguments.of("20", 20),
                Arguments.of("21-ea", 21),
                Arguments.of("1.6.0", 6),
                Arguments.of("1.8.0_362", 8),
                Arguments.of("17.0.6", 17),
                Arguments.of("1.8.0_372-ea", 8) // not a real version, but just in case
        );
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void featureVersion(String input, int expected) {
        assertEquals(expected, JavaVersionUtil.parseFeatureVersion(input));
    }
}
