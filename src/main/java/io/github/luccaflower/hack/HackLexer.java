package io.github.luccaflower.hack;

import java.util.Queue;
import java.util.stream.Collectors;

public class HackLexer implements Lexer<HackLexer.LexedInstructions> {


    public HackLexer() {
    }

    public static final String SYMBOL_PATTERN = "[a-zA-Z0-9:$._]+";
    private static final Lexer<HackInstruction> LABEL_INSTRUCTION = Lexer.string("(")
            .skipAnd(Lexer.regex(SYMBOL_PATTERN))
            .andSkip(Lexer.string(")"))
            .andSkip(Lexer.eol().or(Lexer.eof()))
            .map(HackInstruction.LabelInstruction::new);
    private static final Lexer<HackInstruction> LITERAL_A_INSTRUCTION = Lexer.regex("\\d+")
            .or(Lexer.regex("R(1[0-6]|[0-9])").map(s -> s.substring(1)))
            .map(Short::valueOf)
            .map(HackInstruction.LiteralA::new);
    private static final Lexer<HackInstruction> SYMBOLIC_A_INSTRUCTION = Lexer.regex(SYMBOL_PATTERN)
            .map(HackInstruction.SymbolicA::new);
    private static final Lexer<HackInstruction> COMMENT = Lexer.string("//").andThen(Lexer.regex(".*").andSkip(Lexer.eol()))
            .or(Lexer.string("/*").andThen(Lexer.regex(".*\\*/")))
            .map(ignored -> new HackInstruction.NullInstruction());
    private static final Lexer<HackInstruction> A_INSTRUCTION = Lexer.string("@")
            .skipAnd(LITERAL_A_INSTRUCTION.or(SYMBOLIC_A_INSTRUCTION))
            .andSkip(Lexer.eol().or(Lexer.eof()).or(COMMENT.map(Object::toString))); //map comment to string to satisfy type bounds

    public static final Lexer<HackInstruction.AluInstruction> ALU_INSTRUCTION = Lexer.regex("([ADM][+\\-&|][ADM]|0|1|[ADM]{1,3})")
            .map(HackInstruction.AluInstruction::from);
    public static final Lexer<HackInstruction.CJumpCode> JUMP_INSTRUCTION = Lexer.string(";")
            .skipAnd(Lexer.regex("\\w{3}").map(HackInstruction.CJumpCode::valueOf))
            .andSkip(Lexer.eol());
    public static final Lexer<HackInstruction.CDest> C_DEST_INSTRUCTION = Lexer.regex("[ADM]{1,3}")
            .map(HackInstruction.CDest::from)
            .andSkip(Lexer.string("="));
    private static final Lexer<HackInstruction> C_INSTRUCTION = C_DEST_INSTRUCTION
            .or(Lexer.string("").map(s -> HackInstruction.CDest.NO_DEST))
            .andThen(ALU_INSTRUCTION)
            .andThen(JUMP_INSTRUCTION.or(Lexer.eol().or(Lexer.eof()).map(eol -> HackInstruction.CJumpCode.NONE)))
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
                .andSkip(Lexer.eof())
                .map(LexedInstructions::new)
                .tryParse(input);
    }


    public record LexedInstructions(Queue<? extends HackInstruction> instructions) {}

}
