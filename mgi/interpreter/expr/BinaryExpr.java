package interpreter.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import interpreter.util.Utils;
import interpreter.value.ArrayValue;
import interpreter.value.BooleanValue;
import interpreter.value.MapValue;
import interpreter.value.NumberValue;
import interpreter.value.TextValue;
import interpreter.value.Value;

public class BinaryExpr extends Expr {

    public enum Op {
        AndOp,
        OrOp,
        EqualOp,
        NotEqualOp,
        LowerThanOp,
        LowerEqualOp,
        GreaterThanOp,
        GreaterEqualOp,
        ContainsOp,
        NotContainsOp,
        AddOp,
        SubOp,
        MulOp,
        DivOp,
        ModOp,
        PowerOp;
    }

    private Expr left;
    private Op op;
    private Expr right;

    public BinaryExpr(int line, Expr left, Op op, Expr right) {
        super(line);

        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public Value<?> expr() {
        Value<?> v = null;
        switch (op) {
            case AndOp:
                v = andOp();
                break;
            case OrOp:
                v = orOp();
                break;
            case EqualOp:
                v = equalOp();
                break;
            case NotEqualOp:
                v = notEqualOp();
                break;
            case LowerThanOp:
                v = lowerThanOp();
                break;
            case LowerEqualOp:
                v = lowerEqualOp();
                break;
            case GreaterThanOp:
                v = greaterThanOp();
                break;
            case GreaterEqualOp:
                v = greaterEqualOp();
                break;
            case ContainsOp:
                v = containsOp();
                break;
            case NotContainsOp:
                v = notContainsOp();
                break;
            case AddOp:
                v = addOp();
                break;
            case SubOp:
                v = subOp();
                break;
            case MulOp:
                v = mulOp();
                break;
            case DivOp:
                v = divOp();
                break;
            case ModOp:
                v = modOp();
                break;
            case PowerOp:
                v = powerOp();
                break;
            default:
                Utils.abort(super.getLine());
        }

        return v;
    }

    private Value<?> andOp() {
        BooleanValue bv = null;
        if(left.expr() instanceof BooleanValue && right.expr() instanceof BooleanValue){
            BooleanValue l = (BooleanValue)left.expr();
            BooleanValue r = (BooleanValue)right.expr();
            if(l.eval() && r.eval()){ //curto circuito padrao do java
                bv = new BooleanValue(true);
            }
            else{
                bv = new BooleanValue(false);
            }
        }
        else{
            Utils.abort(super.getLine());
        }
        return bv;
    }

    private Value<?> orOp() {
        BooleanValue bv = null;
        if(left.expr() instanceof BooleanValue && right.expr() instanceof BooleanValue){
            BooleanValue l = (BooleanValue)left.expr();
            BooleanValue r = (BooleanValue)right.expr();
            if(l.eval() || r.eval()){ //curto circuito padrao do java
                bv = new BooleanValue(true);
            }
            else{
                bv = new BooleanValue(false);
            }
        }
        else{
            Utils.abort(super.getLine());
        }
        return bv;
    }

    private Value<?> equalOp() {
        Value<?> lValue = left.expr();
        Value<?> rValue = right.expr();

        boolean boolRet = false; //se lvalue == null e rvalue != null: null == naoNull? falso

        if(lValue != null){
            boolRet = lValue.equals(rValue);
        }
        else if(rValue == null){ //null == null? verdadeiro
            boolRet = true;
        }

        BooleanValue bv = new BooleanValue(boolRet);
        return bv;
    }

    private Value<?> notEqualOp() {
        Value<?> lValue = left.expr();
        Value<?> rValue = right.expr();

        boolean boolRet = true; //se lvalue == null e rvalue != null: null != naoNull eh verdadeiro

        if(lValue != null){
            boolRet = !lValue.equals(rValue);
        }
        else if(rValue == null){ //null != null? falso
            boolRet = false;
        }
        BooleanValue bv = new BooleanValue(boolRet);
        return bv;
    }

    private Value<?> lowerThanOp() { //l < r. groovy compara boolean, inteiro e string.
        Value<?> l = left.expr();
        Value<?> r = right.expr();

        BooleanValue retorno = null;

        if(l instanceof BooleanValue && r instanceof BooleanValue){
            BooleanValue bvl = (BooleanValue)l;
            BooleanValue bvr = (BooleanValue)r;
            if(bvl.value() == false && bvr.value() == true){
                retorno = new BooleanValue(true);
            }
            else{
                retorno = new BooleanValue(false);
            }
        }
        else if(l instanceof NumberValue && r instanceof NumberValue){
            NumberValue nvl = (NumberValue) l;
            NumberValue nvr = (NumberValue) r;
            if(nvl.value() < nvr.value()){
                retorno = new BooleanValue(true);
            }
            else{
                retorno = new BooleanValue(false);
            }
        }
        else if(l instanceof TextValue && r instanceof TextValue){
            TextValue tvl = (TextValue) l;
            TextValue tvr = (TextValue) r;
            String strL = tvl.value();
            String strR = tvr.value();

            boolean isLower = false;
            int tamanhoMaiorString = (strL.length() > strR.length())? strL.length() : strR.length();
            //tamanho da maior string garantira que ocorrera a excecao quando nao encontrar um caractere diferente ate uma das strings acabar
            try{
                for(int i=0; i<tamanhoMaiorString; i++){
                    char cL = strL.charAt(i);
                    char cR = strR.charAt(i);
                    if(cL < cR){
                        isLower = true;
                        break;
                    }
                }
            }
            catch(StringIndexOutOfBoundsException e){ //alguma string acabou, e ainda nao encontrou caracteres diferentes
                if(strL.length() < strR.length()){
                    isLower = true;
                }
            }
            finally{
                retorno = new BooleanValue(isLower);
            }
        }
        else{
            Utils.abort(super.getLine());
        }

        return retorno;
    }

    private Value<?> lowerEqualOp() {
        BooleanValue greater = (BooleanValue) greaterThanOp();
        BooleanValue bv = new BooleanValue(!greater.value());

        return bv;
    }

    private Value<?> greaterThanOp() {
        Value<?> l = left.expr();
        Value<?> r = right.expr();

        BooleanValue retorno = null;

        if(l instanceof BooleanValue && r instanceof BooleanValue){
            BooleanValue bvl = (BooleanValue)l;
            BooleanValue bvr = (BooleanValue)r;
            if(bvl.value() == true && bvr.value() == false){
                retorno = new BooleanValue(true);
            }
            else{
                retorno = new BooleanValue(false);
            }
        }
        else if(l instanceof NumberValue && r instanceof NumberValue){
            NumberValue nvl = (NumberValue) l;
            NumberValue nvr = (NumberValue) r;
            if(nvl.value() > nvr.value()){
                retorno = new BooleanValue(true);
            }
            else{
                retorno = new BooleanValue(false);
            }
        }
        else if(l instanceof TextValue && r instanceof TextValue){
            TextValue tvl = (TextValue) l;
            TextValue tvr = (TextValue) r;
            String strL = tvl.value();
            String strR = tvr.value();

            boolean isGreater = false;
            int tamanhoMaiorString = (strL.length() > strR.length())? strL.length() : strR.length();
            //tamanho da maior string garantira que ocorrera a excecao quando nao encontrar um caractere diferente ate uma das strings acabar
            try{
                for(int i=0; i<tamanhoMaiorString; i++){
                    char cL = strL.charAt(i);
                    char cR = strR.charAt(i);
                    if(cL > cR){
                        isGreater = true;
                        break;
                    }
                }
            }
            catch(StringIndexOutOfBoundsException e){ //alguma string acabou, e ainda nao encontrou caracteres diferentes
                if(strL.length() > strR.length()){
                    isGreater = true;
                }
            }
            finally{
                retorno = new BooleanValue(isGreater);
            }
        }
        else{
            Utils.abort(super.getLine());
        }

        return retorno;
    }

    private Value<?> greaterEqualOp() {
        BooleanValue lower = (BooleanValue) lowerThanOp();
        BooleanValue bv = new BooleanValue(!lower.value());

        return bv;
    }

    private Value<?> containsOp() {
        Value<?> lvalue = left.expr();
        Value<?> rvalue = right.expr();

        boolean boolRetorno = false;

        if(lvalue == null){
            //do nothing: manter retorno em falso. nao eh necessario verificar o rvalue eh null, porque os outros verificam
        }
        else if(rvalue instanceof ArrayValue){ //provavelmente nao funcionaria com arrays e mapas contendo arrays ou mapas como elementos
            ArrayValue avr = (ArrayValue) rvalue;
            for(Value<?> v : avr.value()){
                if(v.value().equals(lvalue.value())){
                    boolRetorno = true;
                    break;
                }
            }

        }
        else if(lvalue instanceof TextValue && rvalue instanceof MapValue){ //verifica se contem chave
            TextValue tvl = (TextValue) lvalue;
            MapValue mvr = (MapValue) rvalue;
            Set<String> keys = mvr.value().keySet();

            for(String s : keys){
                if(s.equals(tvl.value())){
                    boolRetorno = true;
                    break;
                }
            }
            
        }
        else if(lvalue instanceof TextValue && rvalue instanceof TextValue){
            TextValue tvl = (TextValue) lvalue;
            TextValue tvr = (TextValue) rvalue;

            if(tvr.value().contains(tvl.value())){
                boolRetorno = true;
            }
        }
        else{
            Utils.abort(super.getLine());
        }

        return new BooleanValue(boolRetorno);
    }

    private Value<?> notContainsOp() {
        BooleanValue contains = (BooleanValue) containsOp();
        BooleanValue bv = new BooleanValue(!(contains.value()));
        return bv;
    }

    //Simplificacao do groovy para somente soma de inteiros e concatenacao de strings e arrays
    private Value<?> addOp() {
        Value<?> lvalue = left.expr();
        Value<?> rvalue = right.expr();

        Value<?> retorno = null;
        
        if(lvalue == null || rvalue == null){
            Utils.abort(super.getLine());
        }

        if(lvalue instanceof NumberValue && rvalue instanceof NumberValue){
            NumberValue nvl = (NumberValue) lvalue;
            NumberValue rvl = (NumberValue) rvalue;

            retorno = new NumberValue(nvl.value() + rvl.value());
        }
        else if(lvalue instanceof TextValue && rvalue instanceof TextValue){
            TextValue tvl = (TextValue) lvalue;
            TextValue tvr = (TextValue) rvalue;

            retorno = new TextValue(tvl.value() + tvr.value()); //concatenacao
        }
        else if(lvalue instanceof ArrayValue && rvalue instanceof ArrayValue){
            
            ArrayValue arrayL = (ArrayValue) lvalue;
            ArrayValue arrayR = (ArrayValue) rvalue;

            ArrayList<Value<?>> array = new ArrayList<>();
            for(Value<?> v : arrayL.value()){
                array.add(v);
            }
            for(Value<?> v : arrayR.value()){
                array.add(v);
            }
            
            retorno = new ArrayValue(array);
        }
        else if(lvalue instanceof MapValue && rvalue instanceof MapValue){
            Map<String, Value<?>> mapa = new HashMap<>();
            MapValue mvl = (MapValue) lvalue;
            MapValue mvr = (MapValue) rvalue;

            for(String k : mvl.value().keySet()){
                mapa.put(k, mvl.value().get(k));
            }

            for(String k : mvr.value().keySet()){
                if(mapa.containsKey(k)){
                    mapa.replace(k, mvr.value().get(k));
                }
                else{
                    mapa.put(k, mvr.value().get(k));
                }
            }

            retorno = new MapValue(mapa);
        }
        else if(lvalue instanceof TextValue || rvalue instanceof TextValue){
            retorno = new TextValue(lvalue.toString() + rvalue.toString());
        }
        else{
            Utils.abort(super.getLine());
            //retorno = new TextValue(lvalue.toString() + rvalue.toString());
        }

        return retorno;
    }

    //Simplificacao do groovy para somente subtracao de inteiros
    private Value<?> subOp() {
        Value<?> lvalue = left.expr();
        Value<?> rvalue = right.expr();

        if (!(lvalue instanceof NumberValue) ||
            !(rvalue instanceof NumberValue))
            Utils.abort(super.getLine());

        NumberValue nvl = (NumberValue) lvalue;
        int lv = nvl.value();

        NumberValue nvr = (NumberValue) rvalue;
        int rv = nvr.value();

        NumberValue res = new NumberValue(lv - rv);
        return res;
    }

    //Simplificacao do groovy para somente operar multiplicacao de inteiros
    private Value<?> mulOp() {
        Value<?> l = left.expr();
        Value<?> r = right.expr();

        Value<?> retorno = null;

        if(l instanceof NumberValue && r instanceof NumberValue){
            NumberValue nvl = (NumberValue) l;
            NumberValue nvr = (NumberValue) r;
            retorno = new NumberValue(nvl.value()*nvr.value());
        }
        else if(l instanceof ArrayValue && r instanceof NumberValue || l instanceof NumberValue && r instanceof ArrayValue){
            int numero = 0;
            List<Value<?>> array = null;
            if(l instanceof ArrayValue){
                array = ((ArrayValue)l).value();
                numero = ((NumberValue)r).value();
            }
            else{ //r eh o arrayValue
                array = ((ArrayValue)r).value();
                numero = ((NumberValue)l).value();
            }

            if(numero < 0){
                Utils.abort(super.getLine()); //multiplicacao de array por numero negativo
            }

            List<Value<?>> novoArray = new ArrayList<>();
            for(int i =0; i<numero; i++){
                for(int j=0; j<array.size(); j++){
                    novoArray.add(array.get(j));
                }
            }
            
            retorno = new ArrayValue(novoArray);
        }
        else{
            Utils.abort(super.getLine());
        }

        return retorno;
    }

    //Ignorar mod 0. Utilizar ArithmeticException padrao do java
    private Value<?> divOp() {
        Value<?> l = left.expr();
        Value<?> r = right.expr();

        Value<?> retorno = null;

        if(l instanceof NumberValue && r instanceof NumberValue){
            NumberValue nvl = (NumberValue) l;
            NumberValue nvr = (NumberValue) r;
            retorno = new NumberValue(nvl.value()/nvr.value());
        }
        else{
            Utils.abort(super.getLine());
        }

        return retorno;
    }

    //Ignorar mod 0. Utilizar ArithmeticException padrao do java
    private Value<?> modOp() {
        Value<?> l = left.expr();
        Value<?> r = right.expr();

        Value<?> retorno = null;

        if(l instanceof NumberValue && r instanceof NumberValue){
            NumberValue nvl = (NumberValue) l;
            NumberValue nvr = (NumberValue) r;
            retorno = new NumberValue(nvl.value()%nvr.value());
        }
        else{
            Utils.abort(super.getLine());
        }

        return retorno;
    }

    //Aceita potencia negativa-> Math.pow(), mas como o resultado eh inteiro sera 1/N, N natural, o que trunca para 0
    private Value<?> powerOp() {
        Value<?> l = left.expr();
        Value<?> r = right.expr();

        Value<?> retorno = null;

        if(l instanceof NumberValue && r instanceof NumberValue){
            NumberValue nvl = (NumberValue) l;
            NumberValue nvr = (NumberValue) r;
            retorno = new NumberValue((int) Math.pow(nvl.value(),nvr.value()));
        }
        else{
            Utils.abort(super.getLine());
        }

        return retorno;
    }
    
}
