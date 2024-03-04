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
        if (!filename.endsWith(".asm")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        String machineCode;
        try (var input = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            var assembly = input.lines().collect(Collectors.joining("\n"));
            var lexed = new HackLexer().parse(assembly);
            machineCode = new HackParser(lexed).toBinaryString();
        }

        String outputFilename = filename.replace(".asm", ".hack");
        File outputFile = new File(outputFilename);
        if (outputFile.exists() && !(outputFile.delete())) {
            throw new IllegalStateException("Failed to overwrite existing hack file");
        }
        try (var output = new FileWriter(outputFilename, false)) {
            output.write(machineCode);
        }
    }


}
