package io.github.luccaflower.hack;

import java.util.ArrayDeque;
import java.util.Queue;

public class HackParser implements Parser<HackParser.AssemblyContext> {


    public HackParser() {
    }

    private static final Parser<HackInstruction> LABEL_INSTRUCTION = Parser.string("(")
            .skipAnd(Parser.regex("\\w+"))
            .andSkip(Parser.string(")"))
            .andSkip(Parser.eol().or(Parser.eof()))
            .map(HackInstruction.LabelInstruction::new);
    private static final Parser<HackInstruction> LITERAL_A_INSTRUCTION = Parser.regex("\\d+")
            .map(Short::valueOf)
            .map(HackInstruction.LiteralA::new);
    private static final Parser<HackInstruction> SYMBOLIC_A_INSTRUCTION = Parser.regex("\\w+")
            .map(HackInstruction.SymbolicA::new);
    private static final Parser<HackInstruction> A_INSTRUCTION = Parser.string("@")
            .skipAnd(LITERAL_A_INSTRUCTION.or(SYMBOLIC_A_INSTRUCTION))
            .andSkip(Parser.eol().or(Parser.eof()));
    private static final Parser<HackInstruction> COMMENT = Parser.string("//").andThen(Parser.regex(".*\\n"))
            .or(Parser.string("/*").andThen(Parser.regex(".*\\*/")))
            .map(ignored -> new HackInstruction.NullInstruction());

    public static final Parser<HackInstruction.AluInstruction> ALU_INSTRUCTION = Parser.regex("[ADM][+\\-&|][ADM]")
            .map(HackInstruction.AluInstructionCode::from)
            .map(HackInstruction.AluInstruction::new);
    public static final Parser<HackInstruction.JumpCode> JUMP_INSTRUCTION = Parser.string(";").skipAnd(Parser.regex("\\w{3}").map(HackInstruction.JumpCode::valueOf)).andSkip(Parser.eol());
    private static final Parser<HackInstruction> C_INSTRUCTION = Parser.regex("[ADM]{1,3}")
            .map(s -> new HackInstruction.CDest(HackInstruction.CDestCode.from(s)))
            .andSkip(Parser.string("="))
            .andThen(ALU_INSTRUCTION)
            .andThen(JUMP_INSTRUCTION.or(Parser.eol().or(Parser.eof()).map(eol -> HackInstruction.JumpCode.NONE)).map(HackInstruction.CJump::new))
            .map(p -> new HackInstruction.CInstruction(p.left().right(), p.left().left(), p.right()));

    public Parsed<AssemblyContext> tryParse(CharSequence in) throws ParseException {
        var input = in.toString().strip();
        return A_INSTRUCTION.or(LABEL_INSTRUCTION).or(C_INSTRUCTION).or(COMMENT)
                .repeating()
                .andSkip(Parser.eof())
                .map(AssemblyContext::new)
                .tryParse(input);
    }


    public record AssemblyContext(Queue<? extends HackInstruction> instructions) {}

}
