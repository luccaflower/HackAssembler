package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LexerTest {
    @Test
    void parsesString() throws Lexer.ParseException {
        assertThat(Lexer.string("in").parse("in")).isEqualTo("in");
    }

    @Test
    void parsesRegex() throws Lexer.ParseException {
        assertThat(Lexer.regex("\\d+").parse("234a")).isEqualTo("234");
    }

    @Test
    void mapToNumber() throws Lexer.ParseException {
        assertThat(Lexer.regex("\\d+").map(Integer::parseInt).parse("234")).isEqualTo(234);
    }

    @Test
    void repeating() throws Lexer.ParseException {
        assertThat(Lexer.regex("\\d").repeating().map(l -> String.join("", l)).parse("123"))
                .isEqualTo("123");
    }

    @Test
    void andThen() throws Lexer.ParseException {
        assertThat(Lexer.string("12").andThen(Lexer.string("23")).map(in -> in.reduce((a, b) -> a + b)).parse("1223"))
                .isEqualTo("1223");
    }

    @Test
    void orElse() throws Lexer.ParseException {
        assertThat(Lexer.string("23").or(Lexer.string("12")).parse("12"))
                .isEqualTo("12");
    }
}