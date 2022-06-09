package interpreter.expr;

import java.util.ArrayList;
import java.util.List;

import interpreter.value.Value;

public class SwitchExpr extends Expr {
    
    public class CaseItem{
        private Expr key;
        private Expr value;

        public CaseItem(Expr key, Expr value){
            this.key = key;
            this.value = value;
        }
    }
    
    private Expr expr; //o que sera avaliado
    private List<CaseItem> cases;
    private Expr _default;

    public SwitchExpr(int line, Expr expr){
        super(line);
        this.expr = expr;
        cases = new ArrayList<>();
    }

    public void addItem(CaseItem item){
        cases.add(item);
    }

    public void setDefault(Expr _default){
        this._default = _default;
    }

    @Override
    public Value<?> expr(){
        Value<?> retorno = null;
        Value<?> _case = expr.expr();

        //por padrao, coloca o resultado do default, se existir. Se outro caso bater, substitui o default.
        if(_default != null){
            retorno = _default.expr();
        }

        for(CaseItem i : cases){
            if(i.key.expr() == null){
                if(_case == null){
                    if(i.value == null){
                        retorno = null;
                    }
                    else{
                        retorno = i.value.expr();
                    }
                }
            }
            else{
                if(i.key.expr().equals(_case)){
                    if(i.value == null){
                        retorno = null;
                    }
                    else{
                        retorno = i.value.expr();
                    }
                }
            }
        }
    return retorno;
    }
}
