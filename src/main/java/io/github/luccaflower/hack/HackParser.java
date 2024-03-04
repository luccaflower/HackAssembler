package io.github.luccaflower.hack;

import java.util.*;
import java.util.stream.Collectors;

public class HackParser {

    private final HashMap<Short, HackInstruction> instructions = new HashMap<>();
    public HackParser(HackLexer.LexedInstructions lexed) {
        Set<String> symbols = lexed.instructions().stream()
                .filter(i -> i instanceof HackInstruction.SymbolicA)
                .map(i -> ((HackInstruction.SymbolicA) i).name())
                .collect(Collectors.toSet());
        HashMap<String, Short> labels = new HashMap<>();
        Set<Short> literals = new HashSet<>();
        short line = 1;
        for (HackInstruction instruction : lexed.instructions()) {
            switch (instruction) {
                case HackInstruction.NullInstruction ignored: break;
                case HackInstruction.LabelInstruction(String name): {
                    labels.put(name, line);
                    break;
                }
                case HackInstruction.LiteralA literal: {
                    literals.add(literal.address());
                    instructions.put(line++, literal);
                    break;
                }
                case HackInstruction.SymbolicA symbolic: {
                    instructions.put(line++, symbolic);
                    symbols.add(symbolic.name());
                    break;
                }
                case HackInstruction.CInstruction cInstruction: {
                    instructions.put(line++, cInstruction);
                    line++;
                    break;
                }
            }
        }
        var symbolicMapping = new HashMap<String, Short>();
        short counter = 16;
        for (String symbol : symbols) {
            var inserted = false;
            while (!inserted) {
                if(labels.containsKey(symbol)) {
                    symbolicMapping.put(symbol, labels.get(symbol));
                    inserted = true;
                } else if (literals.contains(counter)) {
                    counter++;
                } else {
                    literals.add(counter);
                    symbolicMapping.put(symbol, counter);
                    inserted = true;
                }
            }
            for (Map.Entry<Short, HackInstruction> instruction : instructions.entrySet()) {
                if (Objects.requireNonNull(instruction.getValue()) instanceof HackInstruction.SymbolicA a) {
                    instructions.put(instruction.getKey(), new HackInstruction.LiteralA(symbolicMapping.get(a.name())));
                }
            }
        }
    }

    public String toBinaryString() {
        return instructions.values().stream()
                .map(HackParser::binaryFrom)
                .collect(Collectors.joining("\n"));
    }

    private static String binaryFrom(HackInstruction instruction) {
        return switch (instruction) {
            case HackInstruction.LiteralA a -> binaryFrom(a);
            case HackInstruction.CInstruction c -> binaryFrom(c);
            default -> throw new IllegalStateException("Something went wrong, not all instructions were processed");
        };
    }
    private static String binaryFrom(HackInstruction.LiteralA a) {
        return Integer.toBinaryString(a.address()  | 0x10000).substring(1);
    }

    private static String binaryFrom(HackInstruction.CInstruction c) {
        return Integer.toBinaryString(((0b111 << 13) + c.alu().bytecode() + c.dest().destination() + c.jump().condition()));
    }
}
