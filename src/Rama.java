import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

import java.util.ArrayList;
import java.util.HashSet;

public class Rama {
    private String contenido;
    private int ID;

    private boolean nullable = false;
    private HashSet<Rama> firstPos = new HashSet<>();
    private HashSet<Rama> lastPos = new HashSet<>();
    private HashSet<Rama> followPos = new HashSet<>();

    private Rama leftChild = null;
    private Rama rightChild = null;

    /*Tomaremos firstPos como el identificador de las ramas que tiene cada RAMA*/
    private ArrayList<String> transiciones = new ArrayList<>();
    private ArrayList<Rama> arrivals = new ArrayList<>();


    public Rama(String s){
        this.contenido = s;

    }


    public void setLeftChild(Rama leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(Rama rightChild) {
        this.rightChild = rightChild;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getContenido() {
        return contenido;
    }

    public Rama getLeftChild() {
        return leftChild;
    }

    public Rama getRightChild() {
        return rightChild;
    }

    public HashSet<Rama> getFirstPos() {
        return firstPos;
    }

    public HashSet<Rama> getLastPos() {
        return lastPos;
    }

    public HashSet<Rama> getFollowPos() {
        return followPos;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setFirstPos(HashSet<Rama> firstPos) {
        this.firstPos = firstPos;
    }

    public void setLastPos(HashSet<Rama> lastPos) {
        this.lastPos = lastPos;
    }

    public void setFollowPos(HashSet<Rama> followPos) {
        this.followPos = followPos;
    }

    public void setTransiciones(ArrayList<String> transiciones) {
        this.transiciones = transiciones;
    }

    public void setArrivals(ArrayList<Rama> arrivals) {
        this.arrivals = arrivals;
    }

    public int getID() {
        return ID;
    }

    public ArrayList<String> getTransiciones() {
        return transiciones;
    }

    public ArrayList<Rama> getArrivals() {
        return arrivals;
    }

    public void settearTodo(Rama r){
        this.contenido = r.getContenido();
        this.ID = r.getID();
        this.nullable = r.isNullable();
        this.firstPos = r.getFirstPos();
        this.lastPos = r.getLastPos();
        this.followPos = r.getFollowPos();
        this.leftChild = r.getLeftChild();
        this.rightChild = r.getRightChild();
        this.transiciones = r.getTransiciones();
        this.arrivals = r.getArrivals();

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

