package io.github.luccaflower.hack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Assembler {
    public static void main(String[] args) throws IOException, Parser.ParseException {
        String filename;
        if (args.length == 0) {
            filename = "Prog.asm";
        } else {
            filename = args[0];
        }
        String machineCode;
        try (var input = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            var assembly = input.lines().collect(Collectors.joining("\n"));
            var lexed = new HackLexer().parse(assembly);
            machineCode = new HackParser(lexed).toBinaryString();
        }
        try (var output = new BufferedOutputStream(new FileOutputStream(filename.replace(".asm", ".hack")))) {
            output.write(machineCode.getBytes(StandardCharsets.UTF_8));
        }
    }


}
