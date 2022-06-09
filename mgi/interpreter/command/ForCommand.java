package interpreter.command;

import interpreter.expr.Expr;
import interpreter.value.ArrayValue;
import interpreter.value.BooleanValue;
import interpreter.value.MapValue;
import interpreter.value.NumberValue;
import interpreter.value.TextValue;
import interpreter.value.Value;

public class ForCommand extends Command {
    private Command init;
    private Expr cond;
    private Command inc;
    private Command cmds;

    public ForCommand(int line, Command init, Expr cond, Command inc, Command cmds){
        super(line);
        this.init = init;
        this.cond = cond;
        this.inc = inc;
        this.cmds = cmds;
    }

    @Override
    public void execute(){
        if(init != null){
            init.execute();
        }

        boolean condicao = false;
        while(true){
            if(cond == null){
                condicao = true;
            }
            else{
                Value<?> v = cond.expr();
                if(v instanceof NumberValue){
                    NumberValue nv = (NumberValue) v;
                    condicao = nv.eval();
                }
                else if(v instanceof TextValue){
                    TextValue tv = (TextValue) v;
                    condicao = tv.eval();
                }
                else if(v instanceof ArrayValue){
                    ArrayValue av = (ArrayValue) v;
                    condicao = av.eval();
                }
                else if(v instanceof MapValue){
                    MapValue mv = (MapValue) v;
                    condicao = mv.eval();
                }
                else if(v instanceof BooleanValue){
                    BooleanValue bv = (BooleanValue) v;
                    condicao = bv.eval();
                }
                else{
                    condicao = false;
                }
            }

            if(condicao == false){
                break;
            }
            else{
                if(cmds != null){
                    cmds.execute();
                }
                if(inc != null){
                    inc.execute();
                }
            }
        }
    }
}
