package interpreter.expr;

import java.util.ArrayList;
import java.util.List;

import interpreter.value.ArrayValue;
import interpreter.value.Value;

public class ArrayExpr extends Expr{
    
    private List<Expr> array;

    public ArrayExpr(int line, List<Expr> array){
        super(line);
        this.array = array;
    }

    @Override
    public Value<?> expr(){
        ArrayList<Value<?>> arv = new ArrayList<>();
        for(Expr e : array){
            arv.add(e.expr());
        }

        ArrayValue retorno = new ArrayValue(arv);

        return retorno;
    }
}
