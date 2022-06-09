package interpreter.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import interpreter.util.Utils;
import interpreter.value.ArrayValue;
import interpreter.value.BooleanValue;
import interpreter.value.MapValue;
import interpreter.value.NumberValue;
import interpreter.value.TextValue;
import interpreter.value.Value;

public class UnaryExpr extends Expr {

    public enum Op {
        NotOp,
        NegOp,
        ReadOp,
        EmptyOp,
        SizeOp,
        KeysOp,
        ValuesOp;
    }
    private static Scanner input = new Scanner(System.in);

    private Expr expr;
    private Op op;

    public UnaryExpr(int line, Expr expr, Op op) {
        super(line);
        
        this.expr = expr;
        this.op = op;
    }

    @Override
    public Value<?> expr() {
        Value<?> v = null;
        switch (op) {
            case NotOp:
                v = notOp();
                break;
            case NegOp:
                v = negOp();
                break;
            case ReadOp:
                v = readOp();
                break;
            case EmptyOp:
                v = emptyOp();
                break;
            case SizeOp:
                v = sizeOp();
                break;
            case KeysOp:
                v = keysOp();
                break;
            case ValuesOp:
                v = valuesOp();
                break;
            default:
                Utils.abort(super.getLine());
        }

        return v;
    }

    private Value<?> notOp() {
        Value<?> v = expr.expr();
        boolean b = v == null ? false : v.eval();
        BooleanValue bv = new BooleanValue(!b);
        return bv;
    }

    private Value<?> negOp() {
        Value<?> v = expr.expr();
        if (!(v instanceof NumberValue))
            Utils.abort(super.getLine());

        NumberValue nv = (NumberValue) v;
        int n = nv.value();

        NumberValue res = new NumberValue(-n);
        return res;
    }

    private Value<?> readOp() {
        Value<?> v = expr.expr();
        System.out.print(v == null ? "null" : v.toString()); //read('Digite seu nome') ou read('')

        String line = input.nextLine();
        TextValue tv = new TextValue(line);
        return tv;
    }

    private Value<?> emptyOp() {
        Value<?> v = expr.expr();
        BooleanValue retorno = null;

        if(v instanceof ArrayValue){
            ArrayValue av = (ArrayValue) v;
            List<Value<?>> values = av.value();
            if(values.size() == 0){
                retorno = new BooleanValue(true);
            }
            else{
                retorno = new BooleanValue(false);
            }
        }
        else if(v instanceof MapValue){
            MapValue mv = (MapValue) v;
            Map<String, Value<?>> itens = mv.value();
            if(itens.size() == 0){
                retorno = new BooleanValue(true);
            }
            else{
                retorno = new BooleanValue(false);
            }
        }
        else{
            Utils.abort(super.getLine());
        }
        return retorno;
    }

    private Value<?> sizeOp() {
        Value<?> v = expr.expr();
        NumberValue retorno = null;
        if(v instanceof ArrayValue){
            ArrayValue av = (ArrayValue) v;
            retorno = new NumberValue(av.value().size());
        }
        else if(v instanceof MapValue){
            MapValue mv = (MapValue) v;
            retorno = new NumberValue(mv.value().size());
        }
        else if(v instanceof TextValue){
            TextValue tv = (TextValue) v;
            retorno = new NumberValue(tv.value().length());
        }
        else{
            Utils.abort(super.getLine());
        }
        return retorno;
    }

    private Value<?> keysOp() {
        Value<?> v = expr.expr();

        if(!(v instanceof MapValue)){
            Utils.abort(super.getLine());
        }

        MapValue mv = (MapValue) v;
        Set<String> keySet = mv.value().keySet();
        ArrayList<Value<?>> keysValues = new ArrayList<>();

        for(String k : keySet){
            keysValues.add(new TextValue(k));
        }

        ArrayValue av = new ArrayValue(keysValues);
        return av;
    }

    private Value<?> valuesOp() {
        Value<?> v = expr.expr();

        if(!(v instanceof MapValue)){
            Utils.abort(super.getLine());
        }

        MapValue mv = (MapValue) v;
        Collection<Value<?>> colValues = mv.value().values();
        ArrayList<Value<?>> values = new ArrayList<>();
        
        for(Value<?> val : colValues){
            values.add(val);
        }

        ArrayValue av = new ArrayValue(values);
        return av;
    }

}
