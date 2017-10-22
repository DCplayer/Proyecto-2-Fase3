import com.sun.corba.se.impl.corba.ServerRequestImpl;
import com.sun.java.util.jar.pack.*;
import com.sun.javafx.scene.traversal.SceneTraversalEngine;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class LectordeArchivos {

    private ArrayList<NodosRamas> ident;
    private ArrayList<NodosRamas> number;
    private ArrayList<NodosRamas> string;
    private ArrayList<NodosRamas> Char;
    private ArrayList<NodosRamas> Characters;
    private ArrayList<NodosRamas> Keywords;
    private ArrayList<NodosRamas> Symbol;
    private ArrayList<NodosRamas> Any;

    private Stack<String> token = new Stack<>();

    public static Token PunteroCompleto;
    public static Token lookahead = new Token();
    public static int numeroDeLinea = 0;
    public static BufferedReader lectorTokens;

    private ArrayList<String> parteDelToken = new ArrayList<>();                 //Cada Token procesado
    private ArrayList<ArrayList<String>> estructuraTokens = new ArrayList<>();   //Conjunto de todos los Tokens

    private ArrayList<String> parteDeLaProduccion = new ArrayList<>();                  //Cada una de las producciones
    private ArrayList<ArrayList<String>> estructuraProducciones = new ArrayList<>();    //Conjunto de Producciones
    private Stack<String> produccion = new Stack<>();

    private int cantidadKeywords = 0;

    private ArrayList<String> abecedario = new ArrayList<>();
    private ArrayList<ArrayList<String>> laBanca = new ArrayList<>();
    private ArrayList<String> peticiones = new ArrayList<>();
    private int numeroDePeticion = 0;




    public LectordeArchivos(){

        /*-----------------------------Creacion de Expresiones Regulares----------------------------------------------*/
        String letter = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I" +
                "|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|É";
        String digit = "0|1|2|3|4|5|6|7|8|9";
        String butQuote = "!|@|$|^|&|{|}|[|]|\'|>|<|;|:|_|,|-|æ|Æ|┼|\\|«|»|~|©";
        String butApostrophw = "!|@|$|^|&|{|}|[|]|\"|>|<|;|:|_|,|-|æ|Æ|┼|\\|«|»|~|©";

        String identRegex = "(" + letter+ ")"+ "(" + letter + "|" + digit + ")*";
        String numberRegex = "(" + digit+ ")"  + "(" + digit+ ")" + "*";
        String stringRegex = "\"" + "(" + letter + "|" + digit + "|" + butQuote + ")*" + "\"";
        String charRegex = "\'(" + letter + "|" + digit + "|" + butApostrophw +")\'";

        String any = "("+butApostrophw + ")";

        String CharRegex = "(" + charRegex  +")|(CHR(" + numberRegex + "))";
        String BasicSetRegex = "(" + stringRegex + ")|(" + identRegex + ")|((" + CharRegex + ")|(" + CharRegex + "--" + CharRegex + "))";
        String SetRegex = "(" + BasicSetRegex + ")" + "((┼|-)(" + BasicSetRegex + "))*";
        String SetDeclRegex = "(" + identRegex+ ")=(" + SetRegex + ")";

        String KeywordDecl = "(" +identRegex + ")=(" +stringRegex +")";

        String SymbolRegex = "(" + identRegex + ")|(" + stringRegex + ")|(" + charRegex + ")" ;

        /*---------------------------------------------CreacionDirecta------------------------------------------------*/
        abecedario.add("á");
        abecedario.add("í");
        abecedario.add("ó");
        abecedario.add("ú");
        abecedario.add("ñ");
        abecedario.add("Ñ");
        abecedario.add("ª");
        abecedario.add("º");
        abecedario.add("¿");
        abecedario.add("®");
        abecedario.add("¬");
        abecedario.add("½");
        abecedario.add("¼");
        abecedario.add("¡");
        abecedario.add("«");
        abecedario.add("»");
        abecedario.add("↑");
        abecedario.add("▒");
        abecedario.add("▓");
        abecedario.add("│");

        /*Se tiene la creacion directa, por medio del algoritmo de Hopcroft, diseñado en las clases de Arbol, Ramas y
        * NodosRamas. Ahora lo que se esta haciendo es crear un Automata AFD directo de la expresion Regular
        * especificada en la parte de arriba, y dejando cada uno de los NodosRamas con sus transiciones y nodos a donde
        * llegan con esas transiciones dentro de un Array, el cual es lo que se inicializa cada vez que se usa el
        * LectordeArchivos.java*/
        /*Automata Any servira para la parte de productions*/
        Arbol arbolAny = new Arbol();
        ArrayList<NodosRamas> Any = arbolAny.CrearElAFDDirecto(any);
        int numeroParaElID = 0;

        for(NodosRamas nodoRama: Any){
            nodoRama.setId(numeroParaElID);
            numeroParaElID = numeroParaElID + 1;
        }
        this.Any = Any;


        /*Automata ident*/
        Arbol arbolIdent = new Arbol();
        ArrayList<NodosRamas> ident = arbolIdent.CrearElAFDDirecto(identRegex);
        numeroParaElID = 0;

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
        /*Lo que viene a continuación es el laboratorio 7 de compiladores en donde se desmenuza la parte de tokens para
        ver si su sintaxis es correcta o no. Ademas de ello, se generan las expresiones regulares en  */
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
                case("\'"):{
                    parteDelToken.add(token.pop());
                    break;
                }
                case("\""):{
                    parteDelToken.add(token.pop());
                    break;
                }
                case("┼"):
                case(">"):
                case("<"):
                case("="):
                case("."):
                case("|"):
                case("«"):
                case("»"):
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
        if(lookahead.getId().equals("«") || lookahead.getId().equals("»")){
            match(lookahead.getId());
            return true;
        }

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
        else if(match("«")){
            if(!TokenExpr()){
                System.out.println("Syntax Error: Not a TokenExpr, linea " + numeroDeLinea + " de codigo escrito");
            }
            if(!match("»")){
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



    /*--PRODUCTIONS---------------------------------------------------------------------------------------------------*/
    public void ParserSpecification(String Production){
        parteDeLaProduccion.clear();
        produccion.clear();
        String nolinea = Production.replaceAll("\\s+","");
        String linea = nolinea.substring(0,nolinea.length()-1);
        String dot = nolinea.substring(nolinea.length()-1, nolinea.length());
        if(!dot.equals(".")){
            System.out.println("Syntax Error: Exprected \".\" in line " + numeroDeLinea + " of written code");
        }
        for(int i = linea.length()-1; i >= 0; i--){
            String s = linea.substring(i, i+1);
            produccion.push(s);
        }
        getNextProduct();
        production();

        parteDeLaProduccion.add(".");
        ArrayList<String> remplazo = new ArrayList<>();
        remplazo.addAll(parteDeLaProduccion);
        estructuraProducciones.add(remplazo);



    }

    public void getNextProduct(){
        boolean bandera = false;
        String entrega = "";
        while(!bandera && !produccion.isEmpty()){
            switch (produccion.peek()){
                case("\'"):{
                    parteDeLaProduccion.add(produccion.pop());
                    break;
                }
                case("\""):{
                    parteDeLaProduccion.add(produccion.pop());
                    break;
                }
                case("×"):
                case("Æ"):
                case("┼"):
                case(">"):
                case("<"):
                case("="):
                case("."):
                case("|"):
                case("«"):
                case("»"):
                case("{"):
                case("}"):
                case("["):
                case("]"):
                case("+"):
                case("-"):{
                    bandera = true;
                    if(entrega.equals("")){
                        entrega = produccion.pop();

                    }
                    break;
                }
                default:{
                    entrega = entrega + produccion.pop();
                }
            }
        }
        parteDeLaProduccion.add(entrega);
        lookahead.setId(entrega);
    }

    public boolean production(){

        identA();

        if(atribute()){
            if(semaction()){

            }

        }else if(semaction()){


        }
        if(!matchA("=")){
            System.out.println("Production not accepted: Missing \"=\" in line " + numeroDeLinea+" of written code");
            return false;
        }
        if(!expression()){
            System.out.println("Production not accepted: Missing Expression in line " + numeroDeLinea+" of written code" );
            return false;



        }

        return true;


    }

    public boolean expression(){
        if(!term()){
            System.out.println("Production not accepted: Missing Term in line " +numeroDeLinea + " of written code");
            return false;
        }
        while(matchA("|")){
            if(!term()){
                System.out.println("Production not accepted: Missing Term in line " +numeroDeLinea + " of written code");
                return false;
            }
        }
        return true;
    }

    public boolean term(){
        if(!factor()){
            System.out.println("Production not accepted: Not a factor in line " + numeroDeLinea+ " of written code");
            return false;
        }
        while(factor()){

        }
        return true;

    }

    public boolean factor(){
        if(any()){
            if(atribute()){

            }
            return true;
        }
        else if(matchA("«")){
            if(!expression()){
                System.out.println("Syntax Error: Not a Production, line  " + numeroDeLinea + " of written code");
            }
            if(!matchA("»")){
                System.out.println("Syntax Error: Expected \")\" on line " + numeroDeLinea+ " of written code");
            }
            return true;
        }
        else if(matchA("{")){
            if(!expression()){
                System.out.println("Syntax Error: Not a Production, line " + numeroDeLinea + " of written code");
            }
            if(!matchA("}")){
                System.out.println("Syntax Error: Expected \"}\" on line " + numeroDeLinea+ " of written code");
            }
            return true;
        }
        else if(matchA("[")){
            if(!expression()){
                System.out.println("Syntax Error: Not a Production, line " + numeroDeLinea + " of written code");
            }
            if(!matchA("]")){
                System.out.println("Syntax Error: Expected \"]\" on line " + numeroDeLinea+ " of written code");
            }
            return true;
        }
        else if(semaction()){
            return true;

        }
        else{
            return false;
        }
    }

    public boolean atribute(){
        if(!matchA("<")){
            System.out.println("Syntax Error: Missing \"<\" on line " + numeroDeLinea + " of written code, not an " +
                    "Atribute");
            return false;

        }
        if(!matchA("Æ")){
            System.out.println("Syntax Error: Missing \".\" on line " + numeroDeLinea + " of written code, not an " +
                    "Atribute");
            return false;
        }

        while(any()){

        }


        if(!matchA("Æ")){
            System.out.println("Syntax Error: Missing \".\" on line " + numeroDeLinea + " of written code, not an " +
                    "Atribute");
            return false;
        }
        if(!matchA(">")){
            System.out.println("Syntax Error: Missing \">\" on line " + numeroDeLinea + "of written code, not an " +
                    "Atribute");
            return false;
        }

        return true;

    }

    public boolean semaction(){
        if(!matchA("«")){
            System.out.println("Syntax Error: Missing \"(\" on line " + numeroDeLinea + " of written code, not a " +
                    "SemAction");
            return false;

        }
        if(!matchA("Æ")){
            System.out.println("Syntax Error: Missing \".\" on line " + numeroDeLinea + " of written code, not a " +
                    "SemAction");
            return false;
        }

        while(any()){

        }

        if(!matchA("Æ")){
            System.out.println("Syntax Error: Missing \".\" on line " + numeroDeLinea + "of written code, not a " +
                    "SemAction");
            return false;
        }
        if(!matchA("»")){
            System.out.println("Syntax Error: Missing \")\" on line " + numeroDeLinea + " of written code, not a " +
                    "SemAction");
            return false;
        }

        return true;
    }

    public boolean any(){
        switch (lookahead.getId()){
            case("Æ"):
                matchA(lookahead.getId());
                return false;
            case("("):
            case(")"):
            case("©"):
            case("×"):
            case("┼"):
            case(">"):
            case("<"):
            case("="):
            case("."):
            case("|"):
            case("«"):
            case("»"):
            case("{"):
            case("}"):
            case("["):
            case("]"):
            case("+"):
            case("-"):
                matchA(lookahead.getId());
                return true;
            default:
                if(simularAFD(ident, lookahead.getId())){
                    matchA(lookahead.getId());
                    return true;

        }



        }
        return false;
    }

    public boolean matchA(String terminal){
        if(lookahead.getId().equals(terminal)){
            getNextProduct();
            return true;
        }
        return false;
    }

    public void identA(){
        if(simularAFD(ident, lookahead.getId())){
            matchA(lookahead.getId());
        }
        else{
            System.out.println("Syntax Error: linea de codigo " + numeroDeLinea + " con identificador invalido");
        }
    }



    /*----------------------------------------------------------------------------------------------------------------*/

    public boolean simularAFD(ArrayList<NodosRamas> listaDeAFD, String s){

        NodosRamas elNodo = listaDeAFD.get(0);

        int index = 0;
        while (index < s.length()){
            String substri = s.substring(index, index+1);
            int numero = elNodo.getTransiciones().indexOf(substri);
            if(numero == -1){
                return false;
            }
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
        int numeroIgnore = 0;
        int numeroProduct = 0;
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
            else if(laPrueba.equals("IGNORE")){
                numeroIgnore = s;

            }else if(laPrueba.equals("PRODUCTIONS")){
                numeroProduct = s;
            }

        }
       cantidadKeywords = numeroTokens - numeroKeywords - 1;


        boolean soloChequeando = true;

        /*Verificar si existen las palabras reservadas, simular si esta correcto lo que se puso debajo de ellas */
        if(numeroCharacters != 0 && numeroKeywords == 0 && numeroTokens == 0){
            /*Solo se encontro la palabra CHARACTERS*/

            //Metodo para revisar hasta donde debe de leer, si en dado caso existen las palabras reservadas
            //IGNORE O PRODUCTIONS
            int tope = 0;
            switch(numeroIgnore){
                case 0:
                    switch (numeroProduct){
                        case 0:
                            tope = lineas.size()-1;
                            break;
                        default:
                            tope = numeroProduct;
                    }
                    break;
                default:
                    tope = numeroIgnore;
            }

            for(int i = numeroCharacters;i < tope; i++ ){
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

            //Metodo para revisar hasta donde debe de leer, si en dado caso existen las palabras reservadas
            //IGNORE O PRODUCTIONS
            int tope = 0;
            switch(numeroIgnore){
                case 0:
                    switch (numeroProduct){
                        case 0:
                            tope = lineas.size()-1;
                            break;
                        default:
                            tope = numeroProduct;
                    }
                    break;
                default:
                    tope = numeroIgnore;
            }

            for(int i = numeroKeywords; i < tope; i++){
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
                //Metodo para revisar hasta donde debe de leer, si en dado caso existen las palabras reservadas
                //IGNORE O PRODUCTIONS
                int tope = 0;
                switch(numeroIgnore){
                    case 0:
                        switch (numeroProduct){
                            case 0:
                                tope = lineas.size()-1;
                                break;
                            default:
                                tope = numeroProduct;
                        }
                        break;
                    default:
                        tope = numeroIgnore;
                }

                for(int i = numeroKeywords; i< tope; i++){

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
            System.err.println("Syntax Error: Tokens must depend on CHARACTERS");

        }else if(numeroCharacters == 0 && numeroKeywords != 0 && numeroTokens != 0){
            /*Se encuentra la palabra KEYWORDS y TOKENS*/
            System.err.println("Syntax Error: Tokens must depend on CHARACTERS");


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
            //Metodo para revisar hasta donde debe de leer, si en dado caso existen las palabras reservadas
            //IGNORE O PRODUCTIONS
            int tope = 0;
            switch(numeroIgnore){
                case 0:
                    switch (numeroProduct){
                        case 0:
                            tope = lineas.size()-1;
                            break;
                        default:
                            tope = numeroProduct;
                    }
                    break;
                default:
                    tope = numeroIgnore;
            }

            for(int i = numeroTokens; i< tope; i++){
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
                //Metodo para revisar hasta donde debe de leer, si en dado caso existen las palabras reservadas
                //IGNORE O PRODUCTIONS
                int tope = 0;
                switch(numeroIgnore){
                    case 0:
                        switch (numeroProduct){
                            case 0:
                                tope = lineas.size()-1;
                                break;
                            default:
                                tope = numeroProduct;
                        }
                        break;
                    default:
                        tope = numeroIgnore;
                }

                for (int i = numeroTokens; i < tope; i++){
                    numeroDeLinea  = i ;
                    TokenDecl(lineas.get(i));


                }

                String lostokencitos = "";
                for(ArrayList<String> Ttokens: estructuraTokens){
                    ArrayList<String> tokens = deconvertidor(Ttokens);
                    for(String s: tokens){
                        if(!s.equals("=") && !s.equals("")){
                            lostokencitos = lostokencitos + s + ",";
                        }
                    }
                    lostokencitos = lostokencitos + "\n";
                }

                try {

                    PrintWriter writer = new PrintWriter("TokensEstructurados.txt");
                    writer.println(lostokencitos);
                    writer.close();

                } catch (Exception e) {

                    e.printStackTrace();

                }

            }
        }


        if(numeroProduct !=0){
            for(int i = numeroProduct +1; i < lineas.size()-1; i++){
                numeroDeLinea = i;
                ParserSpecification(lineas.get(i));
                System.out.println(i);
                System.out.println(lineas.get(i));


            }
        }



        limpiarEspacios(estructuraProducciones);
        transcribir(estructuraProducciones);
        System.out.println(estructuraProducciones);

        ArrayList<ArrayList<String>> estructuraNueva = SepararNoTerminales(estructuraProducciones);
        System.out.println(estructuraNueva);

        estructuraProducciones.clear();
        estructuraProducciones.addAll(estructuraNueva);

        destranscribir(estructuraProducciones);
        System.out.println(estructuraProducciones);


        QuitandoOR(estructuraProducciones);
        System.out.println(estructuraProducciones);

        System.out.println("Ingrese la Cadena que desea probar con FIRST y FOLLOW");
        Scanner scanner = new Scanner(System.in);
        String ausar = scanner.nextLine();
        System.out.println("FIRST: ");
        HashSet<String> resultante = first(ausar);
        System.out.println(resultante);







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

    public void QuitandoOR(ArrayList<ArrayList<String >> estructura){
        ArrayList<ArrayList<String>> remover = new ArrayList<>();
        ArrayList<ArrayList<String>> implementar = new ArrayList<>();
        for(ArrayList<String> s : estructura ){
            ArrayList<Integer> posiciones = new ArrayList<>();
            if(s.contains("|")){
                int numeroPos = 0;
                for(String string : s){
                    if(string.equals("|")){
                        posiciones.add(numeroPos);
                    }
                    numeroPos = numeroPos+1;
                }
                if(posiciones.size() > 0){
                    int inicio = 2;
                    int numeroDePos = 0;
                    int tope = posiciones.get(numeroDePos);
                    for(int x = 0; x < posiciones.size()+1; x++){
                        ArrayList<String> temporal = new ArrayList<>();

                        temporal.add(s.get(0));
                        temporal.add(s.get(1));

                        for(int i = inicio; i < tope; i++){
                            temporal.add(s.get(i));
                        }
                        if(!temporal.get(temporal.size()-1).equals(".")){
                            temporal.add(".");
                        }

                        implementar.add(temporal);

                        inicio = tope +1;
                        numeroDePos = numeroDePos +1;
                        if(tope == s.size()-1){
                            break;
                        }
                        else if(tope == posiciones.get(posiciones.size()-1)){
                            tope = s.size()-1;
                        }
                        else{
                            tope = posiciones.get(numeroDePos);
                        }


                    }
                }

               remover.add(s);

            }
        }
        for(ArrayList<String> I: implementar){
            estructuraProducciones.add(I);
        }
        for(ArrayList<String> R: remover){
            estructuraProducciones.remove(R);
        }

    }

    public void limpiarEspacios(ArrayList<ArrayList<String>> laestructura){
        ArrayList<String > vacio = new ArrayList<>();
        vacio.add("");
        for(ArrayList<String> s: laestructura){
            for (int i = 0; i < s.size(); i++){
                s.removeAll(vacio);
            }
        }
    }

    public void transcribir(ArrayList<ArrayList<String>> estructurado){
        int numeroPos = 0;
        for(ArrayList<String> produccion: estructurado){
            ArrayList<String> remplazo = deconvertidor(produccion);
            estructurado.set(numeroPos, remplazo);
            numeroPos  = numeroPos +1;
        }

    }

    public ArrayList<ArrayList<String>> SepararNoTerminales(ArrayList<ArrayList<String>> armazon){
        int numeroABC = 0;
        for(ArrayList<String> arr: armazon){
            if(arr.get(0).length() > 1){
                ArrayList<String> suplente = new ArrayList<>();

                String original = arr.get(0);
                String cambio = abecedario.get(numeroABC);
                suplente.add(original);
                suplente.add(cambio);
                laBanca.add(suplente);

                arr.set(0, cambio);
                for(ArrayList<String> arry: armazon){
                    for(int x = 0; x < arry.size(); x++){
                        String s = arry.get(x);
                        if(s.contains(original)){
                            String remplazo = s.replace(original, cambio);
                            arry.set(x, remplazo);
                        }
                    }
                }
            }
            numeroABC = numeroABC + 1;
        }
        ArrayList<ArrayList<String>> elRemplazo = new ArrayList<>();
        for(ArrayList<String> desarmando: armazon){
            ArrayList<String > pieza = new ArrayList<>();
            for(String s: desarmando){
                if(s.length() == 1){
                    pieza.add(s);
                }else{
                    String terminal = "";
                    for(int i = 0; i < s.length(); i++){
                        String elemento = s.substring(i, i+1);
                        boolean term = true;
                        for(ArrayList<String> revision: armazon){
                            if(elemento.equals(revision.get(0))){
                                if(!terminal.equals("")){
                                    pieza.add(terminal);
                                    terminal = "";
                                }
                                pieza.add(elemento);

                                term = false;
                                break;
                            }

                        }
                        if(term){
                            terminal = terminal + elemento;
                        }
                    }
                    if(!terminal.equals("")){
                        pieza.add(terminal);
                    }


                }
            }
            elRemplazo.add(pieza);
        }
        return elRemplazo;

    }

    //Regresa True si el string ingresado existe como un No-Terminal dentro de la gramatica
    public boolean chequear(String s){
        boolean respuesta = false;
        for(ArrayList<String> i: estructuraProducciones){
            if(i.get(0).equals(s)){
                respuesta = true;
                break;
            }
        }
        return respuesta;

    }

    public HashSet<String> encontrado(String conocido){
        HashSet<String> contenidoFirsteno = new HashSet<>();
        for(ArrayList<String> producciones: estructuraProducciones){
            if(producciones.get(0).equals(conocido)){

                String firstino = producciones.get(2);
                if(chequear(firstino)){
                    contenidoFirsteno.addAll(encontrado(firstino));
                }
                else{
                    contenidoFirsteno.add(firstino);
                }

            }
        }
        if(contenidoFirsteno.size() == 0){
            contenidoFirsteno.add(conocido);
        }
        return contenidoFirsteno;
    }

    public void destranscribir(ArrayList<ArrayList<String>> antes){
        for(ArrayList<String> produccion: antes){
            for(int i = 0; i< produccion.size(); i++){
                String designado = produccion.get(i);
                for(ArrayList<String> banca: laBanca){
                    if(designado.equals(banca.get(1))){
                        produccion.set(i, banca.get(0));
                    }
                }
            }
        }
    }

    public ArrayList<String> convertidor(ArrayList<String> lineas){
        ArrayList<String> resultado = new ArrayList<>();
        for(String S: lineas){
            String dot = S.substring(S.length() -1, S.length());
            String s;
            if(dot.equals(".")){
                s = S.substring(0, S.length()-1);
            }else{
                s = S;
            }
            String temporal = "";
            for(int i = 0; i < s.length(); i++){
                if(s.substring(i, i+1).equals("+")){
                    temporal = temporal + "┼";
                }else if(s.substring(i, i+1).equals(".")){
                    temporal = temporal + "Æ";
                }else if(s.substring(i, i+1).equals("(")){
                    temporal = temporal + "«";
                }else if(s.substring(i, i+1).equals(")")){
                    temporal = temporal + "»";
                }else if(s.substring(i,i+1).equals("#")){
                    temporal = temporal + "©";
                }else if(s.substring(i,i+1).equals("*")){
                    temporal = temporal + "×";
                }
                else{
                    temporal = temporal + s.substring(i, i+1);

                }
            }
            if(dot.equals(".")){
                temporal = temporal + ".";
            }

            resultado.add(temporal);
        }
        return resultado;
    }

    public ArrayList<String> deconvertidor(ArrayList<String> lineas){
        ArrayList<String> resultado = new ArrayList<>();
        for(String S: lineas){
            String s;
            String dot = "";
            if(S.length() > 1){
                dot = S.substring(S.length() -1, S.length());
                if(dot.equals(".")){
                    s = S.substring(0, S.length()-1);
                }else{
                    s = S;
                }
            }else{
                s = S;
            }

            String temporal = "";
            for(int i = 0; i < s.length(); i++){
                if(s.substring(i, i+1).equals("┼")){
                    temporal = temporal + "+";
                }else if(s.substring(i, i+1).equals("Æ")){
                    temporal = temporal + ".";
                }else if(s.substring(i, i+1).equals("«")){
                    temporal = temporal + "(";
                }else if(s.substring(i, i+1).equals("»")){
                    temporal = temporal + ")";
                }else if(s.substring(i,i+1).equals("©")){
                    temporal = temporal + "#";
                }else if(s.substring(i,i+1).equals("×")){
                    temporal = temporal + "*";
                }
                else{
                    temporal = temporal + s.substring(i, i+1);

                }
            }
            if(dot.equals(".")){
                temporal = temporal + ".";
            }

            resultado.add(temporal);
        }
        return resultado;
    }

    public int getCantidadKeywords(){
        return cantidadKeywords;
    }

    public HashSet<String> first(String peticion){
        HashSet<String> resultado = new HashSet<>();
        String item = "";
        for(int i = 0; i < peticion.length(); i++){
            String parte = peticion.substring(i, i+1);
            if(!parte.equals(" ")){
                item = item + parte;
            }
            else{
                peticiones.add(item);
                item = "";
            }
        }
        peticiones.add(item);
        boolean pruebaDeFuego = true;
        while(pruebaDeFuego){
            if(!chequear(peticiones.get(numeroDePeticion))){
                resultado.add(peticiones.get(numeroDePeticion));
            }
            else{
                resultado.addAll(encontrado(peticiones.get(numeroDePeticion)));
            }


            if(!resultado.contains("#") || numeroDePeticion > peticiones.size()-1){
                pruebaDeFuego = false;
            }

            if(resultado.contains("#")){
                resultado.remove("#");
            }
            numeroDePeticion = numeroDePeticion +1;
        }



        return resultado;
    }


}
