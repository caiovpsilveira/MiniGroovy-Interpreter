package interpreter.command;

import interpreter.expr.Expr;
import interpreter.value.ArrayValue;
import interpreter.value.BooleanValue;
import interpreter.value.MapValue;
import interpreter.value.NumberValue;
import interpreter.value.TextValue;
import interpreter.value.Value;

public class IfCommand extends Command {
    private Expr expr;
    private Command thenCmds;
    private Command elseCmds;

    public IfCommand(int line, Expr expr, Command thenCmds){
        super(line);
        this.expr = expr;
        this.thenCmds = thenCmds;
        this.elseCmds = null;
    }

    public void setElseCommands(Command elseCmds){
        this.elseCmds = elseCmds;
    }

    @Override
    public void execute(){
        Value<?> v = expr.expr();

        if(v instanceof NumberValue){
            NumberValue nv = (NumberValue) v;
            if(nv.value() != 0){
                thenCmds.execute();
            }
            else if(elseCmds != null){
                elseCmds.execute();
            }
        }
        else if(v instanceof ArrayValue){
            ArrayValue av = (ArrayValue) v;
            if(!av.value().isEmpty()){
                thenCmds.execute();
            }
            else if(elseCmds != null){
                elseCmds.execute();
            }
        }
        else if(v instanceof MapValue){
            MapValue mv = (MapValue) v;
            if(!mv.value().isEmpty()){
                thenCmds.execute();
            }
            else if(elseCmds != null){
                elseCmds.execute();
            }
        }
        else if(v instanceof TextValue){
            TextValue tv = (TextValue) v;
            if(!tv.value().isEmpty()){
                thenCmds.execute();
            }
            else if(elseCmds != null){
                elseCmds.execute();
            }
        }
        else if(v instanceof BooleanValue){
            BooleanValue bv = (BooleanValue) v;
            if(bv.value()){
                thenCmds.execute();
            }
            else if(elseCmds != null){
                elseCmds.execute();
            }
        }
        else if(elseCmds != null){ //inclui v == null
            elseCmds.execute();
        }
    }
}
