package interpreter.expr;

import java.util.List;

import interpreter.util.Utils;
import interpreter.value.ArrayValue;
import interpreter.value.MapValue;
import interpreter.value.NumberValue;
import interpreter.value.TextValue;
import interpreter.value.Value;

public class AccessExpr extends SetExpr{
    
    private Expr base;
    private Expr index; //indice eh sempre uma Variavel ou NumberValue (ou TextValue para mapas), que pode existir na memoria ou nao. Se nao existir, deve ser tratado como texto (String key = texto)
    //index.expr() retornara null se nao existe no mapa da memoria

    public AccessExpr(int line, Expr base, Expr index){
        super(line);
        this.base = base;
        this.index = index;
    }

    @Override
    public Value<?> expr() {
        Value<?> b = base.expr();
        Value<?> expIndex = index.expr();

        Value<?> retorno = null;

        if(b instanceof ArrayValue && expIndex instanceof NumberValue){
            ArrayValue arv = (ArrayValue) b;
            NumberValue nv = (NumberValue) expIndex;
            List<Value<?>> array = arv.value();

            if(nv.value() < 0){
                Utils.abort(super.getLine());
            }

            if(nv.value() < array.size()){
                retorno = array.get(nv.value());
            }
            else{
                retorno = null;
            }
        }
        else if(b instanceof MapValue){
            String str = ((TextValue)expIndex).value();
            MapValue mv = (MapValue) b;
            if(mv.value().containsKey(str)){
                retorno = mv.value().get(str);
            }
            else{
                return null;
            }
        }
        else{ //tentou utilizar . ou [] em algo que nao eh mapa ou array
            Utils.abort(super.getLine());
        }

        return retorno;
    }

    @Override
    public void setValue(Value<?> value) {
        Value<?> b = base.expr();
        Value<?> expIndex = index.expr();

        if(b instanceof ArrayValue){ //index pode ser variavel ou NumberValue
            if(expIndex instanceof NumberValue){ //index eh uma (expressao ou variavel) que resulta em NumberValue
                ArrayValue arv = (ArrayValue) b;
                int n = ((NumberValue)expIndex).value();
                List<Value<?>> array = arv.value();
                if(n >= array.size()){
                    for(int i = array.size(); i<=n; i++){
                        if(i==n){
                            array.add(value);
                        }
                        else{
                            array.add(null);
                        }
                    }
                }
                arv.value().set(((NumberValue)expIndex).value(), value); //pode dar erro se index out of bounds
            }
            else{
                Utils.abort(super.getLine());
            }
        }
        //index pode ser uma variavel ou TextValue.
        //Se for variavel, deve expandir para TextValue se existir.
            //Se nao exitir, o nome da variavel deve ser considerado como chave
        else if(b instanceof MapValue){
            String str = ((TextValue) index.expr()).value();
            MapValue mv = (MapValue) b;
            if(mv.value().containsKey(str)){
                mv.value().replace(str, value);
            }
            else{
                mv.value().put(str, value);
            }
        }
        else{ //tentou utilizar . ou [] em algo que nao eh mapa ou array
            Utils.abort(super.getLine());
        }
    }
}
