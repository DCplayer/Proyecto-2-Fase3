import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Diego Castaneda on 11/08/2017.
 */
public class NodosRamas {
    private int id;
    private HashSet<Rama> conjunto;
    private ArrayList<String> transiciones = new ArrayList<>();
    private ArrayList<NodosRamas> arrivals = new ArrayList<>();


    public NodosRamas(HashSet<Rama> propiedad) {
        this.conjunto = propiedad;

    }

    public void add(String movimiento, NodosRamas llegada) {
        transiciones.add(movimiento);
        arrivals.add(llegada);

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public HashSet<Rama> getConjunto() {
        return conjunto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<String> getTransiciones() {
        return transiciones;
    }

    public ArrayList<NodosRamas> getArrivals() {
        return arrivals;
    }
}