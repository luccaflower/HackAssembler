package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {
    @Test
    void parsesString() throws Parser.ParseException {
        assertThat(Parser.string("in").parse("in")).isEqualTo("in");
    }

    @Test
    void parsesRegex() throws Parser.ParseException {
        assertThat(Parser.regex("\\d+").parse("234a")).isEqualTo("234");
    }

    @Test
    void mapToNumber() throws Parser.ParseException {
        assertThat(Parser.regex("\\d+").map(Integer::parseInt).parse("234")).isEqualTo(234);
    }

    @Test
    void repeating() throws Parser.ParseException {
        assertThat(Parser.regex("\\d").repeating().map(l -> String.join("", l)).parse("123"))
                .isEqualTo("123");
    }

    @Test
    void andThen() throws Parser.ParseException {
        assertThat(Parser.string("12").andThen(Parser.string("23")).map(in -> in.reduce((a, b) -> a + b)).parse("1223"))
                .isEqualTo("1223");
    }

    @Test
    void orElse() throws Parser.ParseException {
        assertThat(Parser.string("23").or(Parser.string("12")).parse("12"))
                .isEqualTo("12");
    }
}