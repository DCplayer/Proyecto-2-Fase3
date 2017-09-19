import com.sun.java.util.jar.pack.*;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.StringTokenizer;

public class LectordeArchivos {

    private ArrayList<NodosRamas> ident;
    private ArrayList<NodosRamas> number;
    private ArrayList<NodosRamas> string;
    private ArrayList<NodosRamas> Char;
    private ArrayList<NodosRamas> Characters;
    private ArrayList<NodosRamas> Keywords;
    private ArrayList<NodosRamas> Symbol;

    private Stack<String> token = new Stack<>();

    public static Token PunteroCompleto;
    public static Token lookahead = new Token();
    public static int numeroDeLinea = 0;
    public static BufferedReader lectorTokens;

    private ArrayList<String> parteDelToken = new ArrayList<>();
    private ArrayList<ArrayList<String>> estructuraTokens = new ArrayList<>();



    public LectordeArchivos(){

        /*-----------------------------Creacion de Expresiones Regulares----------------------------------------------*/
        String letter = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I" +
                "|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z";
        String digit = "0|1|2|3|4|5|6|7|8|9";
        String butQuote = "!|@|$|^|&|{|}|[|]|\'|>|<|;|:";
        String butApostrophw = "!|@|$|^|&|{|}|[|]|\"|>|<|;|:";

        String identRegex = "(" + letter+ ")"+ "(" + letter + "|" + digit + ")*";
        String numberRegex = "(" + digit+ ")"  + "(" + digit+ ")" + "*";
        String stringRegex = "\"" + "(" + letter + "|" + digit + "|" + butQuote + ")*" + "\"";
        String charRegex = "\'(" + letter + "|" + digit + "|" + butApostrophw +")\'";

        String CharRegex = "(" + charRegex  +")|(CHR(" + numberRegex + "))";
        String BasicSetRegex = "(" + stringRegex + ")|(" + identRegex + ")|((" + CharRegex + ")|(" + CharRegex + "--" + CharRegex + "))";
        String SetRegex = "(" + BasicSetRegex + ")" + "((~|-)(" + BasicSetRegex + "))*";
        String SetDeclRegex = "(" + identRegex+ ")=(" + SetRegex + ")";

        String KeywordDecl = "(" +identRegex + ")=(" +stringRegex +")";

        String SymbolRegex = "(" + identRegex + ")|(" + stringRegex + ")|(" + charRegex + ")" ;

        /*---------------------------------------------CreacionDirecta------------------------------------------------*/


        /*Se tiene la creacion directa, por medio del algoritmo de Hopcroft, diseñado en las clases de Arbol, Ramas y
        * NodosRamas. Ahora lo que se esta haciendo es crear un Automata AFD directo de la expresion Regular
        * especificada en la parte de arriba, y dejando cada uno de los NodosRamas con sus transiciones y nodos a donde
        * llegan con esas transiciones dentro de un Array, el cual es lo que se inicializa cada vez que se usa el
        * LectordeArchivos.java*/

        /*Automata ident*/
        Arbol arbolIdent = new Arbol();
        ArrayList<NodosRamas> ident = arbolIdent.CrearElAFDDirecto(identRegex);
        int numeroParaElID = 0;

        for(NodosRamas nodoRama: ident){
            nodoRama.setId(numeroParaElID);
            numeroParaElID = numeroParaElID + 1;
        }
        this.ident = ident;

        /*Automata number*/
        Arbol arbolNumber = new Arbol();
        ArrayList<NodosRamas> number = arbolNumber.CrearElAFDDirecto(numberRegex);
        numeroParaElID = 0;

        for(NodosRamas nodoRama: number){
            nodoRama.setId(numeroParaElID);
            numeroParaElID = numeroParaElID + 1;
        }
        this.number = number;

        /*Automata string*/
        Arbol arbolString = new Arbol();
        ArrayList<NodosRamas> string = arbolString.CrearElAFDDirecto(stringRegex);
        numeroParaElID = 0;

        for(NodosRamas nodoRama: string){
            nodoRama.setId(numeroParaElID);
            numeroParaElID = numeroParaElID + 1;
        }
        this.string = string;

        /*Automata char*/
        Arbol arbolChar = new Arbol();
        ArrayList<NodosRamas> Char = arbolChar.CrearElAFDDirecto(charRegex);
        numeroParaElID = 0;

        for(NodosRamas nodoRama: Char){
            nodoRama.setId(numeroParaElID);
            numeroParaElID = numeroParaElID + 1;
        }
        this.Char = Char;

        /*Automata CHARACTERS*/
        Arbol arbolCharacters = new Arbol();
        ArrayList<NodosRamas> Characters = arbolCharacters.CrearElAFDDirecto(SetDeclRegex);
        numeroParaElID = 0;


        for(NodosRamas nodoRama: Characters){
            nodoRama.setId(numeroParaElID);
            numeroParaElID = numeroParaElID + 1;
        }
        this.Characters = Characters;

        /*Automata KEYWORDS*/
        Arbol arbolKeywords = new Arbol();
        ArrayList<NodosRamas> Keywords = arbolKeywords.CrearElAFDDirecto(KeywordDecl);
        numeroParaElID = 0;

        for(NodosRamas nodoRama: Keywords){
            nodoRama.setId(numeroParaElID);
            numeroParaElID = numeroParaElID + 1;
        }
        this.Keywords = Keywords;
        /*Automata Symbool*/
        Arbol arbolSymbol = new Arbol();
        ArrayList<NodosRamas> Symbol = arbolSymbol.CrearElAFDDirecto(SymbolRegex);
        numeroParaElID = 0;

        for(NodosRamas nodoRama: Symbol){
            nodoRama.setId(numeroParaElID);
            numeroParaElID = numeroParaElID + 1;
        }
        this.Symbol = Symbol;


        /*------------------------------------------------Simular AFD-------------------------------------*/
        /*Lo que se hace es, teniendo un ArrayList de NodosRamas, se le es alimentado cada uno de los caracteres de
        * un string y se va moviendo de nodo en nodo hasta que se acaba el string, siendo el ultimo nodo de llegada el
        * resultado de ese string dentro del automata y, viendo el contenido de ese nodo, podemos verificar si es un nodo
        * de aceptacion, ergo el string es aceptado, o si este string no es aceptado por dicho automata*/
    }


    public void TokenDecl(String Token){
        parteDelToken.clear();
        token.clear();
        String nolinea = Token.replaceAll("\\s+", "");
        String linea = nolinea.substring(0, nolinea.length()-1);
        String dot = nolinea.substring(nolinea.length()-1 ,  nolinea.length());
        if(!dot.equals(".")){
            System.out.println("Syntax Error: Expected \".\" en linea " + numeroDeLinea + " de codigo escrito");
        }
        for(int i = linea.length()-1; i >= 0; i--){
            String s = linea.substring(i, i+1);
            token.push(s);
        }
        getNextToken();
        ident();
        if(!lookahead.getId().equals(".")){
            if(!match("=")){
                System.out.println("Syntax Error: Expected \"=\" en linea " + numeroDeLinea+ " de codigo escrito");
            }
            if(!TokenExpr()){
                System.out.println("Syntax Error: Expresion en linea " + numeroDeLinea + " no es una TokenExpr");

            }

        }
        parteDelToken.add(".");
        ArrayList<String> remplazo = new ArrayList<>();
        remplazo.addAll(parteDelToken);
        estructuraTokens.add(remplazo);

    }


    public void getNextToken(){
        boolean bandera = false;
        String entrega = "";
        while(!bandera && !token.isEmpty()){
            switch (token.peek()){
                case("="):
                case("."):
                case("|"):
                case("("):
                case(")"):
                case("{"):
                case("}"):
                case("["):
                case("]"):
                case("+"):
                case("-"):{
                    bandera = true;
                    if(entrega.equals("")){
                        entrega = token.pop();

                    }
                    break;
                }
                default:{
                    entrega = entrega + token.pop();
                }
            }
        }
        parteDelToken.add(entrega);
        lookahead.setId(entrega);

    }

    public void ident(){
        if(simularAFD(ident, lookahead.getId())){
            match(lookahead.getId());
        }
        else{
            System.out.println("Syntax Error: linea de codigo " + numeroDeLinea + " con identificador invalido");
        }
    }

    public boolean symbol(){
        if(simularAFD(Symbol, lookahead.getId())){
            match(lookahead.getId());
            return true;
        }
        return false;
    }

    public boolean match(String terminal){
        if(lookahead.getId().equals(terminal)){
            getNextToken();
            return true;
        }
        return false;
    }

    public boolean TokenExpr(){
        if(!TokenTerm()){
            System.out.println("TokenTerm not accepted: linea " +numeroDeLinea + " de codigo escrito");
            return false;
        }
        while(match("|")){
            if(!TokenTerm()){
                System.out.println("TokenTerm not accepted: linea " +numeroDeLinea + " de codigo escrito");
                return false;
            }
        }
        return true;


    }

    public boolean TokenTerm(){
        if(!TokenFactor()){
            return false;
        }
        while(TokenFactor()){

        }
        return true;
    }

    public boolean TokenFactor(){
        if(symbol()){
           return true;
        }
        else if(match("(")){
            if(!TokenExpr()){
                System.out.println("Syntax Error: Not a TokenExpr, linea " + numeroDeLinea + " de codigo escrito");
            }
            if(!match(")")){
                System.out.println("Syntax Error: Expected \")\" en linea " + numeroDeLinea+ " de codigo escrito");
            }
            return true;
        }
        else if(match("{")){
            if(!TokenExpr()){
                System.out.println("Syntax Error: Not a TokenExpr, linea " + numeroDeLinea + " de codigo escrito");
            }
            if(!match("}")){
                System.out.println("Syntax Error: Expected \"}\" en linea " + numeroDeLinea+ " de codigo escrito");
            }
            return true;
        }
        else if(match("[")){
            if(!TokenExpr()){
                System.out.println("Syntax Error: Not a TokenExpr, linea " + numeroDeLinea + " de codigo escrito");
            }
            if(!match("]")){
                System.out.println("Syntax Error: Expected \"]\" en linea " + numeroDeLinea+ " de codigo escrito");
            }
            return true;
        }
        else{
            return false;
        }

    }

    /*-----------------------------------------------------------------------------------------------------------------*/

    public boolean simularAFD(ArrayList<NodosRamas> listaDeAFD, String s){

        NodosRamas elNodo = listaDeAFD.get(0);

        int index = 0;
        while (index < s.length()){
            String substri = s.substring(index, index+1);
            int numero = elNodo.getTransiciones().indexOf(substri);
            NodosRamas resul = elNodo.getArrivals().get(numero);
            for(NodosRamas i: listaDeAFD){
                if(i.getConjunto().equals(resul.getConjunto())){
                    elNodo = i;
                    break;

                }
            }
            index = index + 1;
        }
        for(Rama rama: elNodo.getConjunto()){
            if(rama.getContenido().equals("#")){
                return true;
        }

        }
        return false;

    }

    /*Metodo para la separacion de cada una de las lineas de un .txt dentro de un ArrayList de String */
    public ArrayList<String> crearLector(String archivo){
        ArrayList<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.equals("")){
                    lineas.add(line);
                }

            }
        }
        catch (IOException e){

        }

        return lineas;
    }


    /*-------------------------------------------ChequearSintaxisDelArchivo--------------------------------------------*/
    public boolean chequearSintaxis(ArrayList<String> lineas){

        boolean resultado = true;
        /*Chequear en caso el documento esta vacio*/
        if(lineas.size() == 0){
            System.out.println("Archivo Vacio");
            resultado =  false;
        }

        /*Chequear si la primera linea esta correcta*/
        StringTokenizer primeraLinea = new StringTokenizer(lineas.get(0));
        if(!primeraLinea.nextToken().equals("COMPILER")){
            System.out.println("Syntax error: COMPILER not found");
            resultado =   false;
        }
        String identificador = primeraLinea.nextToken();
        if(!simularAFD(ident, identificador)){
            System.out.println("Syntax error: ident " + identificador +  " not viable ");
            resultado =   false;
        }

        /*verificar en que lineas se encuentran las palabras Characters y Keywords o si no existen siquiera en el
        * documento*/
        int numeroCharacters = 0;
        int numeroKeywords = 0;
        int numeroTokens = 0;
        for(int s = 0; s<lineas.size(); s++){
            StringTokenizer lineaALeer = new StringTokenizer(lineas.get(s));
            String laPrueba = lineaALeer.nextToken();
            if(laPrueba.equals("CHARACTERS")){
                numeroCharacters = s +1;
            }
            else if(laPrueba.equals("KEYWORDS")){
                numeroKeywords = s +1;
            }
            else if(laPrueba.equals("TOKENS")){
                numeroTokens = s + 1;
            }

        }

        boolean soloChequeando = true;
        /*Verificar si existen las palabras reservadas, simular si esta correcto lo que se puso debajo de ellas */
        if(numeroCharacters != 0 && numeroKeywords == 0 && numeroTokens == 0){
            /*Solo se encontro la palabra CHARACTERS*/
            for(int i = numeroCharacters;i < lineas.size()-2; i++ ){
                boolean sintaxis = true;
                String lineaConPunto = lineas.get(i);
                lineaConPunto = lineaConPunto.replaceAll("\\s+","");
                String lineaSinPunto = lineaConPunto.substring(0, lineaConPunto.length()-1);

                if(!lineaConPunto.substring(lineaConPunto.length()-1, lineaConPunto.length()).equals(".")){
                    resultado = false;
                    sintaxis = false;
                    System.out.println("Syntax error: Line " +(i+1) + " written code, exprected \'.\'");
                }
                if(sintaxis){
                    HashSet<NodosRamas> prueba = new HashSet<>();
                    HashSet<Rama> masPruebas = new HashSet<>();
                    for(NodosRamas j: Characters){
                        if(j.getConjunto().size() == 0){
                            prueba.add(j);
                        }
                        for(Rama x: j.getConjunto()){
                            if(x.getContenido().equals("#")){
                                masPruebas.add(x);
                            }
                        }
                    }
                    for(NodosRamas unNodo: prueba){
                        unNodo.getConjunto().addAll(masPruebas);
                    }

                    if(!simularAFD(Characters, lineaSinPunto)){
                        resultado = false;
                        System.out.println("Syntax error: Line " +(i+1) + " of written code, expression "+
                                lineaConPunto+ "is not a CHARACTER expression");
                    }
                }



            }

        }else if(numeroCharacters == 0 && numeroKeywords != 0 && numeroTokens == 0){
            /*Solo se encontro la palabra KEYWORDS*/
            for(int i = numeroKeywords; i < lineas.size()-2; i++){
                boolean sintaxis = true;
                String lineaConPunto = lineas.get(i);
                lineaConPunto = lineaConPunto.replaceAll("\\s+","");
                String lineaSinPunto = lineaConPunto.substring(0, lineaConPunto.length()-1);

                if(!lineaConPunto.substring(lineaConPunto.length()-1, lineaConPunto.length()).equals(".")){
                    resultado = false;
                    sintaxis = false;
                    System.out.println("Syntax error: Line " +(i+1) + " written code, exprected \'.\'");
                }

                if(sintaxis){
                    if(!simularAFD(Keywords, lineaSinPunto)){
                        resultado = false;
                        System.out.println("Syntax error: Line " +(i+1)+ " of written code, " + lineaConPunto +
                                " not an KEYWORD expresion");
                    }
                }
            }

        }else if(numeroCharacters != 0 && numeroKeywords != 0 && numeroTokens == 0){
            /*Se encontraron ambas palabras, y se valida si Characters esta antes que Keywords*/
            if(numeroKeywords < numeroCharacters){
                resultado = false;
                soloChequeando = false;
                System.out.println("Syntax error: KEYWORDS before CHARACTERS");

            }
            if(soloChequeando){
                for(int i = numeroCharacters; i < numeroKeywords-1; i++){
                        boolean sintaxis = true;
                        String lineaConPunto = lineas.get(i);
                        lineaConPunto = lineaConPunto.replaceAll("\\s+","");
                        String lineaSinPunto = lineaConPunto.substring(0, lineaConPunto.length()-1);

                        if(!lineaConPunto.substring(lineaConPunto.length()-1, lineaConPunto.length()).equals(".")){
                            resultado = false;
                            sintaxis = false;
                            System.out.println("Syntax error: Line " +(i+1) + " written code, exprected \'.\'");
                        }
                        if(sintaxis){
                            HashSet<NodosRamas> prueba = new HashSet<>();
                            HashSet<Rama> masPruebas = new HashSet<>();
                            for(NodosRamas j: Characters){
                                if(j.getConjunto().size() == 0){
                                    prueba.add(j);
                                }
                                for(Rama x: j.getConjunto()){
                                    if(x.getContenido().equals("#")){
                                        masPruebas.add(x);
                                    }
                                }
                            }
                            for(NodosRamas unNodo: prueba){
                                unNodo.getConjunto().addAll(masPruebas);
                            }

                            if(!simularAFD(Characters, lineaSinPunto)){
                                resultado = false;
                                System.out.println("Syntax error: Line " +(i+1) + " of written code, expression "+
                                        lineaConPunto+ "is not a CHARACTER expression");
                            }
                        }



                }
                for(int i = numeroKeywords; i< lineas.size()-1; i++){

                    boolean sintaxis = true;
                    String lineaConPunto = lineas.get(i);
                    lineaConPunto = lineaConPunto.replaceAll("\\s+","");
                    String lineaSinPunto = lineaConPunto.substring(0, lineaConPunto.length()-1);

                    if(!lineaConPunto.substring(lineaConPunto.length()-1, lineaConPunto.length()).equals(".")){
                        resultado = false;
                        sintaxis = false;
                        System.out.println("Syntax error: Line " +(i+1) + " written code, exprected \'.\'");
                    }

                    if(sintaxis){
                        if(!simularAFD(Keywords, lineaSinPunto)){
                            resultado = false;
                            System.out.println("Syntax error: Line " +(i+1)+ " of written code, " + lineaConPunto +
                                    " not an KEYWORD expresion");
                        }
                    }


                }
            }
        }else if(numeroCharacters == 0 && numeroKeywords == 0 && numeroTokens != 0){
            /*Se encuentra la palabra TOKEN pero no ninguna de las anteriores*/
            System.out.println("Syntax Error: Tokens must depend on CHARACTERS");

        }else if(numeroCharacters == 0 && numeroKeywords != 0 && numeroTokens != 0){
            /*Se encuentra la palabra KEYWORDS y TOKENS*/
            System.out.println("Syntax Error: Tokens must depend on CHARACTERS");


        }else if(numeroCharacters != 0 && numeroKeywords == 0 && numeroTokens != 0){/*--------------------------------*/
            /*Se encuentra las palabras CHARACTERS y TOKENS*/
            for(int i = numeroCharacters; i < numeroKeywords-1; i++){
                boolean sintaxis = true;
                String lineaConPunto = lineas.get(i);
                lineaConPunto = lineaConPunto.replaceAll("\\s+","");
                String lineaSinPunto = lineaConPunto.substring(0, lineaConPunto.length()-1);

                if(!lineaConPunto.substring(lineaConPunto.length()-1, lineaConPunto.length()).equals(".")){
                    resultado = false;
                    sintaxis = false;
                    System.out.println("Syntax error: Line " +(i+1) + " written code, exprected \'.\'");
                }
                if(sintaxis){
                    HashSet<NodosRamas> prueba = new HashSet<>();
                    HashSet<Rama> masPruebas = new HashSet<>();
                    for(NodosRamas j: Characters){
                        if(j.getConjunto().size() == 0){
                            prueba.add(j);
                        }
                        for(Rama x: j.getConjunto()){
                            if(x.getContenido().equals("#")){
                                masPruebas.add(x);
                            }
                        }
                    }
                    for(NodosRamas unNodo: prueba){
                        unNodo.getConjunto().addAll(masPruebas);
                    }

                    if(!simularAFD(Characters, lineaSinPunto)){
                        resultado = false;
                        System.out.println("Syntax error: Line " +(i+1) + " of written code, expression "+
                                lineaConPunto+ "is not a CHARACTER expression");
                    }
                }

            }

            for(int i = numeroTokens; i< lineas.size()-1; i++){
                numeroDeLinea = i;
                TokenDecl(lineas.get(i));

            }
        }else if(numeroCharacters != 0 && numeroKeywords != 0 && numeroTokens != 0){
            /*Se encuentran las palabras CHARACTERS, KEYWORDS y TOKENS*/
            if(numeroKeywords < numeroCharacters){
                resultado = false;
                soloChequeando = false;
                System.out.println("Syntax error: KEYWORDS before CHARACTERS");

            }
            if(soloChequeando){
                for(int i = numeroCharacters; i < numeroKeywords-1; i++){
                    boolean sintaxis = true;
                    String lineaConPunto = lineas.get(i);
                    lineaConPunto = lineaConPunto.replaceAll("\\s+","");
                    String lineaSinPunto = lineaConPunto.substring(0, lineaConPunto.length()-1);

                    if(!lineaConPunto.substring(lineaConPunto.length()-1, lineaConPunto.length()).equals(".")){
                        resultado = false;
                        sintaxis = false;
                        System.out.println("Syntax error: Line " +(i+1) + " written code, exprected \'.\'");
                    }
                    if(sintaxis){
                        HashSet<NodosRamas> prueba = new HashSet<>();
                        HashSet<Rama> masPruebas = new HashSet<>();
                        for(NodosRamas j: Characters){
                            if(j.getConjunto().size() == 0){
                                prueba.add(j);
                            }
                            for(Rama x: j.getConjunto()){
                                if(x.getContenido().equals("#")){
                                    masPruebas.add(x);
                                }
                            }
                        }
                        for(NodosRamas unNodo: prueba){
                            unNodo.getConjunto().addAll(masPruebas);
                        }

                        if(!simularAFD(Characters, lineaSinPunto)){
                            resultado = false;
                            System.out.println("Syntax error: Line " +(i+1) + " of written code, expression "+
                                    lineaConPunto+ "is not a CHARACTER expression");
                        }
                    }



                }
                for(int i = numeroKeywords; i< numeroTokens-1; i++){

                    boolean sintaxis = true;
                    String lineaConPunto = lineas.get(i);
                    lineaConPunto = lineaConPunto.replaceAll("\\s+","");
                    String lineaSinPunto = lineaConPunto.substring(0, lineaConPunto.length()-1);

                    if(!lineaConPunto.substring(lineaConPunto.length()-1, lineaConPunto.length()).equals(".")){
                        resultado = false;
                        sintaxis = false;
                        System.out.println("Syntax error: Line " +(i+1) + " written code, exprected \'.\'");
                    }

                    if(sintaxis){
                        if(!simularAFD(Keywords, lineaSinPunto)){
                            resultado = false;
                            System.out.println("Syntax error: Line " +(i+1)+ " of written code, " + lineaConPunto +
                                    " not an KEYWORD expresion");
                        }
                    }


                }
                for (int i = numeroTokens; i <  lineas.size()-1; i++){
                    numeroDeLinea  = i ;
                    TokenDecl(lineas.get(i));

                }
                System.out.println(estructuraTokens);
            }
        }



        /*Chequear si la ultima linea esta correcta*/
        StringTokenizer ultimaLinea = new StringTokenizer(lineas.get(lineas.size()-1));
        if (!ultimaLinea.nextToken().equals("END")){
            System.out.println("Syntax error: END not found");
            resultado =   false;
        }
        String identificadorFinal = ultimaLinea.nextToken();
        String identFinal = identificadorFinal.substring(0, identificadorFinal.length() - 1);
        String dot = identificadorFinal.substring(identificadorFinal.length() - 1, identificadorFinal.length());
        if(!dot.equals(".")){
            System.out.println("Syntax error: notation \".\" not found");
            resultado = false;
        }
        else if(!identificador.equals(identFinal)){
            System.out.println("Syntax error: identificator "+ identFinal+ " not found");
            resultado =   false;
        }

        return resultado;
    }










}
