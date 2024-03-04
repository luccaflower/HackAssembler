package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class HackLexerTest {

    private final HackLexer parser = new HackLexer();

    @Test
    void parsesAInstructionLiteral() throws Parser.ParseException {
        assertThat(parser.parse("@1").instructions().remove())
                .isEqualTo(new HackInstruction.LiteralA((short) 1));
    }

    @Test
    void regex() {
        var pattern = Pattern.compile("\\d+");
        assertThat(pattern.matcher("1").matches()).isTrue();
        assertThat(pattern.matcher("11").matches()).isTrue();
        assertThat(pattern.matcher("a1").lookingAt()).isFalse();
    }
    @Test
    void parsesSeveralAInstructionLiterals() throws Parser.ParseException {
        HackLexer.LexedInstructions result = parser.parse("@1\n@2");
        assertThat(result.instructions().remove())
                .isEqualTo(new HackInstruction.LiteralA((short) 1));
        assertThat(result.instructions().remove())
                .isEqualTo(new HackInstruction.LiteralA((short) 2));
    }

    @Test
    void parsesLabels() throws Parser.ParseException {
        assertThat(parser.parse("(label)").instructions().remove())
                .isEqualTo(new HackInstruction.LabelInstruction("label"));
    }

    @Test
    void parsesSymbolicLabels() throws Parser.ParseException {
        assertThat(parser.parse("@symbol").instructions().remove())
                .isEqualTo(new HackInstruction.SymbolicA("symbol"));
    }

    @Test
    void addsAToD() throws Parser.ParseException {
        var expected = new HackInstruction.CInstruction(
                HackInstruction.AluInstruction.D_PLUS_A,
                HackInstruction.CDest.D,
                HackInstruction.CJumpCode.NONE);
        assertThat(parser.parse("D=D+A").instructions().remove())
                .isEqualTo(expected);
    }

    @Test
    void assignOneRegisterToAnother() throws Parser.ParseException {
        var expected = new HackInstruction.CInstruction(
                HackInstruction.AluInstruction.A,
                HackInstruction.CDest.D,
                HackInstruction.CJumpCode.NONE);
        assertThat(parser.parse("D=A").instructions().remove())
                .isEqualTo(expected);
    }

    @Test
    void commentOnSameLineAsAInstruction() throws Parser.ParseException {
        var expected = new HackInstruction.SymbolicA("symbolic");
        assertThat(parser.parse("@symbolic //a symbol").instructions().remove()).isEqualTo(expected);
    }

    @Test
    void jumpInstructionWithoutAssignment() throws Parser.ParseException {
        var expected = new HackInstruction.CInstruction(
                HackInstruction.AluInstruction.ZERO,
                HackInstruction.CDest.NO_DEST,
                HackInstruction.CJumpCode.JEQ);
        assertThat(parser.parse("0;JEQ").instructions().remove())
                .isEqualTo(expected);
    }
    @Test
    void firstLineComment() throws Parser.ParseException {
        var expected = new HackInstruction.NullInstruction();
        assertThat(parser.parse("// This file is part of www.nand2tetris.org\n").instructions().remove())
                .isEqualTo(expected);
    }
}