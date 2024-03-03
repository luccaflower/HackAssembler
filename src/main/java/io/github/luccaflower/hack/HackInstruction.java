package io.github.luccaflower.hack;

public sealed interface HackInstruction permits HackInstruction.AInstruction, HackInstruction.LabelInstruction, HackInstruction.CInstruction, HackInstruction.NullInstruction {
    sealed interface AInstruction extends HackInstruction {
    }

    record LiteralA(short address) implements AInstruction {
    }

    record SymbolicA(String name) implements AInstruction {}

    record LabelInstruction(String label) implements HackInstruction {
    }

    record CInstruction(AluInstruction cOut, CDest dest, CJump jump) implements HackInstruction {

    }
    record AluInstruction(AluInstructionCode alu) {}
    record CDest(CDestCode dest) {}
    record CJump(JumpCode code) {}

    enum AluInstructionCode {
        ZERO(0b0101010),
        ONE(0b111111),
        MINUS_ONE(0b0111010),
        D(0b0001100),
        A(0b0110000),
        M(0b1110000),
        NOT_D(D.bytecode()+1),
        NOT_A(A.bytecode()+1),
        NOT_M(M.bytecode()+1),
        NEGATIVE_D(D.bytecode()+0b11),
        NEGATIVE_A(A.bytecode()+0b11),
        NEGATIVE_M(M.bytecode()+0b11),
        D_PLUS_ONE(0b0011111),
        A_PLUS_ONE(A.bytecode()+0b111),
        M_PLUS_ONE(M.bytecode()+0b111),
        D_MINUS_ONE(D.bytecode()+0b10),
        A_MINUS_ONE(A.bytecode()+0b10),
        M_MINUS_ONE(M.bytecode()+0b10),
        D_PLUS_A(0b0000010),
        D_PLUS_M(D_PLUS_A.bytecode() + 0b1000000),
        D_MINUS_A(0b0010011),
        A_MINUS_D(0b0000111),
        D_MINUS_M(D_MINUS_A.bytecode() + 0b1000000),
        M_MINUS_D(0b1000111),
        D_AND_A(0b0000000),
        D_AND_M(0b1000000),
        D_OR_A(0b0010101),
        D_OR_M(D_OR_A.bytecode()+0b1000000);

        private final int bytecode;
        AluInstructionCode(int bytecode) {
            this.bytecode = bytecode;
        }

        public int bytecode() {
            return bytecode;
        }

        public static AluInstructionCode from(String instruction) {
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
    enum CDestCode {

        NO_DEST(0b000),
        M(0b001),
        D(0b010),
        A(0b100),
        MD(M.destination() + D.destination()),
        AM(A.destination() + M.destination()),
        AD(A.destination() + D.destination()),
        ADM(AD.destination() + M.destination());
        private final int destination;
        CDestCode(int destination) {
            this.destination = destination;
        }
        public static CDestCode from(String instruction) {
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

        public int destination() {
            return destination;
        }
    }

    enum JumpCode {

        NONE(0b000),
        JGT(0b001),
        JEQ(0b010),
        JLT(0b100),
        JGE(JGT.condition() + JEQ.condition()),
        JNE(JGT.condition() + JLT.condition()),
        JLE(JLT.condition() + JEQ.condition()),
        JMP(0b111);
        private final int condition;

        JumpCode(int condition) {
            this.condition = condition;
        }
        public int condition() {
            return condition;
        }
    }
    record NullInstruction() implements HackInstruction {}

}
