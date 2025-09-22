package Template;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: spartie [file]");
            System.exit(ErrorCode.INCORRECT_USAGE);
        }
        else {
            String filename = args[0];
            File source = new File(filename);
            if (!source.exists()) {
                System.err.printf("File %s not found\n", filename);
                System.exit(ErrorCode.FILE_NOT_FOUND);
            }

            try {
                byte [] sourceCodeBytes = Files.readAllBytes(Paths.get(filename));
                String sourceCode = new String(sourceCodeBytes, Charset.defaultCharset());

                SpartieScanner spartieScanner = new SpartieScanner(sourceCode);
                List<Token> tokens = spartieScanner.scan();
                for(Token token : tokens) {
                    System.out.println(token);
                }
            } catch (IOException e) {
                System.err.printf("Unable to read file %s\n", filename);
            }
        }
    }
}