package io.github.luccaflower.hack;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AssemblerTest {

    @ParameterizedTest
    @CsvSource({"Add.asm,Add.hack", "Max.asm,Max.hack", "MaxL.asm,MaxL.hack", "Rect.asm,Rect.hack", "RectL.asm,RectL.hack", "PongL.asm,PongL.hack"})
    void assembles(String inputFile, String expectedFile) throws IOException, Lexer.ParseException {
        String actual;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try (var inputStream = contextClassLoader.getResourceAsStream(inputFile)) {
            actual = Assembler.assemble(new BufferedReader(new InputStreamReader(inputStream)));
        }
        String expected;
        try (var inputStream = contextClassLoader.getResourceAsStream(expectedFile)) {
            expected = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
        }
        assertThat(actual).isEqualTo(expected);
    }
}
