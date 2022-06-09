/*
 * Mini Groovy Interpreter
 * Uso: java mgi [arquivo de texto]
 * 
 * Trabalho desenvolvido para a disciplina de Laboratório de Linguagens de Programacao.
 * 
 * Caio Vinicius Pereira Silveira
 * Vinicius Hiago Goncalves Ribeiro
 * 
 * Junho 2022
 */

import interpreter.command.Command;
import lexical.LexicalAnalysis;
import syntatic.SyntaticAnalysis;

//javac -classpath . $(find ./ -type f -name '*.java')
// rm $(find . -name \*.class -type f)
public class mgi {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java mgi [miniGroovy file]");
            return;
        }

        try (LexicalAnalysis l = new LexicalAnalysis(args[0])) {
            // O código a seguir é dado para testar o interpretador.
            SyntaticAnalysis s = new SyntaticAnalysis(l);
            Command c = s.start();
            c.execute();

            /*
            // O código a seguir é usado apenas para testar o analisador léxico.
            Lexeme lex;
            do {
                lex = l.nextToken();
                System.out.printf("%02d: (\"%s\", %s)\n", l.getLine(),
                    lex.token, lex.type);
            } while (lex.type != TokenType.END_OF_FILE &&
                     lex.type != TokenType.INVALID_TOKEN &&
                     lex.type != TokenType.UNEXPECTED_EOF);
            */
        } catch (Exception e) {
            System.err.println("Internal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}