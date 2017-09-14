import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public class Arbol {
    private int index = 0;
    private ArrayList<Rama> transicionesIdentificadas = new ArrayList<>();
    private ArrayList<Rama> ramasDelArbol = new ArrayList<>();

    private Stack<Rama> arboleda = new Stack<>();
    private NormalizadorDeRegex norma = new NormalizadorDeRegex();

    public Arbol(){

    }

    public void crearElArbol(String regex){
        String conNumeralAlfinal = "("+ regex +")#";
        String postfixeado = norma.PostFixYNormalizar(conNumeralAlfinal);


        int j = postfixeado.length();
        for (int i = 0; i < j; i++) {
            String x = postfixeado.substring(i, i + 1);

            if(x.equals(".") || x.equals("|")){
                Rama b = arboleda.pop();
                Rama a = arboleda.pop();
                Rama laRama = new Rama(x);

                laRama.setRightChild(b);
                laRama.setLeftChild(a);
                ramasDelArbol.add(laRama);
                arboleda.push(laRama);

            }
            else if(x.equals("+")||x.equals("*")) {
                Rama a = arboleda.pop();
                Rama laRama = new Rama(x);
                laRama.setLeftChild(a);
                laRama.setRightChild(null);

                ramasDelArbol.add(laRama);
                arboleda.push(laRama);

            }
            else{
                Rama a = new Rama(x);
                ramasDelArbol.add(a);
                transicionesIdentificadas.add(a);
                arboleda.push(a);

            }
        }

    }

    public void nombrarListas(){
        int contador = 1;
        for (Rama r: transicionesIdentificadas){
            r.setID(contador);
            contador = contador + 1;
        }



    }

    public void nullable() {
        for (Rama r : ramasDelArbol) {
            if(r.getContenido().equals("@")){
                r.setNullable(true);
            }
            else if(r.getContenido().equals("*")){
                r.setNullable(true);
            }
            else if(r.getContenido().equals("|")){
                if(r.getLeftChild().isNullable() || r.getRightChild().isNullable()){
                    r.setNullable(true);
                }
            }
            else if(r.getContenido().equals(".")){
                if(r.getLeftChild().isNullable() && r.getRightChild().isNullable()){
                    r.setNullable(true);
                }

            }
        }
    }

    public void firstPos(){
        for (Rama r: ramasDelArbol){
            if(r.getContenido().equals("*")){
                r.getFirstPos().addAll(r.getLeftChild().getFirstPos());
            }
            else if(r.getContenido().equals("|")){
                r.getFirstPos().addAll(r.getLeftChild().getFirstPos());
                r.getFirstPos().addAll(r.getRightChild().getFirstPos());
            }
            else if(r.getContenido().equals(".")){
                if(r.getLeftChild().isNullable()){
                    r.getFirstPos().addAll(r.getLeftChild().getFirstPos());
                    r.getFirstPos().addAll(r.getRightChild().getFirstPos());
                }
                else{
                    r.getFirstPos().addAll(r.getLeftChild().getFirstPos());
                }
            }
        }
    }

    public void lastPos(){
        for (Rama r: ramasDelArbol){
            if(r.getContenido().equals("*")){
                r.getLastPos().addAll(r.getLeftChild().getLastPos());
            }
            else if(r.getContenido().equals("|")){
                r.getLastPos().addAll(r.getLeftChild().getLastPos());
                r.getLastPos().addAll(r.getRightChild().getLastPos());
            }
            else if(r.getContenido().equals(".")){
                if(r.getRightChild().isNullable()){
                    r.getLastPos().addAll(r.getLeftChild().getLastPos());
                    r.getLastPos().addAll(r.getRightChild().getLastPos());
                }
                else{
                    r.getLastPos().addAll(r.getRightChild().getLastPos());
                }

            }
        }
    }

    /*REVISAR: Si el arqueotipo de followPos de concatenacion funciona tambien para el Or*/
    /*ARREGLADO: El OR no afecta al calculo de followPos, ni el KleeneSuma. Simplemente se obvian de la ecuacion*/
    public void followPos(){
        for (Rama r: ramasDelArbol){
            if(r.getContenido().equals(".")){
                for(Rama x :r.getLeftChild().getLastPos()){
                    x.getFollowPos().addAll(r.getRightChild().getFirstPos());
                }

            }
            else if(r.getContenido().equals("*")){
                for (Rama y: r.getLastPos()){
                    y.getFollowPos().addAll(r.getFirstPos());
                }
            }
        }
    }

    public void primeroLoPrimerio(){
        for (Rama i: transicionesIdentificadas){
            if (!i.getContenido().equals("@")){
                i.getFirstPos().add(i);
                i.getLastPos().add(i);
            }
        }
    }

    public HashSet<Rama> movimiento(String identity, NodosRamas x){
        ArrayList<Rama> y = new ArrayList<>();
        for (Rama r: x.getConjunto()){
            if (r.getContenido().equals(identity)){
                y.add(r);
            }
        }

        HashSet<Rama> resultado = new HashSet<>();
        for(Rama i: y){
            resultado.addAll(i.getFollowPos());

        }
        return resultado;
    }

    public boolean estaEnElConjunto(ArrayList<NodosRamas> listado, NodosRamas nodazo){
        for (NodosRamas r: listado){
            if(r.getConjunto().equals(nodazo.getConjunto())){
                return true;
            }
        }
        return false;
    }

    public ArrayList<NodosRamas> CrearElAFDDirecto(String regex){

        crearElArbol(regex);
        nombrarListas();
        primeroLoPrimerio();
        nullable();
        firstPos();
        lastPos();
        followPos();

        /*Creando el alfabeto del AFD*/
        HashSet<String> alfabeto = new HashSet<>();
        for(Rama elemento: transicionesIdentificadas){
            if(!elemento.getContenido().equals("@") && !elemento.getContenido().equals("#")){
                alfabeto.add(elemento.getContenido());
            }

        }

        ArrayList<NodosRamas> marcado = new ArrayList<>();
        ArrayList<NodosRamas> noMarcado = new ArrayList<>();
        ArrayList<NodosRamas> nodosDelAFD = new ArrayList<>();

        /*Encontrando la raiz del arbol recientemente creado*/

        /*REVISAR ESTO POR SI CAUSA PROBLEMAS*/
        /*Nuevo Update, probablemente ya no cause problemas, previendo que se dio el firstpos de la raiz, como se
        * especifica en el libro del dragon*/


        for(Rama nodo: ramasDelArbol){
            if(nodo.getLeftChild() != null && nodo.getRightChild() != null){
                if (nodo.getRightChild().getContenido().equals("#")){
                    HashSet<Rama> raiz = nodo.getFirstPos();
                    NodosRamas enraizado = new NodosRamas(raiz);
                    noMarcado.add(enraizado);
                }
            }

        }


        int tamano = 0;
        while(true){
            tamano = noMarcado.size();
            if(index >= tamano){
                break;
            }

            NodosRamas x = noMarcado.get(index);
            marcado.add(x);
            index = index + 1;

            for (String stringy: alfabeto){

                HashSet<Rama> movidaDelNodo = movimiento(stringy, x);
                NodosRamas nodoAgregado = new NodosRamas(movidaDelNodo);
                x.add(stringy, nodoAgregado);


                /*He aqui el probelma, no esta diferenciando los objetos de NodosRamas correctamente, preferible hacer
                * un comparador con un atributo, modificar el .equals o bien usar un HasSet*/
                /*int revision = 0;
                while (revision < noMarcado.size()){
                    if(noMarcado.get(revision).getConjunto().equals(nodoAgregado.getConjunto())){
                        noMarcado.add(nodoAgregado);
                        break;
                    }
                }*/
                if (!estaEnElConjunto(noMarcado, nodoAgregado)){
                    noMarcado.add(nodoAgregado);
                }

            }
        }
        return marcado;
    }





}
