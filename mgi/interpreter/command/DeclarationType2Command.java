package interpreter.command;

import java.util.List;

import interpreter.expr.Expr;
import interpreter.expr.Variable;
import interpreter.util.Utils;
import interpreter.value.ArrayValue;
import interpreter.value.Value;

public class DeclarationType2Command extends DeclarationCommand{
    private List<Variable> lhs;

    public DeclarationType2Command(int line, List<Variable> lhs, Expr rhs){
        super(line, rhs);
        this.lhs = lhs;
    }
    
    @Override
    public void execute() {
        Value<?> v = (rhs != null ? rhs.expr() : null);
        if(v == null){
            for(Variable var : lhs){
                var.setValue(v);
            }
        }
        else if(v instanceof ArrayValue){
            ArrayValue arv = (ArrayValue) v;
            int menorIndice = (lhs.size() < arv.value().size())? lhs.size() : arv.value().size();

            if(lhs.size() < arv.value().size()){
                menorIndice = lhs.size();
            }
            else{
                menorIndice = arv.value().size();
                for(int i=menorIndice; i<lhs.size(); i++){
                    lhs.get(i).setValue(null);
                }
            }

            for(int i = 0; i<menorIndice; i++){
                lhs.get(i).setValue(arv.value().get(i));
            }
        }
        else{
            Utils.abort(super.getLine());
        }
    }
}
