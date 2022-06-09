package lexical;

import java.io.FileInputStream;
import java.io.PushbackInputStream;

public class LexicalAnalysis implements AutoCloseable {

    private int line;
    private SymbolTable st;
    private PushbackInputStream input;

    public LexicalAnalysis(String filename) {
        try {
            input = new PushbackInputStream(new FileInputStream(filename), 2);
        } catch (Exception e) {
            throw new LexicalException("Unable to open file");
        }

        st = new SymbolTable();
        line = 1;
    }

    public void close() {
        try {
            input.close();
        } catch (Exception e) {
            throw new LexicalException("Unable to close file");
        }
    }

    public int getLine() {
        return this.line;
    }

    public Lexeme nextToken() {
        Lexeme lex = new Lexeme("", TokenType.END_OF_FILE);

        int state = 1;
        while (state != 14 && state != 15) {
            int c = getc();
            // System.out.printf("  [%02d, %03d ('%c')]\n",
            //     state, c, (char) c);

            switch (state) {
                case 1:
                    if (c == ' ' || c == '\r' || c == '\t') {
                        state = 1;
                    }
                    else if (c == '\n') {
                        this.line++;
                        state = 1;
                    }
                    else if(c == '/'){
                        state = 2;
                    }
                    else if(c == '*'){
                        lex.token += (char) c;
                        state = 4;
                    }
                    else if(c == '+' || c == '=' || c == '<' || c == '>' || c == '%'){
                        lex.token += (char) c;
                        state = 5;
                    }
                    else if(c == '-'){
                        lex.token += (char) c;
                        state = 6;
                    }
                    else if (c == '.' || c == ',' || c == ';' || c == ':' ||
                                c == '(' || c == ')' || c == '[' || c == ']' ||
                                c == '{' || c == '}') {
                        lex.token += (char) c;
                        state = 14;
                    }
                    else if (c == '!') {
                        lex.token += (char) c;
                        state = 7;
                    }
                    else if (c == '&') {
                        lex.token += (char) c;
                        state = 9;
                    }
                    else if(c == '|'){
                        lex.token += (char) c;
                        state = 10;
                    }
                    else if (c == '_' || c == '$' || Character.isLetter(c)) {
                        lex.token += (char) c;
                        state = 11;
                    }
                    else if (Character.isDigit(c)) {
                        lex.token += (char) c;
                        state = 12;
                    }
                    else if(c == '\''){// ' de inicio de string
                        //nao se adiciona a ' na string. Se define como TT.TEXT posteriormente
                        state = 13;
                    }
                    else if (c == -1) {
                        lex.type = TokenType.END_OF_FILE;
                        state = 15;
                    }
                    else {
                        lex.token += (char) c;
                        lex.type = TokenType.INVALID_TOKEN;
                        state = 15;
                    }
                    break;
                case 2:
                    if(c == '/'){
                        //nao se adiciona += (char) '/' para nao adicionar no token caso seja um comentario. Adicionar a barra depois
                        state = 3;
                    }
                    else if(c == '='){
                        lex.token += "/=";
                        state = 14;
                    }
                    else{
                        lex.token += '/'; //adiciona a barra da divisao e devolve o proximo caractere
                        ungetc(c);
                        state = 14;
                    }
                    break;
                case 3:
                    if(c == '\n'){
                        state = 1;
                        this.line++;
                    }
                    else{ //nao necessario, mas deixar para enfatizar que se mantem no 3
                        state = 3;
                    }
                    break;
                case 4:
                    if(c == '='){
                        lex.token += (char) c;
                        state = 14;
                    }
                    else if(c == '*'){
                        lex.token += (char) c;
                        state = 5;
                    }
                    else{
                        ungetc(c);
                        state = 14;
                    }
                    break;
                case 5:
                    if(c == '='){
                        lex.token += (char) c;
                        state = 14;
                    }
                    else{
                        ungetc(c);
                        state = 14;
                    }
                    break;
                case 6:
                    if(c == '=' || c == '>'){
                        lex.token += (char) c;
                        state = 14;
                    }
                    else{
                        ungetc(c);
                        state = 14;
                    }
                    break;
                case 7:
                    if (c == '='){
                        lex.token += (char) c;
                        state = 14;
                    }
                    else if(c == 'i'){
                        state = 8; //nao adiciona o i no caso de ser um in ou uma variavel i. Deve ser adicionado depois, se for o caso, no 8
                    }
                    else{
                        ungetc(c);
                        state = 14;
                    }
                    break;
                case 8:
                    if(c == 'n'){
                        lex.token += "in";
                        state = 14;
                    }
                    else{
                        ungetc(c);
                        ungetc('i');
                        state = 14;
                    }
                    break;
                case 9:
                    if(c == '&'){
                        lex.token += (char) c;
                        state = 14;
                    }
                    else{
                        lex.type = TokenType.INVALID_TOKEN;
                        state = 15;
                    }
                    break;
                case 10:
                    if(c == '|'){
                        lex.token += (char) c;
                        state = 14;
                    }
                    else{
                        lex.type = TokenType.INVALID_TOKEN;
                        state = 15;
                    }
                    break;
                case 11:
                    if(c == '_' || c == '$' || Character.isLetter(c) ||
                            Character.isDigit(c)){
                        lex.token += (char) c;
                        state = 11;
                    }
                    else{
                        ungetc(c);
                        state = 14;
                    }
                    break;
                case 12:
                    if(Character.isDigit(c)){
                        lex.token += (char) c;
                        state = 12;
                    }
                    else{
                        ungetc(c);
                        lex.type = TokenType.NUMBER;
                        state = 15;
                    }
                    break;
                case 13:
                    if(c == '\''){ //fim da string
                        lex.type = TokenType.TEXT;
                        state = 15;
                    }
                    else if(c == -1){//EOF inesperado, sem fechar a string
                        lex.type = TokenType.UNEXPECTED_EOF;
                        state = 15;
                    }
                    else if(c == '\n'){ //se for uma quebra de linha dentro da string
                        this.line++;
                        lex.token += (char) c;
                        state = 13; //nao eh necessario mas enfatizar que se mantem no 13
                    }
                    else{
                        lex.token += (char) c;
                        state = 13; //nao eh necessario mas enfatizar que se mantem no 13
                    }
                    break;
                default:
                    throw new LexicalException("Unreachable");
            }
        }

        if (state == 14)
            lex.type = st.find(lex.token);

        return lex;
    }

    private int getc() {
        try {
            return input.read();
        } catch (Exception e) {
            throw new LexicalException("Unable to read file");
        }
    }

    private void ungetc(int c) {
        if (c != -1) {
            try {
                input.unread(c);
            } catch (Exception e) {
                throw new LexicalException("Unable to ungetc");
            }
        }
    }
}
