public class RNA implements Comparable<RNA>{
    public static final int N_MASK = 0b1111000000000;
    public static final int C_MASK = 0b0000110000000;
    public static final int E_MASK = 0b0000001110000;
    public static final int LR_MASK = 0b0000000001100;
    public static final int M_MASK = 0b0000000000011;
    private int rna;
    private double r;

    public RNA(int rna){
        this.rna = rna;
        r = 0;
    }
    
    public int getRNA() {
        return rna;
    }

    public void setRNA(int rna) {
        this.rna = rna;
    }

    public double getResultado() {
        return r;
    }

    public void setResultado(double r) {
        this.r = r;
    }
    
    public int getNeuronas(){
        return rna & N_MASK;
    }
    
    public int getCapas(){
        return rna & C_MASK;
    }
    
    public int getEpocas(){
        return rna & E_MASK;
    }
    
    public int getLearningRate(){
        return rna & LR_MASK;
    }
    
    public int getMomentum(){
        return rna & M_MASK;
    }
    
    @Override
    public int compareTo(RNA o) {
        return Double.compare(r, o.getResultado());
    }
    
    
    
}