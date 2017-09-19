import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.io.FileReader;
import java.util.StringTokenizer;

public class Lexer{
    public static void main (String args[]){
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
