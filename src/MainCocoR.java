import com.sun.corba.se.impl.ior.IdentifiableFactoryFinderBase;
import com.sun.corba.se.impl.oa.poa.ActiveObjectMap;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

import java.io.*;
import java.util.*;

/*Este programa funciona siempre y cuando
* 1. Si se quiere realizar el basicSet, el cual se añade con el termino de Char [.. Char], se debde de utilizar -- en lugar de ..
* 2. Para sumar basicSets dentro de CHARACTERS, se debe de usar el signo ~ o bien Alt+126 para *//**
 * Created by DiegoCastaneda on 01/09/2017.
 */
public class MainCocoR {
    public static void main (String args[]){
        StringTokenizer tokens;
        String linea;
        ArrayList<String> contenido = new ArrayList<>();
        LectordeArchivos lector = new LectordeArchivos();

        Scanner escaner = new Scanner(System.in);
        System.out.println("Ingrese el archivo con las especificaciones del lexer: ");
        String archivaldo = escaner.nextLine();


        //Arraylist que contiene las lineas no vacias del documento con las especificaciones de Cocol/R
        ArrayList<String> lineasSinConversionInicial = lector.crearLector(archivaldo);
        ArrayList<String> lineas = lector.convertidor(lineasSinConversionInicial);

        //Creando el creador de tokens con las lineas del documento de especificaciones de Cocol/R
        CreadorTokens creador = new CreadorTokens(lineas);


        //Verificar si tiene algun error sintactico el documento. Si no tuviera ninguno, se pasa a la parte de creacion
        // del lexer.
        if(!lector.chequearSintaxis(lineas)){
            System.out.println("Malfunction of .txt, Found several Syntax errors");
        }
        else{
            System.out.println("Archivo Sintacticamente Correcto");


            creador.CreateNewJavaFile(lineas);
            creador.CreateNewTokenFile();
        }

        try {

            PrintWriter writer = new PrintWriter("NumeroKeywords.txt");
            writer.println(lector.getCantidadKeywords());
            writer.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        try (BufferedReader lectorTokens = new BufferedReader(new FileReader("tokens.txt"))){
            while(!(lectorTokens.readLine() == null)){

            }
        }
        catch (Exception e){

        }

        ArrayList<ArrayList<String>> conocidos = new ArrayList<>();
        ArrayList<ArrayList<String>> EstructurasParaTokens = new ArrayList<>();
        LectordeArchivos investigador = new LectordeArchivos();
        ArrayList<ArrayList<NodosRamas>> automatas = new ArrayList<>();
        ArrayList<ArrayList<String>> keywords = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el nombre del archivo con el código que desea lexear: ");
        String archivo = scanner.nextLine();
        ArrayList<String> lineasAImprimir = new ArrayList();
        try (BufferedReader lectorArchivo = new BufferedReader(new FileReader(archivo))){
            try (BufferedReader lectorTokens = new BufferedReader(new FileReader("tokens.txt"))){
                lectorTokens.mark(1000);
                String lineaArchivo = lectorArchivo.readLine();
                while(lineaArchivo != null){
                    StringTokenizer st = new StringTokenizer(lineaArchivo);
                    while(st.hasMoreTokens()){
                        String componente = st.nextToken();
                        String lineaToken = lectorTokens.readLine();
                        boolean existencia = false;
                        while(lineaToken != null && !lineaToken.equals("")){
                            int indexComa = lineaToken.indexOf(",");
                            String s = lineaToken.substring(0,indexComa);
                            if(s.equals(componente)){
                                lineasAImprimir.add(lineaToken);
                                lectorTokens.reset();
                                lectorTokens.mark(1000);
                                existencia = true;
                                break;
                            }
                            lineaToken = lectorTokens.readLine();
                        }
                        if(!existencia){
                            for(int x = 0; x<componente.length(); x++){
                                lectorTokens.reset();
                                lectorTokens.mark(1000);
                                String particion = componente.substring(x, x+1);
                                lineaToken = lectorTokens.readLine();
                                while(lineaToken != null){
                                    int indexComa = lineaToken.indexOf(",");
                                    String s = lineaToken.substring(0,indexComa);

                                    if(s.equals(particion)){
                                        lineasAImprimir.add(lineaToken);
                                        lectorTokens.reset();
                                        lectorTokens.mark(1000);
                                        break;
                                    }
                                    lineaToken = lectorTokens.readLine();
                                }
                            }
                        }                    }
                    lineaArchivo = lectorArchivo.readLine();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(lineasAImprimir);


        try(BufferedReader CreadorDeRegex = new BufferedReader(new FileReader("tokens.txt"))){
            CreadorDeRegex.mark(1000);
            String lectura = CreadorDeRegex.readLine();
            ArrayList<String> identificadores = new ArrayList<>();
            while(lectura != null){
                int indexComa = lectura.indexOf(",");
                if(!identificadores.contains(lectura.substring(indexComa +1, lectura.length()))){
                    identificadores.add(lectura.substring(indexComa +1, lectura.length()));
                }
                lectura = CreadorDeRegex.readLine();

            }
            for(String s: identificadores){
                if(!s.equals("")){
                    ArrayList<String> AL = new ArrayList<>();
                    AL.add(s);
                    conocidos.add(AL);
                }

            }
            CreadorDeRegex.reset();
            CreadorDeRegex.mark(1000);
            lectura = CreadorDeRegex.readLine();
            while(lectura != null){
                if (!lectura.equals("")){
                    int indexComa = lectura.indexOf(",");
                    String palabra = lectura.substring(indexComa +1, lectura.length());
                    for(ArrayList<String> procesados: conocidos){
                        if(procesados.get(0).equals(palabra)){
                            if(procesados.size() == 1){
                                String agregar = lectura.substring(0, indexComa);
                                procesados.add(agregar);
                            }
                            else{
                                String agregar = lectura.substring(0, indexComa);
                                String hay = procesados.get(1) + "|" + agregar;
                                procesados.set(1,hay);
                            }
                        }
                    }
                }
                lectura = CreadorDeRegex.readLine();
            }



            CreadorDeRegex.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        try(BufferedReader CreadorDeEstructuras = new BufferedReader(new FileReader("TokensEstructurados.txt"))){
            CreadorDeEstructuras.mark(1000);
            String lectura = CreadorDeEstructuras.readLine();
            while (lectura != null){
                ArrayList<String> nuevo = new ArrayList<>();
                String expresionRegular = "";
                List<String> listado = Arrays.asList(lectura.split(","));
                nuevo.add(listado.get(0));

                for(int i = 1; i < listado.size(); i++ ){
                    String identification = listado.get(i);
                    boolean verificacion = false;
                    for(ArrayList<String> S: conocidos){
                        if(identification.equals(S.get(0))){
                            expresionRegular = expresionRegular + "("  + S.get(1) + ")";
                            verificacion = true;
                            break;
                        }
                        else if(identification.equals("{")){
                            expresionRegular = expresionRegular + "(";
                            verificacion = true;
                            break;

                        }else if(identification.equals("}")){
                            expresionRegular = expresionRegular + ")*";
                            verificacion = true;
                            break;

                        }else if(identification.equals("[")){
                            expresionRegular = expresionRegular + "(" ;
                            verificacion = true;
                            break;

                        }else if(identification.equals("]")){
                            expresionRegular = expresionRegular + ")";
                            verificacion = true;
                            break;

                        }

                    }
                    if(!verificacion){
                        expresionRegular = expresionRegular + identification;
                    }
                }
                nuevo.add(expresionRegular);
                EstructurasParaTokens.add(nuevo);
                lectura = CreadorDeEstructuras.readLine();
            }
            EstructurasParaTokens.remove(EstructurasParaTokens.size()-1);



            CreadorDeEstructuras.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }


        try(BufferedReader numeroDeKeyWords = new BufferedReader(new FileReader("NumeroKeywords.txt"))){
            numeroDeKeyWords.mark(1000);
            int numero = Integer.parseInt(numeroDeKeyWords.readLine());
            ArrayList<ArrayList<String>> temporalisimo = new ArrayList<>();
            for(int i = conocidos.size() - numero; i < conocidos.size(); i++ ){
                temporalisimo.add(conocidos.get(i));
            }
            System.out.println(temporalisimo);
            keywords = temporalisimo;

            System.out.println(EstructurasParaTokens);

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        for(ArrayList<String> s: EstructurasParaTokens){
            Arbol arbolString = new Arbol();
            if(!s.get(1).equals("")){
                String sub = s.get(1).substring(0, s.get(1).length()-1);
                ArrayList<NodosRamas> string = arbolString.CrearElAFDDirecto(sub);
                int numeroParaElID = 0;

                for(NodosRamas nodoRama: string){
                    nodoRama.setId(numeroParaElID);
                    numeroParaElID = numeroParaElID + 1;
                }
                automatas.add(string);
            }


        }


        try(BufferedReader CreadorDeTokensFinales = new BufferedReader(new FileReader("Ingreso.txt"))){
            CreadorDeTokensFinales.mark(1000);
            String lineal = CreadorDeTokensFinales.readLine();
            while (lineal != null){
                StringTokenizer st = new StringTokenizer(lineal);
                while(st.hasMoreTokens()){
                    String siguiente = st.nextToken();
                    System.out.println(siguiente);
                    boolean encontrado = false;
                    int indice = 0;
                    for(ArrayList<NodosRamas> automaton: automatas){
                        for(ArrayList<String> s: keywords){
                            if(siguiente.equals(s.get(0))){
                                encontrado = true;
                                System.out.println("<" + s.get(1) + ">");
                                break;

                            }
                            if(encontrado){
                                break;
                            }

                        }

                        if(investigador.simularAFD(automaton, siguiente)){
                            encontrado = true;
                            System.out.println("<" + EstructurasParaTokens.get(indice).get(0) + ">");
                            break;

                        }
                        indice = indice +1;
                        if(encontrado){
                            break;
                        }
                    }
                    if(!encontrado){
                        System.out.println("El token " + siguiente + " no se pudo identificar como Token");
                    }

                }



                lineal = CreadorDeTokensFinales.readLine();
            }


        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }





    }

    }



