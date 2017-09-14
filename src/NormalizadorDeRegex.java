/**
 * Created by Diego Castañeda on 28/07/2017.
 */
public class NormalizadorDeRegex {
    private String normalizado = "" ;
    private String kleeneado = "";
    private int contador = 0;

    public NormalizadorDeRegex(){}

    /*Funcion que serviria para el caso de +, pero realmente existen muchos casos problematicos
    * posibles, por ello no se normalizara el + y mejor solo se creara una operacion para crear
    * un automata si se encontrara una + */
    public String yStringKleeneado(String regex){
        kleeneado = "";
        contador = 0;
        int n = 1;

        String y = regex.substring(n-1, n);
        if (y.equals(")")){
            contador =+ 1;
        }
        kleeneado = y + kleeneado;
        for (int z = 0; z != contador; n = n -1){

            y = regex.substring(n-1, n);
            if (y.equals(")")){
                contador =+ 1;
            }
            else if(y.equals("(")){
                contador =- 1;
            }

            kleeneado = y + kleeneado;
        }
        return kleeneado;
    }

    public String PostFixYNormalizar(String regex){
        String reg = RegExConverter.infixToPostfix(regex);
        int j = reg.length();
        for(int i = 0; i < j; i++){
            String x = reg.substring(i, i+1);
            if(x.equals("?")){
                normalizado = normalizado + "@|";
            }

            else {
                normalizado = normalizado + x;
            }
        }
        return normalizado;
    }
}
