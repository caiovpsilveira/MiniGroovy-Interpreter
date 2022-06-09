package syntatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import interpreter.command.AssignCommand;
import interpreter.command.BlocksCommand;
import interpreter.command.Command;
import interpreter.command.DeclarationType1Command;
import interpreter.command.DeclarationType2Command;
import interpreter.command.ForCommand;
import interpreter.command.ForeachCommand;
import interpreter.command.IfCommand;
import interpreter.command.PrintCommand;
import interpreter.command.WhileCommand;
import interpreter.expr.AccessExpr;
import interpreter.expr.ArrayExpr;
import interpreter.expr.BinaryExpr;
import interpreter.expr.CastExpr;
import interpreter.expr.ConstExpr;
import interpreter.expr.Expr;
import interpreter.expr.MapExpr;
import interpreter.expr.MapExpr.MapItem;
import interpreter.expr.SetExpr;
import interpreter.expr.SwitchExpr;
import interpreter.expr.UnaryExpr;
import interpreter.expr.Variable;
import interpreter.util.Utils;
import interpreter.value.BooleanValue;
import interpreter.value.NumberValue;
import interpreter.value.TextValue;
import interpreter.value.Value;
import lexical.Lexeme;
import lexical.LexicalAnalysis;
import lexical.TokenType;

public class SyntaticAnalysis {

    private LexicalAnalysis lex;
    private Lexeme current;
    private Stack<Lexeme> history;
    private Stack<Lexeme> queued;

    public SyntaticAnalysis(LexicalAnalysis lex) {
        this.lex = lex;
        this.current = lex.nextToken();
        this.history = new Stack<Lexeme>();
        this.queued = new Stack<Lexeme>();
    }

    public Command start() {
        Command cmd = procCode();
        eat(TokenType.END_OF_FILE);
        return cmd;
    }

    private void rollback() {
        assert !history.isEmpty();

        // System.out.println("Rollback (\"" + current.token + "\", " +
        //     current.type + ")");
        queued.push(current);
        current = history.pop();
    }

    private void advance() {
        // System.out.println("Advanced (\"" + current.token + "\", " +
        //     current.type + ")");
        history.add(current);
        current = queued.isEmpty() ? lex.nextToken() : queued.pop();
    }

    private void eat(TokenType type) {
        // System.out.println(lex.getLine() + ":" + "Expected (..., " + type + "), found (\"" + 
        //   current.token + "\", " + current.type + ")");
        if (type == current.type) {
            history.add(current);
            current = queued.isEmpty() ? lex.nextToken() : queued.pop();
        } else {
            showError();
        }
    }

    private void showError() {
        System.out.printf("%02d: ", lex.getLine());

        switch (current.type) {
            case INVALID_TOKEN:
                System.out.printf("Lexema inválido [%s]\n", current.token);
                break;
            case UNEXPECTED_EOF:
            case END_OF_FILE:
                System.out.printf("Fim de arquivo inesperado\n");
                break;
            default:
                System.out.printf("Lexema não esperado [%s]\n", current.token);
                break;
        }

        System.exit(1);
    }

    // <code> ::= { <cmd> }
    private BlocksCommand procCode() {
        int line = lex.getLine();
        List<Command> cmds = new ArrayList<Command>();

        while (current.type == TokenType.DEF ||
            current.type == TokenType.PRINT ||
            current.type == TokenType.PRINTLN ||
            current.type == TokenType.IF ||
            current.type == TokenType.WHILE ||
            current.type == TokenType.FOR ||
            current.type == TokenType.FOREACH ||
            current.type == TokenType.NOT ||
            current.type == TokenType.SUB ||
            current.type == TokenType.OPEN_PAR ||
            current.type == TokenType.NULL ||
            current.type == TokenType.FALSE ||
            current.type == TokenType.TRUE ||
            current.type == TokenType.NUMBER ||
            current.type == TokenType.TEXT ||
            current.type == TokenType.READ ||
            current.type == TokenType.EMPTY ||
            current.type == TokenType.SIZE ||
            current.type == TokenType.KEYS ||
            current.type == TokenType.VALUES ||
            current.type == TokenType.SWITCH ||
            current.type == TokenType.OPEN_BRA ||
            current.type == TokenType.NAME) {
                Command c = procCmd();
                cmds.add(c);
        }

        BlocksCommand bc = new BlocksCommand(line, cmds);
        return bc;
    }

    // <cmd> ::= <decl> | <print> | <if> | <while> | <for> | <foreach> | <assign>
    private Command procCmd() {
        Command cmd = null;
        switch (current.type) {
            case DEF:
                BlocksCommand dc = procDecl();
                cmd = dc;
                break;
            case PRINT:
            case PRINTLN:
                PrintCommand pc = procPrint();
                cmd = pc;
                break;
            case IF:
                cmd = procIf();
                break;
            case WHILE:
                WhileCommand wc = procWhile();
                cmd = wc;
                break;
            case FOR:
                cmd = procFor();
                break;
            case FOREACH:
                cmd = procForeach();
                break;
            case NOT:
            case SUB:
            case OPEN_PAR:
            case NULL:
            case FALSE:
            case TRUE:
            case NUMBER:
            case TEXT:
            case READ:
            case EMPTY:
            case SIZE:
            case KEYS:
            case VALUES:
            case SWITCH:
            case OPEN_BRA:
            case NAME:
                AssignCommand ac = procAssign();
                cmd = ac;
                break;
            default:
                showError();
        }

        return cmd;
    }

    // <decl> ::= def ( <decl-type1> | <decl-type2> )
    private BlocksCommand procDecl() {
        eat(TokenType.DEF);
        BlocksCommand bc = null;
        if (current.type == TokenType.NAME) {
            bc = procDeclType1();
        } else {
            ArrayList<Command> listCmd = new ArrayList<>();
            listCmd.add(procDeclType2());
            int line = lex.getLine();
            bc = new BlocksCommand(line, listCmd);
        }
        return bc;
    }

    // <decl-type1> ::= <name> [ '=' <expr> ] { ',' <name> [ '=' <expr> ] }
    private BlocksCommand procDeclType1() {
        ArrayList<Command> listCmd = new ArrayList<>();
        Variable lhs = procName();
        int line = lex.getLine();

        Expr rhs = null;
        if (current.type == TokenType.ASSIGN) {
            advance();
            rhs = procExpr();
        }

        DeclarationType1Command dt1c = new DeclarationType1Command(line, lhs, rhs);
        listCmd.add(dt1c);

        while (current.type == TokenType.COMMA) {
            advance();

            line = lex.getLine();
            lhs = procName();

            rhs = null;

            if (current.type == TokenType.ASSIGN) {
                advance();
                rhs = procExpr();
            }

            dt1c = new DeclarationType1Command(line, lhs, rhs);
            listCmd.add(dt1c);
        }

        return new BlocksCommand(line, listCmd);
    }

    // <decl-type2> ::= '(' <name> { ',' <name> } ')' = <expr>
    private DeclarationType2Command procDeclType2() {
        
        int line = lex.getLine();
        ArrayList<Variable> lhs = new ArrayList<>();
        
        eat(TokenType.OPEN_PAR);
        lhs.add(procName());
        while(current.type == TokenType.COMMA){ //,
            advance();
            lhs.add(procName());
        }
        eat(TokenType.CLOSE_PAR);
        eat(TokenType.ASSIGN);
        Expr rhs = procExpr();

        return new DeclarationType2Command(line, lhs, rhs);
    }

    // <print> ::= (print | println) '(' <expr> ')'
    private PrintCommand procPrint() {
        boolean newline = false;
        if (current.type == TokenType.PRINT) {
            advance();
        } else if (current.type == TokenType.PRINTLN) {
            newline = true;
            advance();
        } else {
            showError();
        }

        int line = lex.getLine();
        eat(TokenType.OPEN_PAR);
        Expr expr = procExpr();
        eat(TokenType.CLOSE_PAR);

        PrintCommand pc = new PrintCommand(line, newline, expr);
        return pc;
    }

    // <if> ::= if '(' <expr> ')' <body> [ else <body> ]
    private IfCommand procIf() {
        int line = lex.getLine();

        eat(TokenType.IF);
        eat(TokenType.OPEN_PAR);

        Expr expr = procExpr();

        eat(TokenType.CLOSE_PAR);

        Command thenCmds = procBody();
        IfCommand ifcmd = new IfCommand(line, expr, thenCmds);

        if (current.type == TokenType.ELSE) {
            advance();
            Command elseCmds = procBody();
            ifcmd.setElseCommands(elseCmds);
        }
        return ifcmd;
    }

    // <while> ::= while '(' <expr> ')' <body>
    private WhileCommand procWhile() {
        eat(TokenType.WHILE);
        int line = lex.getLine();
        eat(TokenType.OPEN_PAR);
        Expr expr = procExpr();
        eat(TokenType.CLOSE_PAR);
        Command cmds = procBody();

        WhileCommand wc = new WhileCommand(line, expr, cmds);
        return wc;
    }

    // <for> ::= for '(' [ [ def ] <assign> { ',' <assign> } ] ] ';' [ <expr> ] ';' [ <assign> { ',' <assign> } ] ')' <body>
    private ForCommand procFor(){
        eat(TokenType.FOR);
        eat(TokenType.OPEN_PAR);

        int line = lex.getLine();
        BlocksCommand init = null;
        Expr cond = null;
        BlocksCommand inc = null;
        Command cmds = null;

        //primeira parte do for
        if (current.type == TokenType.DEF ||
                current.type == TokenType.NOT ||
                current.type == TokenType.SUB ||
                current.type == TokenType.OPEN_PAR ||
                current.type == TokenType.NULL ||
                current.type == TokenType.FALSE ||
                current.type == TokenType.TRUE ||
                current.type == TokenType.NUMBER ||
                current.type == TokenType.TEXT ||
                current.type == TokenType.READ ||
                current.type == TokenType.EMPTY ||
                current.type == TokenType.SIZE ||
                current.type == TokenType.KEYS ||
                current.type == TokenType.VALUES ||
                current.type == TokenType.SWITCH ||
                current.type == TokenType.OPEN_BRA ||
                current.type == TokenType.NAME) {
                
                    if(current.type == TokenType.DEF){
                        init = procDecl();
                    }
                    else{
                        List<Command> listInit = new ArrayList<>();
                        listInit.add(procAssign());
                        while(current.type == TokenType.COMMA){
                            advance();
                            listInit.add(procAssign());
                        }
                        init = new BlocksCommand(line, listInit);
                    }
        }

        //Segunda parte do for
        eat(TokenType.SEMI_COLON);
        
        if(current.type == TokenType.NOT ||
            current.type == TokenType.SUB ||
            current.type == TokenType.OPEN_PAR ||
            current.type == TokenType.NULL ||
            current.type == TokenType.FALSE ||
            current.type == TokenType.TRUE ||
            current.type == TokenType.NUMBER ||
            current.type == TokenType.TEXT ||
            current.type == TokenType.READ ||
            current.type == TokenType.EMPTY ||
            current.type == TokenType.SIZE ||
            current.type == TokenType.KEYS ||
            current.type == TokenType.VALUES ||
            current.type == TokenType.SWITCH ||
            current.type == TokenType.OPEN_BRA ||
            current.type == TokenType.NAME){
                cond = procExpr();
        }

        eat(TokenType.SEMI_COLON);

        //terceira parte do for
        if(current.type == TokenType.NOT ||
            current.type == TokenType.SUB ||
            current.type == TokenType.OPEN_PAR ||
            current.type == TokenType.NULL ||
            current.type == TokenType.FALSE ||
            current.type == TokenType.TRUE ||
            current.type == TokenType.NUMBER ||
            current.type == TokenType.TEXT ||
            current.type == TokenType.READ ||
            current.type == TokenType.EMPTY ||
            current.type == TokenType.SIZE ||
            current.type == TokenType.KEYS ||
            current.type == TokenType.VALUES ||
            current.type == TokenType.OPEN_BRA ||
            current.type == TokenType.NAME){
            
                List<Command> listInc = new ArrayList<>();
                listInc.add(procAssign());

            while(current.type == TokenType.COMMA){
                advance();
                listInc.add(procAssign());
            }
            inc = new BlocksCommand(line, listInc);
        }

        eat(TokenType.CLOSE_PAR);
        cmds = procBody();

        return new ForCommand(line, init, cond, inc, cmds);
    }

    // <foreach> ::= foreach '(' [ def ] <name> in <expr> ')' <body>
    private ForeachCommand procForeach() {
        int line = lex.getLine();

        eat(TokenType.FOREACH);
        eat(TokenType.OPEN_PAR);

        if(current.type == TokenType.DEF){
            advance();
        }
        Variable var = procName();
        eat(TokenType.CONTAINS);
        Expr expr = procExpr();
        eat(TokenType.CLOSE_PAR);
        Command cmds = procBody();

        return new ForeachCommand(line, var, expr, cmds);
    }

    // <body> ::= <cmd> | '{' <code> '}'
    private Command procBody() {
        Command cmd;
        if (current.type == TokenType.OPEN_CUR) {
            advance();
            cmd = procCode();
            eat(TokenType.CLOSE_CUR);
        } else {
            cmd = procCmd();
        }

        return cmd;
    }

    // <assign> ::= <expr>  ( '=' | '+=' | '-=' | '*=' | '/=' | '%=' | '**=') <expr>
    private AssignCommand procAssign() {
        Expr left = procExpr();
        if (!(left instanceof SetExpr)){
            Utils.abort(lex.getLine());
        }

        AssignCommand.Op op = null;
        switch (current.type) {
            case ASSIGN:
                op = AssignCommand.Op.StdOp;
                break;
            case ASSIGN_ADD:
                op = AssignCommand.Op.AddOp;
                break;
            case ASSIGN_SUB:
                op = AssignCommand.Op.SubOp;
                break;
            case ASSIGN_MUL:
                op = AssignCommand.Op.MulOp;
                break;
            case ASSIGN_DIV:
                op = AssignCommand.Op.DivOp;
                break;
            case ASSIGN_MOD:
                op = AssignCommand.Op.ModOp;
                break;
            case ASSIGN_POWER:
                op = AssignCommand.Op.PowerOp;
                break;
            default:
                showError();
        }
        advance();
        int line = lex.getLine();

        Expr right = procExpr();
        
        AssignCommand ac = new AssignCommand(line, (SetExpr) left, op, right);
        return ac;
    }

    // <expr> ::= <rel> { ('&&' | '||') <rel> }
    private Expr procExpr() {
        Expr LExpr = procRel();
        while (current.type == TokenType.AND ||
        current.type == TokenType.OR) {

            BinaryExpr.Op op;
            int line = lex.getLine();
            if (current.type == TokenType.AND) {
                op = BinaryExpr.Op.AndOp;
            } else {
                op = BinaryExpr.Op.OrOp;
            }
            advance();
            
            Expr RExpr = procRel();
            LExpr = new BinaryExpr(line, LExpr, op, RExpr);
        }

        return LExpr;
    }

    // <rel> ::= <cast> [ ('<' | '>' | '<=' | '>=' | '==' | '!=' | in | '!in') <cast> ]
    private Expr procRel() {
        Expr LExpr = procCast();
        int line = lex.getLine();
        if(current.type == TokenType.LOWER ||
        current.type == TokenType.GREATER ||
        current.type == TokenType.LOWER_EQUAL ||
        current.type == TokenType.GREATER_EQUAL ||
        current.type == TokenType.EQUALS ||
        current.type == TokenType.NOT_EQUALS ||
        current.type == TokenType.CONTAINS ||
        current.type == TokenType.NOT_CONTAINS){
            BinaryExpr.Op op = null;
            switch(current.type){
                case LOWER:
                    op = BinaryExpr.Op.LowerThanOp;
                    break;
                case GREATER:
                    op = BinaryExpr.Op.GreaterThanOp;
                    break;
                case LOWER_EQUAL:
                    op = BinaryExpr.Op.LowerEqualOp;
                    break;
                case GREATER_EQUAL:
                    op = BinaryExpr.Op.GreaterEqualOp;
                    break;
                case EQUALS:
                    op = BinaryExpr.Op.EqualOp;
                    break;
                case NOT_EQUALS:
                    op = BinaryExpr.Op.NotEqualOp;
                    break;
                case CONTAINS:
                    op = BinaryExpr.Op.ContainsOp;
                    break;
                case NOT_CONTAINS:
                    op = BinaryExpr.Op.NotContainsOp;
                    break;
                default:
                    break;
            }

            advance();
            Expr RExpr = procCast();
            LExpr = new BinaryExpr(line, LExpr, op, RExpr);
        }

        return LExpr;
    }

    // <cast> ::= <arith> [ as ( Boolean | Integer | String) ]
    private Expr procCast() {
        Expr expr = procArith();
        int line = lex.getLine();
        if(current.type == TokenType.AS){
            advance();
            CastExpr cExpr = null;
            if(current.type == TokenType.BOOLEAN){
                advance();
                cExpr = new CastExpr(line, expr, CastExpr.Op.BooleanOp);
            }
            else if(current.type == TokenType.INTEGER){
                advance();
                cExpr = new CastExpr(line, expr, CastExpr.Op.IntegerOp);
            }
            else{
                eat(TokenType.STRING); //mostrar mensagem de erro caso nao seja a string, que e o ultimo TT aceitavel
                cExpr = new CastExpr(line, expr, CastExpr.Op.StringOp);
            }
            expr = cExpr;
        }
        
        return expr;
    }

    // <arith> ::= <term> { ('+' | '-') <term> }
    private Expr procArith() {
        Expr left = procTerm();

        while (current.type == TokenType.ADD ||
                current.type == TokenType.SUB) {
            BinaryExpr.Op op = null;
            switch (current.type) {
                case ADD:
                    advance();
                    op = BinaryExpr.Op.AddOp;
                    break;
                case SUB:
                default:
                    advance();
                    op = BinaryExpr.Op.SubOp;
                    break;
            }
            int line = lex.getLine();

            Expr right = procTerm();

            BinaryExpr bexpr = new BinaryExpr(line, left, op, right);
            left = bexpr;
        }

        return left;
    }

    // <term> ::= <power> { ('*' | '/' | '%') <power> }
    private Expr procTerm() {
        Expr LExpr = procPower();
        while(current.type == TokenType.MUL ||
        current.type == TokenType.DIV ||
        current.type == TokenType.MOD){
            
            BinaryExpr.Op op = null;
            int line = lex.getLine();
            
            switch(current.type){
                case MUL:
                    op = BinaryExpr.Op.MulOp;
                    break;
                case DIV:
                    op = BinaryExpr.Op.DivOp;
                    break;
                case MOD:
                    op = BinaryExpr.Op.ModOp;
                    break;
                default:
                    break;
            }
            
            advance();
            Expr RExpr = procPower();
            LExpr = new BinaryExpr(line, LExpr, op, RExpr);
        }

        return LExpr;
    }

    // <power> ::= <factor> { '**' <factor> }
    private Expr procPower() {
        Expr LExpr = procFactor();
        while(current.type == TokenType.POWER){
            int line = lex.getLine();
            advance();
            Expr RExpr = procFactor();
            LExpr = new BinaryExpr(line, LExpr, BinaryExpr.Op.PowerOp, RExpr);
        }
        return LExpr;
    }
    
    // <factor> ::= [ '!' | '-' ] ( '(' <expr> ')' | <rvalue> )
    private Expr procFactor() {
        Expr expr = null;

        UnaryExpr.Op op = null;
        if (current.type == TokenType.NOT) {
            advance();
            op = UnaryExpr.Op.NotOp;
        } else if (current.type == TokenType.SUB) {
            advance();
            op = UnaryExpr.Op.NegOp;
        }
        int line = lex.getLine();

        if (current.type == TokenType.OPEN_PAR) {
            advance();
            expr = procExpr();
            eat(TokenType.CLOSE_PAR);
        } else {
            expr = procRValue();
        }

        if (op != null) {
            UnaryExpr uexpr = new UnaryExpr(line, expr, op);
            expr = uexpr;
        }

        return expr;
    }

    // <lvalue> ::= <name> { '.' <name> | '[' <expr> ']' }
    private SetExpr procLValue() {
        SetExpr sExpr = procName();

        while(current.type == TokenType.DOT ||
        current.type == TokenType.OPEN_BRA){
            int line = lex.getLine();
            if(current.type == TokenType.DOT){
                advance();
                Variable index = procName();
                TextValue value = new TextValue(index.getName());
                ConstExpr cexpr = new ConstExpr(line, value);
                sExpr = new AccessExpr(line, sExpr, cexpr);
            }
            else{
                advance();
                Expr index = procExpr();
                eat(TokenType.CLOSE_BRA);
                sExpr = new AccessExpr(line, sExpr, index);
            }
        }
        return sExpr;
    }

    // <rvalue> ::= <const> | <function> | <switch> | <struct> | <lvalue>
    private Expr procRValue() {
        Expr expr = null;
        switch (current.type) {
            case NULL:
            case FALSE:
            case TRUE:
            case NUMBER:
            case TEXT:
                Value<?> v = procConst();
                int line = lex.getLine();
                ConstExpr cexpr = new ConstExpr(line, v);
                expr = cexpr;
                break;
            case READ:
            case EMPTY:
            case SIZE:
            case KEYS:
            case VALUES:
                UnaryExpr uexpr = procFunction();
                expr = uexpr;
                break;
            case SWITCH:
                expr = procSwitch();
                break;
            case OPEN_BRA:
                expr = procStruct();
                break;
            case NAME:
                expr = procLValue();
                break;
            default:
                showError();
        }

        return expr;
    }

    // <const> ::= null | false | true | <number> | <text>
    private Value<?> procConst() {
        Value<?> v = null;
        if (current.type == TokenType.NULL) {
            advance();
        } else if (current.type == TokenType.FALSE) {
            advance();
            BooleanValue bv = new BooleanValue(false);
            v = bv;
        } else if (current.type == TokenType.TRUE) {
            advance();
            BooleanValue bv = new BooleanValue(true);
            v = bv;
        } else if (current.type == TokenType.NUMBER) {
            NumberValue nv = procNumber();
            v = nv;
        } else if (current.type == TokenType.TEXT) {
            TextValue tv = procText();
            v = tv;
        } else {
            showError();
        }

        return v;
    }

    // <function> ::= (read | empty | size | keys | values) '(' <expr> ')'
    private UnaryExpr procFunction() {
        UnaryExpr.Op op = null;
        switch (current.type) {
            case READ:
                op = UnaryExpr.Op.ReadOp;
                break;
            case EMPTY:
                op = UnaryExpr.Op.EmptyOp;
                break;
            case SIZE:
                op = UnaryExpr.Op.SizeOp;
                break;
            case KEYS:
                op = UnaryExpr.Op.KeysOp;
                break;
            case VALUES:
                op = UnaryExpr.Op.ValuesOp;
                break;
            default:
                showError();
        }
        advance();
        int line = lex.getLine();

        eat(TokenType.OPEN_PAR);
        Expr expr = procExpr();
        eat(TokenType.CLOSE_PAR);

        UnaryExpr uexpr = new UnaryExpr(line, expr, op);
        return uexpr;
    }

    // <switch> ::= switch '(' <expr> ')' '{' { case <expr> '->' <expr> } [ default '->' <expr> ] '}'
    private SwitchExpr procSwitch() {

        int line = lex.getLine();

        eat(TokenType.SWITCH);
        eat(TokenType.OPEN_PAR);

        Expr expr = procExpr();
        SwitchExpr se = new SwitchExpr(line, expr);

        eat(TokenType.CLOSE_PAR);
        eat(TokenType.OPEN_CUR);

        while (current.type == TokenType.CASE) {
            advance();
            Expr key = procExpr();
            eat(TokenType.ARROW);
            Expr value = procExpr();
            se.addItem(se.new CaseItem(key, value));
        }

        if (current.type == TokenType.DEFAULT) {
            advance();
            eat(TokenType.ARROW);
            Expr _default = procExpr();
            se.setDefault(_default);
        }

        eat(TokenType.CLOSE_CUR);
        return se;
    }

    // <struct> ::= '[' [ ':' | <expr> { ',' <expr> } | <name> ':' <expr> { ',' <name> ':' <expr> } ] ']'
    private Expr procStruct() {

        Expr expr = null;

        eat(TokenType.OPEN_BRA);

        int line = lex.getLine();

        if (current.type == TokenType.COLON) { //mapa vazio
            advance();
            expr = new MapExpr(line);
        }
        else if (current.type == TokenType.CLOSE_BRA) { //array vazio
            expr = new ArrayExpr(line, new ArrayList<Expr>());
        }
        else{
            Lexeme prev = current;
            advance();

            if (prev.type == TokenType.NAME &&
                    current.type == TokenType.COLON) { //mapa
                rollback();
                
                MapExpr mExpr = new MapExpr(line);
                String key;
                Expr value;

                key = procName().getName(); //chave
                eat(TokenType.COLON);
                value = procExpr(); //valor

                MapItem mi = mExpr.new MapItem(key, value);
                mExpr.addItem(mi);

                while (current.type == TokenType.COMMA) {
                    advance();

                    key = procName().getName(); //chave
                    eat(TokenType.COLON);
                    value = procExpr(); //valor

                    mi = mExpr.new MapItem(key, value);
                    mExpr.addItem(mi);
                }
                expr = mExpr;
            }
            else { //array
                rollback();
                ArrayList<Expr> arrayExpr = new ArrayList<>();

                Expr exprValor = procExpr(); //valor
                arrayExpr.add(exprValor);

                while (current.type == TokenType.COMMA) {
                    advance();

                    exprValor = procExpr(); //valor
                    arrayExpr.add(exprValor);
                }
                expr = new ArrayExpr(line, arrayExpr);
            }
        }

        eat(TokenType.CLOSE_BRA);
        return expr;
    }

    private Variable procName() {
        String tmp = current.token;
        eat(TokenType.NAME);
        int line = lex.getLine();

        Variable var = new Variable(line, tmp);
        return var;
    }

    private NumberValue procNumber() {
        String tmp = current.token;
        eat(TokenType.NUMBER);

        int v;
        try {
            v = Integer.parseInt(tmp);
        } catch (Exception e) {
            v = 0;
        }

        NumberValue nv = new NumberValue(v);
        return nv;
    }

    private TextValue procText() {
        String tmp = current.token;

        eat(TokenType.TEXT);

        TextValue tv = new TextValue(tmp);
        return tv;
    }
 
}
