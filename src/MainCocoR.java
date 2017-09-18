import com.sun.corba.se.impl.ior.IdentifiableFactoryFinderBase;
import com.sun.corba.se.impl.oa.poa.ActiveObjectMap;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;

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
        ArrayList<String> lineas = lector.crearLector(archivaldo);

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

        try (BufferedReader lectorTokens = new BufferedReader(new FileReader("tokens.txt"))){
            while(!(lectorTokens.readLine() == null)){

            }
        }
        catch (Exception e){

        }

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


        try(BufferedReader CreadorDeRegex = new BufferedReader(new FileReader("tokens.txt"))){
            ArrayList<ArrayList<String>> conocidos = new ArrayList<>();
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
            System.out.println(conocidos);
            /*Conocidos = un ArrayList que */


            CreadorDeRegex.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(lineasAImprimir);



    }

    }



