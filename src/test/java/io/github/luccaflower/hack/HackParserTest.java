package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

class HackParserTest {

    @Test
    void aLowValueAInstruction() {
        assertThat(parserFrom(new HackInstruction.LiteralA((short) 4)).toBinaryString())
                .isEqualTo("0000000000000100");
    }

    @Test
    void aHighValueAInstruction() {
        assertThat(parserFrom(new HackInstruction.LiteralA((short) 0x0FFF)).toBinaryString())
                .isEqualTo("0000111111111111");
    }

    @Test
    void AddingAPlusDToM() {
        HackInstruction.CInstruction cInstruction = new HackInstruction.CInstruction(HackInstruction.AluInstruction.D_PLUS_A, HackInstruction.CDest.M, HackInstruction.CJumpCode.NONE);
        assertThat(parserFrom(cInstruction).toBinaryString())
                .isEqualTo("1110000010001000");

    }

    @Test
    void AequalsMminusOne() {
        var cInstruction = new HackInstruction.CInstruction(HackInstruction.AluInstruction.M_MINUS_ONE, HackInstruction.CDest.A, HackInstruction.CJumpCode.NONE);
        assertThat(parserFrom(cInstruction).toBinaryString())
                .isEqualTo("1111110010100000");
    }

    @Test
    void mapsScreen() {
        var aInstruction = new HackInstruction.SymbolicA("SCREEN");
        assertThat(parserFrom(aInstruction).toBinaryString())
                .isEqualTo("0100000000000000");
    }

    @Test
    void mapsKeyboard() {
        var aInstruction = new HackInstruction.SymbolicA("KBD");
        assertThat(parserFrom(aInstruction).toBinaryString())
                .isEqualTo("0110000000000000");
    }

    @Test
    void mapsLabels() {
        var label = new HackInstruction.LabelInstruction("A");
        var symbol = new HackInstruction.SymbolicA("A");
        assertThat(parserFrom(label, symbol).toBinaryString())
                .isEqualTo("0000000000000000");
    }

    @Test
    void mapsLabelsWhenOutOfOrder() {
        var label = new HackInstruction.LabelInstruction("A");
        var inBetween = nullCInstruction();
        var symbol = new HackInstruction.SymbolicA("A");
        var latter = nullCInstruction();
        assertThat(parserFrom(symbol, inBetween, label, latter).toBinaryString())
                .startsWith("0000000000000010");
    }

    @Test
    void labelsDoNotCountTowardsTheSymbolCounter() {
        var label = new HackInstruction.LabelInstruction("A");
        var symbol = new HackInstruction.SymbolicA("B");
        assertThat(parserFrom(label, symbol).toBinaryString())
                .isEqualTo("0000000000010000");
    }

    @Test
    void AMequalsMminusOne() {
        var instruction = new HackInstruction.CInstruction(HackInstruction.AluInstruction.M_MINUS_ONE, HackInstruction.CDest.AM, HackInstruction.CJumpCode.NONE);
        assertThat(parserFrom(instruction).toBinaryString())
                .isEqualTo("1111110010101000");
    }

    private static HackInstruction.CInstruction nullCInstruction() {
        return new HackInstruction.CInstruction(HackInstruction.AluInstruction.ONE, HackInstruction.CDest.NO_DEST, HackInstruction.CJumpCode.NONE);
    }

    private static HackParser parserFrom(HackInstruction ...instructions) {
        return new HackParser(new HackLexer.LexedInstructions(new ArrayDeque<>(Arrays.asList(instructions))));
    }

}