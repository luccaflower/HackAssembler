package io.github.luccaflower.hack;

public sealed interface HackInstruction permits
        HackInstruction.LiteralA,
        HackInstruction.SymbolicA,
        HackInstruction.LabelInstruction,
        HackInstruction.CInstruction,
        HackInstruction.NullInstruction {
    record LiteralA(short address) implements HackInstruction {
    }

    record SymbolicA(String name) implements HackInstruction {}

    record LabelInstruction(String label) implements HackInstruction {
    }

    record CInstruction(AluInstruction alu, CDest dest, CJumpCode jump) implements HackInstruction {

    }

    enum AluInstruction {
        ZERO(0b0101010),
        ONE(0b111111),
        MINUS_ONE(0b0111010),
        D(0b0001100),
        A(0b0110000),
        M(0b1110000),
        NOT_D(0b0001101),
        NOT_A(0b0110001),
        NOT_M(0b1110001),
        NEGATIVE_D(0b0001111),
        NEGATIVE_A(0b0110011),
        NEGATIVE_M(0b1110011),
        D_PLUS_ONE(0b0011111),
        A_PLUS_ONE(0b0110111),
        M_PLUS_ONE(0b1110111),
        D_MINUS_ONE(0b0001110),
        A_MINUS_ONE(0b0110010),
        M_MINUS_ONE(0b1110010),
        D_PLUS_A(0b0000010),
        D_PLUS_M(0b1000010),
        D_MINUS_A(0b0010011),
        A_MINUS_D(0b0000111),
        D_MINUS_M(0b1010011),
        M_MINUS_D(0b1000111),
        D_AND_A(0b0000000),
        D_AND_M(0b1000000),
        D_OR_A(0b0010101),
        D_OR_M(0b1010101);

        private final int bytecode;
        AluInstruction(int bytecode) {
            this.bytecode = bytecode;
        }

        public short bytecode() {
            return (short) (bytecode << 6);
        }

        public static AluInstruction from(String instruction) {
            return switch (instruction) {
                case "0" -> ZERO;
                case "1" -> ONE;
                case "-1" -> MINUS_ONE;
                case "D" ->  D;
                case "-D" -> NEGATIVE_D;
                case "!D" -> NOT_D;
                case "D+1" -> D_PLUS_ONE;
                case "D-1" -> D_MINUS_ONE;
                case "A" -> A;
                case "-A" -> NEGATIVE_A;
                case "!A" -> NOT_A;
                case "A+1" -> A_PLUS_ONE;
                case "A-1" -> A_MINUS_ONE;
                case "M" -> M;
                case "-M" -> NEGATIVE_M;
                case "!M" -> NOT_M;
                case "M+1" -> M_PLUS_ONE;
                case "M-1" -> M_MINUS_ONE;
                case "D+A", "A+D" -> D_PLUS_A;
                case "D-A" -> D_MINUS_A;
                case "A-D" -> A_MINUS_D;
                case "D+M", "M+D" -> D_PLUS_M;
                case "D-M" -> D_MINUS_M;
                case "M-D" -> M_MINUS_D;
                case "D|A" -> D_OR_A;
                case "D|M" -> D_OR_M;
                case "D&A" -> D_AND_A;
                case "D&M" -> D_AND_M;

                default -> throw new IllegalStateException("Unexpected value: " + instruction);
            };
        }
    }
    enum CDest {

        NO_DEST(0b000),
        M(0b001),
        D(0b010),
        A(0b100),
        MD(M.destination() + D.destination()),
        AM(A.destination() + M.destination()),
        AD(A.destination() + D.destination()),
        ADM(AD.destination() + M.destination());
        private final int destination;
        CDest(int destination) {
            this.destination = destination;
        }
        public static CDest from(String instruction) {
            return switch (instruction) {
                case "D" -> D;
                case "A" -> A;
                case "M" -> M;
                case "DA", "AD" -> AD;
                case "DM", "MD" -> MD;
                case "AM", "MA" -> AM;
                case "ADM", "AMD", "DAM", "DMA", "MDA", "MAD" -> ADM;
                default -> throw new IllegalStateException("Unexpected value: " + instruction);
            };
        }

        public short destination() {
            return (short) (destination << 3);
        }
    }

    enum CJumpCode {

        NONE(0b000),
        JGT(0b001),
        JEQ(0b010),
        JLT(0b100),
        JGE(JGT.condition() + JEQ.condition()),
        JNE(JGT.condition() + JLT.condition()),
        JLE(JLT.condition() + JEQ.condition()),
        JMP(0b111);
        private final int condition;

        CJumpCode(int condition) {
            this.condition = condition;
        }
        public short condition() {
            return (short) condition;
        }
    }
    record NullInstruction() implements HackInstruction {}

}
