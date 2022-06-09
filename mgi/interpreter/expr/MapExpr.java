package interpreter.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import interpreter.value.MapValue;
import interpreter.value.Value;

public  class MapExpr extends Expr{
    
    public class MapItem{
        private String key;
        private Expr value;

        public MapItem(String key, Expr value){
            this.key = key;
            this.value = value;
        }
    }

    private List<MapItem> array;

    public MapExpr(int line){
        super(line);
        this.array = new ArrayList<>();
    }

    public void addItem(MapItem item){
        array.add(item);
    }

    @Override
    public Value<?> expr(){
        HashMap<String, Value<?>> mapa = new HashMap<>();

        for(MapItem i : array){
            mapa.put(i.key, i.value.expr());
        }

        MapValue mv = new MapValue(mapa);

        return mv;
    }

}
