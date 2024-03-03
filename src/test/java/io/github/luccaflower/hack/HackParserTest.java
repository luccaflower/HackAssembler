package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class HackParserTest {

    private final HackParser parser = new HackParser();

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
        HackParser.AssemblyContext result = parser.parse("@1\n@2");
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
        HackInstruction.CInstruction expected = new HackInstruction.CInstruction(
                new HackInstruction.AluInstruction(HackInstruction.AluInstructionCode.D_PLUS_A),
                new HackInstruction.CDest(HackInstruction.CDestCode.D),
                new HackInstruction.CJump(HackInstruction.JumpCode.NONE));
        assertThat(parser.parse("D=D+A").instructions().remove())
                .isEqualTo(expected);
    }

}