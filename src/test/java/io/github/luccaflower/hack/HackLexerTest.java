package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class HackLexerTest {

    private final HackLexer lexer = new HackLexer();

    @Test
    void parsesAInstructionLiteral() throws Lexer.ParseException {
        assertThat(lexer.parse("@1").instructions().remove())
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
    void parsesSeveralAInstructionLiterals() throws Lexer.ParseException {
        HackLexer.LexedInstructions result = lexer.parse("@1\n@2");
        assertThat(result.instructions().remove())
                .isEqualTo(new HackInstruction.LiteralA((short) 1));
        assertThat(result.instructions().remove())
                .isEqualTo(new HackInstruction.LiteralA((short) 2));
    }

    @Test
    void parsesLabels() throws Lexer.ParseException {
        assertThat(lexer.parse("(label)").instructions().remove())
                .isEqualTo(new HackInstruction.LabelInstruction("label"));
    }

    @Test
    void parsesSymbolicLabels() throws Lexer.ParseException {
        assertThat(lexer.parse("@symbol").instructions().remove())
                .isEqualTo(new HackInstruction.SymbolicA("symbol"));
    }

    @Test
    void addsAToD() throws Lexer.ParseException {
        var expected = new HackInstruction.CInstruction(
                HackInstruction.AluInstruction.D_PLUS_A,
                HackInstruction.CDest.D,
                HackInstruction.CJumpCode.NONE);
        assertThat(lexer.parse("D=D+A").instructions().remove())
                .isEqualTo(expected);
    }

    @Test
    void assignOneRegisterToAnother() throws Lexer.ParseException {
        var expected = new HackInstruction.CInstruction(
                HackInstruction.AluInstruction.A,
                HackInstruction.CDest.D,
                HackInstruction.CJumpCode.NONE);
        assertThat(lexer.parse("D=A").instructions().remove())
                .isEqualTo(expected);
    }

    @Test
    void commentOnSameLineAsAInstruction() throws Lexer.ParseException {
        var expected = new HackInstruction.SymbolicA("symbolic");
        assertThat(lexer.parse("@symbolic //a symbol").instructions().remove()).isEqualTo(expected);
    }

    @Test
    void jumpInstructionWithoutAssignment() throws Lexer.ParseException {
        var expected = new HackInstruction.CInstruction(
                HackInstruction.AluInstruction.ZERO,
                HackInstruction.CDest.NO_DEST,
                HackInstruction.CJumpCode.JEQ);
        assertThat(lexer.parse("0;JEQ").instructions().remove())
                .isEqualTo(expected);
    }
    @Test
    void firstLineComment() throws Lexer.ParseException {
        var expected = new HackInstruction.NullInstruction();
        assertThat(lexer.parse("// This file is part of www.nand2tetris.org\n").instructions().remove())
                .isEqualTo(expected);
    }

    @Test
    void R14MapsTo14() throws Lexer.ParseException {
        var expected = new HackInstruction.LiteralA((short) 14);
        assertThat(lexer.parse("@R14").instructions().remove())
                .isEqualTo(expected);
    }

    @Test
    void weirdSymbolName() throws Lexer.ParseException {
        String name = "string.setint$while_exp1";
        HackInstruction.SymbolicA expected = new HackInstruction.SymbolicA(name);
        String regex = "[a-zA-Z0-9:$._]+";
        assertThat(name.matches(regex)).isTrue();
        assertThat(lexer.parse("@string.setint$while_exp1").instructions().remove())
                .isEqualTo(expected);
    }

    @Test
    void multipleDestinationArguments() throws Lexer.ParseException {
        var expected = new HackInstruction.CInstruction(HackInstruction.AluInstruction.D_PLUS_A, HackInstruction.CDest.AM, HackInstruction.CJumpCode.NONE);
        assertThat(lexer.parse("AM=D+A").instructions().remove())
                .isEqualTo(expected);
    }

    @Test
    void AMequalsMminusOne() throws Lexer.ParseException {
        var expected = new HackInstruction.CInstruction(HackInstruction.AluInstruction.D_MINUS_ONE, HackInstruction.CDest.AM, HackInstruction.CJumpCode.NONE);
        assertThat(lexer.parse("AM=D-1").instructions().remove()).isEqualTo(expected);
    }

    @Test
    void minusOneAluInstruction() throws Lexer.ParseException {
        var expected = new HackInstruction.CInstruction(HackInstruction.AluInstruction.MINUS_ONE, HackInstruction.CDest.M, HackInstruction.CJumpCode.NONE);
        assertThat(lexer.parse("M=-1").instructions().remove()).isEqualTo(expected);
    }
}