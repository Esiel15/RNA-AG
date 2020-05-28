import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class AG {
    private static final int X1_MASK = 0b1111110000000;
    private static final int X2_MASK = 0b1111111;
    private static final int P1_MASK = 0b1010101011010;
    private static final int P2_MASK = 0b0101010100101;
    
    private ArrayList<RNA> rnas;
    
    public AG(ArrayList<RNA> rnas) {
        this.rnas = rnas;
    }

    public ArrayList<RNA> getRNAs() {
        return rnas;
    }

    public void setRNAs(ArrayList<RNA> rnas) {
        this.rnas = rnas;
    }

    /**Genera descende
     * Solo se agregaran a la descendencia si no existen en la poblacion actual
     */
    public ArrayList<RNA> generarDescendencia(){
        ArrayList<RNA> desc = new ArrayList<>();
        
        for (int i = 0; i < 5 ; i++){
            desc.addAll(tipoPulso(rnas.get(i), rnas.get(9 + i)));
            desc.addAll(tipoX(rnas.get(0), rnas.get(10 + i)));
        }
       return desc; 
    }
    
    /**Genera dos hijos utilizando la estrategia de cruce Pulso
     * Solo se agregaran a la descendencia si no existen en la poblacion actual
     */
    private ArrayList<RNA> tipoPulso(RNA rna1, RNA rna2){
        ArrayList<RNA> descPulso = new ArrayList<>();
        RNA h1 = new RNA((rna1.getRNA() & P1_MASK) + (rna2.getRNA() & P2_MASK));
        RNA h2 = new RNA((rna1.getRNA() & P2_MASK) + (rna2.getRNA() & P1_MASK));
        
        if (!rnas.contains(h1)) descPulso.add(h1);
        if (!rnas.contains(h2)) descPulso.add(h2);
        
        return descPulso;
    }
    
    /**Genera dos hijos utilizando la estrategia de cruce X
     * Solo se agregaran a la descendencia si no existen en la poblacion actual
     */
    private ArrayList<RNA> tipoX(RNA rna1, RNA rna2){
        ArrayList<RNA> descX = new ArrayList<>();
        RNA h1 = new RNA((rna1.getRNA() & X1_MASK) + (rna2.getRNA() & X2_MASK));
        RNA h2 = new RNA((rna1.getRNA() & X2_MASK) + (rna2.getRNA() & X1_MASK));
        
        if (!rnas.contains(h1)) descX.add(h1);
        if (!rnas.contains(h2)) descX.add(h2);
        
        return descX;
    }
    
    /**Genera un hijo resultado de la mutacion del parametro rna
     * El hijo puede ya existir en la poblacion
     * @param rna RNA a la cual se le va a aplicar la mutacion
     * @return retorna la RNA ya mutada
     */
    public RNA generarMutacion(RNA rna){
        RNA mut = new RNA(rna.getRNA());
        Random ram = new Random(System.currentTimeMillis());
        for (int i = 1, num ; i < 3 ; i++){
            num = ram.nextInt(100) + 1;
            if (num <= 5){ //Capas 5%
                switch (ram.nextInt(2) + 1){
                    case 1 : mut.setRNA(mut.getRNA() ^ 0b100000000);
                        break;
                    case 2 : mut.setRNA(mut.getRNA() ^ 0b10000000);
                        break;
                }
            }else if (num <= 15){ //Neuronas 10%
                switch (ram.nextInt(4) + 1){
                    case 1 : mut.setRNA(mut.getRNA() ^ 0b1000000000000);
                        break;
                    case 2 : mut.setRNA(mut.getRNA() ^ 0b100000000000);
                        break;
                    case 3 : mut.setRNA(mut.getRNA() ^ 0b10000000000);
                        break;
                    case 4 : mut.setRNA(mut.getRNA() ^ 0b10000000000);
                        break;
                }
            }else if (num <= 30){ //Epocas 15%
                switch (ram.nextInt(3) + 1){
                    case 1 : mut.setRNA(mut.getRNA() ^ 0b1000000);
                        break;
                    case 2 : mut.setRNA(mut.getRNA() ^ 0b100000);
                        break;
                    case 3 : mut.setRNA(mut.getRNA() ^ 0b10000);
                        break;
                }
            }else if (num <= 60){ //Momentum 30%
                switch (ram.nextInt(2) + 1){
                    case 1 : mut.setRNA(mut.getRNA() ^ 0b10);
                        break;
                    case 2 : mut.setRNA(mut.getRNA() ^ 0b1);
                        break;
                }
            }else{ //Learning Rate 40%
                switch (ram.nextInt(2) + 1){
                    case 1 : mut.setRNA(mut.getRNA() ^ 0b1000);
                        break;
                    case 2 : mut.setRNA(mut.getRNA() ^ 0b100);
                        break;
                }
            }
        }
        return mut; 
    }
    
    
    public void sortRNAs(){
        Collections.sort(rnas, Collections.reverseOrder());
    }
    
    public static ArrayList<RNA> generarPoblacion() {
        ArrayList<RNA> poblacion = new ArrayList<>();
        Random ram = new Random(System.currentTimeMillis());
        
        while (poblacion.size() < 20){
            RNA rna = new RNA(ram.nextInt(RNA.RNA_MASK + 1));
            if (!poblacion.contains(rna))
                poblacion.add(rna);
        }
        return poblacion;
    }

}
