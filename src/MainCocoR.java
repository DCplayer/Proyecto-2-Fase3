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
        System.out.println(lineasAImprimir);



    }

    }



