package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class HackParserTest {

    @Test
    void aLowValueAInstruction() {
        assertThat(parserFrom(new HackInstruction.LiteralA((short) 4)).toBinaryString())
                .isEqualTo(Integer.toBinaryString(0x0004));
    }

    @Test
    void aHighValueAInstruction() {
        assertThat(parserFrom(new HackInstruction.LiteralA((short) 0x0FFF)).toBinaryString())
                .isEqualTo(Integer.toBinaryString(0x0FFF));
    }

    @Test
    void AssingAPlusDToM() {
        HackInstruction.CInstruction cInstruction = new HackInstruction.CInstruction(HackInstruction.AluInstruction.D_PLUS_A, HackInstruction.CDest.M, HackInstruction.CJumpCode.NONE);
        assertThat(parserFrom(cInstruction).toBinaryString())
                .isEqualTo("1110000010001000");

    }

    private static HackParser parserFrom(HackInstruction ...instructions) {
        return new HackParser(new HackLexer.LexedInstructions(new ArrayDeque<>(Arrays.asList(instructions))));
    }

}