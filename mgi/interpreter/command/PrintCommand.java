package interpreter.command;

import interpreter.expr.Expr;
import interpreter.value.Value;

public class PrintCommand extends Command {

    private boolean newline;
    private Expr expr;

    public PrintCommand(int line, boolean newline, Expr expr) {
        super(line);

        this.newline = newline;
        this.expr = expr;
    }
    
    @Override
    public void execute() {
        Value<?> v = expr.expr();

        String str = v == null ? "null" : v.toString();
        System.out.print(str);
        if (newline)
            System.out.println();
    }
}
