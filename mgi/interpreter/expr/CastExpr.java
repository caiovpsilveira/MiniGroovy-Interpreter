package interpreter.expr;

import interpreter.util.Utils;
import interpreter.value.ArrayValue;
import interpreter.value.BooleanValue;
import interpreter.value.MapValue;
import interpreter.value.NumberValue;
import interpreter.value.TextValue;
import interpreter.value.Value;

public class CastExpr extends Expr {
    
    public enum Op{
        BooleanOp,
        IntegerOp,
        StringOp
    };

    private Expr expr;
    private Op op;

    public CastExpr(int line, Expr expr, Op op){
        super(line);
        this.expr = expr;
        this.op = op;
    }

    @Override
    public Value<?> expr(){
        Value<?> v = null;

        switch(op){
            case BooleanOp:
                v = booleanOp();
                break;
            case IntegerOp:
                v = integerOp();
                break;
            case StringOp:
                v = stringOp();
                break;
            default:
                Utils.abort(super.getLine());
            
        }

        return v;
    }

    private Value<?> booleanOp(){
        Value<?> v = expr.expr();

        boolean retorno = true;

        if(v == null){
            retorno = false;
        }
        else if(v instanceof BooleanValue){
            BooleanValue bv = (BooleanValue) v;
            if(bv.value() == false){
                retorno = false;
            }
        }
        else if(v instanceof NumberValue){
            NumberValue nv = (NumberValue) v;
            if(nv.value() == 0){
                retorno = false;
            }
        }
        else if(v instanceof TextValue){ //nao eh previsto no trabalho, deveria virar 0.
            TextValue tv = (TextValue) v;
            if(tv.value().isEmpty()){
                retorno = false;
            }
        }
        else if(v instanceof ArrayValue){
            ArrayValue av = (ArrayValue) v;
            if(av.value().isEmpty()){
                retorno = false;
            }
        }
        else if(v instanceof MapValue){
            MapValue mv = (MapValue) v;
            if(mv.value().isEmpty()){
                retorno = false;
            }
        }

        return new BooleanValue(retorno);
    }

    private Value<?> integerOp(){
        Value<?> v = expr.expr();

        int valor = 0; //null, arrays e mapas

        if(v instanceof BooleanValue){
            BooleanValue bv = (BooleanValue) v;
            if(bv.value() == true){
                valor = 1;
            }
        }
        else if(v instanceof NumberValue){
            NumberValue nv = (NumberValue) v;
            valor = nv.value();
        }
        else if(v instanceof TextValue){
            TextValue tv = (TextValue) v;
            String str = tv.value();
            try{
                valor = Integer.parseInt(str);
            }
            catch(NumberFormatException e){
                valor = 0;
            }
            
        }

        return new NumberValue(valor);
    }

    private Value<?> stringOp(){
        Value<?> v = expr.expr();
        
        String str = (v == null)? "null" : v.toString();

        return new TextValue(str);
    }

}
