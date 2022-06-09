package interpreter.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interpreter.expr.Expr;
import interpreter.expr.SetExpr;
import interpreter.util.Utils;
import interpreter.value.ArrayValue;
import interpreter.value.MapValue;
import interpreter.value.NumberValue;
import interpreter.value.TextValue;
import interpreter.value.Value;

public class AssignCommand extends Command {

    public enum Op {
        StdOp, // =
        AddOp, // +=
        SubOp, // -=
        MulOp, // *=
        DivOp, // /=
        ModOp, // %=
        PowerOp; // **=
    }

    private SetExpr lhs;
    private Op op;
    private Expr rhs;

    public AssignCommand(int line, SetExpr lhs, Op op, Expr rhs) {
        super(line);

        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @Override
    public void execute() {
        switch (op) {
            case StdOp:
                stdOp();
                break;
            case AddOp:
                addOp();
                break;
            case SubOp:
                subOp();
                break;
            case MulOp:
                mulOp();
                break;
            case DivOp:
                divOp();
                break;
            case ModOp:
                modOp();
                break;
            case PowerOp:
                powerOp();
                break;
            default:
                Utils.abort(super.getLine());
        }
    }

    private void stdOp() {
        Value<?> rvalue = rhs.expr();
        lhs.setValue(rvalue);
    }

    //Soma de inteiros, concatenacao de arrays e concatenacao de strings
    private void addOp() {
        Value<?> rvalue = rhs.expr();
        Value<?> lvalue = lhs.expr();
        
        Value<?> novoValor = null;

        if(lvalue instanceof NumberValue && rvalue instanceof NumberValue){
            NumberValue nvl = (NumberValue) lvalue;
            NumberValue nvr = (NumberValue) rvalue;
            novoValor = new NumberValue(nvl.value() + nvr.value());
        }
        // else if(lvalue instanceof TextValue && rvalue instanceof TextValue){
        //     TextValue tvl = (TextValue) lvalue;
        //     TextValue rvl = (TextValue) rvalue;
        //     novoValor = new TextValue(tvl.value() + rvl.value()); //concatenacao do java
        // }
        else if(lvalue instanceof TextValue){
            TextValue tvl = (TextValue) lvalue;
            if(rvalue == null){
                novoValor = new TextValue(tvl.value() + "null");
            }
            else{
                novoValor = new TextValue(tvl.value() + rvalue.toString());
            }
            
        }
        else if(lvalue instanceof ArrayValue && rvalue instanceof ArrayValue){
            ArrayValue avl = (ArrayValue) lvalue;
            ArrayValue avr = (ArrayValue) rvalue;
            ArrayList<Value<?>> novoArray = new ArrayList<>();

            for(Value<?> v : avl.value()){
                novoArray.add(v);
            }
            for(Value<?> v : avr.value()){
                novoArray.add(v);
            }

            novoValor = new ArrayValue(novoArray);
        }
        else if(lvalue instanceof MapValue && rvalue instanceof MapValue){
            Map<String, Value<?>> novoMapa = new HashMap<>();
            MapValue mvl = (MapValue) lvalue;
            MapValue mvr = (MapValue) rvalue;

            for(String k : mvl.value().keySet()){
                novoMapa.put(k, mvl.value().get(k));
            }

            for(String k : mvr.value().keySet()){
                if(novoMapa.containsKey(k)){
                    novoMapa.replace(k, mvr.value().get(k));
                }
                else{
                    novoMapa.put(k, mvr.value().get(k));
                }
            }

            novoValor = new MapValue(novoMapa);

        }
        else{
            Utils.abort(super.getLine());
        }

        lhs.setValue(novoValor);
    }

    //Somente subtracao de inteiros
    private void subOp() {
        Value<?> lvalue = lhs.expr();
        Value<?> rvalue = rhs.expr();

        if(!(lvalue instanceof NumberValue && rvalue instanceof NumberValue)){
            Utils.abort(super.getLine());
        }

        NumberValue nvl = (NumberValue) lvalue;
        NumberValue nvr = (NumberValue) rvalue;

        NumberValue novoValor = new NumberValue(nvl.value() - nvr.value());
        lhs.setValue(novoValor);
    }

    //Somente multiplicacoes de inteiros
    private void mulOp() {
        Value<?> lvalue = lhs.expr();
        Value<?> rvalue = rhs.expr();

        if(lvalue instanceof NumberValue && rvalue instanceof NumberValue){
            NumberValue nvl = (NumberValue) lvalue;
            NumberValue nvr = (NumberValue) rvalue;
    
            NumberValue novoValor = new NumberValue(nvl.value() * nvr.value());
            lhs.setValue(novoValor);   
        }
        else if(lvalue instanceof ArrayValue && rvalue instanceof NumberValue){
            ArrayValue avl = (ArrayValue) lvalue;
            NumberValue nvr = (NumberValue) rvalue;
            if(nvr.value() < 0){
                Utils.abort(super.getLine());
            }
            
            List<Value<?>> novoArray = new ArrayList<>();
            for(int i=0; i<nvr.value(); i++){
                for(int j = 0; j<avl.value().size(); j++){
                    novoArray.add(avl.value().get(j));
                }
            }

            ArrayValue novoValor = new ArrayValue(novoArray);
            lhs.setValue(novoValor);
        }
        else{
            Utils.abort(super.getLine());
        }
    }

    //Somente divisao de inteiros. Ignorando divisao por 0, utilizar ArithmeticException padrao do java
    private void divOp() {
        Value<?> lvalue = lhs.expr();
        Value<?> rvalue = rhs.expr();

        if(!(lvalue instanceof NumberValue && rvalue instanceof NumberValue)){
            Utils.abort(super.getLine());
        }

        NumberValue nvl = (NumberValue) lvalue;
        NumberValue nvr = (NumberValue) rvalue;

        NumberValue novoValor = new NumberValue(nvl.value() / nvr.value());
        lhs.setValue(novoValor);
    }

    //Somente modulo da divisao de inteiros. Ignorando modulo da divisao por 0, utilizar ArithmeticException padrao do java
    private void modOp() {
        Value<?> lvalue = lhs.expr();
        Value<?> rvalue = rhs.expr();

        if(!(lvalue instanceof NumberValue && rvalue instanceof NumberValue)){
            Utils.abort(super.getLine());
        }

        NumberValue nvl = (NumberValue) lvalue;
        NumberValue nvr = (NumberValue) rvalue;

        NumberValue novoValor = new NumberValue(nvl.value() % nvr.value());
        lhs.setValue(novoValor);
    }

    //potencia entre inteiros
    private void powerOp() {
        Value<?> lvalue = lhs.expr();
        Value<?> rvalue = rhs.expr();

        if(!(lvalue instanceof NumberValue && rvalue instanceof NumberValue)){
            Utils.abort(super.getLine());
        }

        NumberValue nvl = (NumberValue) lvalue;
        NumberValue nvr = (NumberValue) rvalue;

        NumberValue novoValor = new NumberValue((int) Math.pow(nvl.value(), nvr.value()));
        lhs.setValue(novoValor);
    }

}
