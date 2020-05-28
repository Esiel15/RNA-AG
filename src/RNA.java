public class RNA implements Comparable<RNA>{
    public static final int N_MASK = 0b1111000000000;
    public static final int C_MASK = 0b110000000;
    public static final int E_MASK = 0b1110000;
    public static final int LR_MASK = 0b1100;
    public static final int M_MASK = 0b11;
    
    public static final int RNA_MASK = 0b1111111111111;
    
    private int rna;
    private double r;

    public RNA(int rna){
        this.rna = rna & RNA_MASK;
        r = -1;
    }
    
    public RNA(int rna, double resultado){
        this.rna = rna & RNA_MASK;
        r = resultado;
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
        return ((rna & N_MASK) >> 9) + 3;
    }
    
    public void setNeuronas(int neuronas){
        if (neuronas >= 3 && neuronas <= 18){
            rna &= RNA_MASK - N_MASK; //Se eliminan las antiguas neuronas
            rna += (neuronas - 3) << 9; //Se colocan las nuevas neuronas
        }
    }

    public int getCapas(){
        return ((rna & C_MASK) >> 7) + 1;
    }
    
    public void setCapas(int capas){
        if (capas >= 1 && capas <= 4){
            rna &= RNA_MASK - C_MASK; //Se eliminan las antiguas capas
            rna += (capas - 1) << 7; ////Se colocan las nuevas capas
        }
    }
    
    public int getEpocas(){
        return ((rna & E_MASK) >> 4) * 250 + 500;
    }
    
    /**El número del numero de épocas sera el 500 + (250 * num)
     * @param num tiene que estar en el rango de 0 - 7*/
    public void setEpocas(int num){
        if (num >= 0 && num <= 7){
            rna &= RNA_MASK - E_MASK; //Se eliminan las antiguas epocas
            rna += num << 4; ////Se colocan las nuevas epocas
        }
    }
    
   
    public double getLearningRate(){
        return ((rna & LR_MASK) >> 2) * 0.5 + 2.0;
    }
    
    /**El valor del Learning Rate sera 2.0 + (0.5 * num)
     * @param num tiene que estar en el rango de 0 - 3*/
    public void setLearningRate(int num){
        if (num >= 0 && num <= 3){
            rna &= RNA_MASK - LR_MASK; //Se elimina el antiguo learning rate
            rna += num << 2; ////Se coloca el nuevo learning rate
        }
    }

    public double getMomentum(){
        return (rna & M_MASK) * 0.5 + 2.0;
    }
    
    /**El valor del Momentum sera 2.0 + (0.5 * num)
     * @param num tiene que estar en el rango de 0 - 3*/
    public void setMomentum(int num){
        if (num >= 0 && num <= 3){
            rna &= RNA_MASK - M_MASK; //Se elimina el antigua momentum
            rna += num; ////Se coloca el nuevo momentum
        }
    }
    
    @Override
    public int compareTo(RNA o) {
        return Double.compare(r, o.getResultado());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.rna;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final RNA other = (RNA) obj;
        if (this.rna != other.rna) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString(){
        return "Neuronas: " + this.getNeuronas() + " Capas: " + this.getCapas() + " Épocas: " + this.getEpocas() 
                + " Learning Rate: " + this.getLearningRate() + " Momentum: " + this.getMomentum();
    }
}
