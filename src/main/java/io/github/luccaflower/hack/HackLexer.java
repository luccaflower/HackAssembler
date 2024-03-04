package io.github.luccaflower.hack;

import java.util.Queue;
import java.util.stream.Collectors;

public class HackLexer implements Parser<HackLexer.LexedInstructions> {


    public HackLexer() {
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
    private static final Parser<HackInstruction> COMMENT = Parser.string("//").andThen(Parser.regex(".*").andSkip(Parser.eol()))
            .or(Parser.string("/*").andThen(Parser.regex(".*\\*/")))
            .map(ignored -> new HackInstruction.NullInstruction());
    private static final Parser<HackInstruction> A_INSTRUCTION = Parser.string("@")
            .skipAnd(LITERAL_A_INSTRUCTION.or(SYMBOLIC_A_INSTRUCTION))
            .andSkip(Parser.eol().or(Parser.eof()).or(COMMENT.map(Object::toString))); //map comment to string to satisfy type bounds

    public static final Parser<HackInstruction.AluInstruction> ALU_INSTRUCTION = Parser.regex("([ADM][+\\-&|][ADM]|0|1|[ADM]{1,3})")
            .map(HackInstruction.AluInstruction::from);
    public static final Parser<HackInstruction.CJumpCode> JUMP_INSTRUCTION = Parser.string(";")
            .skipAnd(Parser.regex("\\w{3}").map(HackInstruction.CJumpCode::valueOf))
            .andSkip(Parser.eol());
    public static final Parser<HackInstruction.CDest> C_DEST_INSTRUCTION = Parser.regex("[ADM]{1,3}")
            .map(HackInstruction.CDest::from)
            .andSkip(Parser.string("="));
    private static final Parser<HackInstruction> C_INSTRUCTION = C_DEST_INSTRUCTION
            .or(Parser.string("").map(s -> HackInstruction.CDest.NO_DEST))
            .andThen(ALU_INSTRUCTION)
            .andThen(JUMP_INSTRUCTION.or(Parser.eol().or(Parser.eof()).map(eol -> HackInstruction.CJumpCode.NONE)))
            .map(p -> new HackInstruction.CInstruction(p.left().right(), p.left().left(), p.right()));

    public Parsed<LexedInstructions> tryParse(CharSequence in) throws ParseException {
        //strip whitespaces, remove empty lines
        var input = in.toString()
                .replace(" ", "")
                .lines()
                .filter(l -> !l.isBlank())
                .collect(Collectors.joining("\n"));
        return A_INSTRUCTION.or(LABEL_INSTRUCTION).or(C_INSTRUCTION).or(COMMENT)
                .repeating()
                .andSkip(Parser.eof())
                .map(LexedInstructions::new)
                .tryParse(input);
    }


    public record LexedInstructions(Queue<? extends HackInstruction> instructions) {}

}
