import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class RNA_AG {
    private static final int X1_MASK = 0b1111110000000;
    private static final int X2_MASK = 0b1111111;
    private static final int P1_MASK = 0b1010101011010;
    private static final int P2_MASK = 0b0101010100101;
    
    private static final String relativePath = "./poblacion/";
    private static final String ext = ".pobl";
    private static final int crossValidation = 10;
    private static final int pobl = 20;
    
    /**
     * Carga las instancias del documento .arff
     * @param path
     * @return
     * @throws Exception 
     */
    private static Instances cargarInstancias(String path) throws Exception {
        Instances data = ConverterUtils.DataSource.read(path);
        if (data.classIndex() == -1) 
            data.setClassIndex(data.numAttributes()-1);
        return data;
    }
    
    private static void guardarEvaluacion(Evaluation evaluation, RNA rna, int poblN, int gen) throws IOException, Exception{
        //Guarda la información de la evaluacion de manera legible
        File file = new File(relativePath + "resultados" + poblN + "_gen" + gen + ".txt");
        FileWriter fw = new FileWriter(file, true);
        PrintWriter w = new PrintWriter(new BufferedWriter(fw));
        
        if (!file.exists()){
            w.println("||                          GENERACION " + gen + "                           ||");
            w.println("----------------------------------------------------------------------");
        }
        w.println("||                           Resultado                              ||");
        w.println("----------------------------------------------------------------------");
        w.print("|| RNA ||");
        w.print(" Neuronas: " + rna.getNeuronas());
        w.print(" Capas: " + rna.getCapas());
        w.print(" Épocas: " + rna.getEpocas());
        w.print(" Learning Rate: " + rna.getLearningRate());
        w.print(" Momentum: " + rna.getMomentum());
        w.println(evaluation.toSummaryString());
        w.println(evaluation.toMatrixString("Matriz de confusión"));
        w.println("----------------------------------------------------------------------");
        w.close();
    }
    
    /**
     * Evalua todos los experimentos que no han sido evaluados o que tiene -1
     * en su atributo resultado
     * @param poblacion
     * @throws Exception 
     */
    private static void evaluarPoblacion(ArrayList<RNA> poblacion, int poblN, int gen) throws Exception {
        Instances instances = cargarInstancias("./10x10Forest.arff");
        Evaluation evaluation = new Evaluation(instances);
        MultilayerPerceptron mlp = new MultilayerPerceptron();
        
        //Configuracion de los parametros de la RNA
        for (RNA ind : poblacion){
            //Si no se ha realizado el experimento lo realiza
            if (ind.getResultado() == -1) {
                StringBuilder hl = new StringBuilder().append(ind.getNeuronas());
                for (int i = 1, c = ind.getCapas(), n = ind.getNeuronas() ; i < c ; i++)
                    hl.append(",").append(n);

                mlp.setHiddenLayers(hl.toString());
                mlp.setTrainingTime(ind.getEpocas());
                mlp.setLearningRate(ind.getLearningRate());
                mlp.setMomentum(ind.getMomentum());

                //Evaluacion del experimento
                evaluation.crossValidateModel(mlp, instances, crossValidation, new Random(1));

                //Modificar el resultado de la RNA dado en el experimento
                ind.setResultado(evaluation.pctCorrect());

                guardarEvaluacion(evaluation, ind, poblN, gen);
            }
        }
    }
    
    /**
     *
     * @return retorna un poblacion creada aleatoriamente
     */
    public static ArrayList<RNA> generarPoblacion() {
        ArrayList<RNA> poblacion = new ArrayList<>();
        Random ram = new Random(System.currentTimeMillis());
        
        while (poblacion.size() < pobl){
            RNA rna = new RNA(ram.nextInt(RNA.RNA_MASK + 1));
            if (!poblacion.contains(rna))
                poblacion.add(rna);
        }
        return poblacion;
    }
    
    /**
     * Ordena ArrayList<RNA> de mayor a menor
     */
    private static void sortRNAs(ArrayList<RNA> rnas){
        Collections.sort(rnas, Collections.reverseOrder());
    }
    
    //Carga la poblacion de un archivo
    private static ArrayList<RNA> cargarPoblacion(String filename) throws FileNotFoundException, IOException, NumberFormatException{
        ArrayList<RNA> poblacion = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(relativePath + filename + ext));
            String l;
            while ((l = br.readLine()) != null){
                if (!l.isBlank()){
                    String[] rna = l.split(",");
                    poblacion.add(new RNA(Integer.parseInt(rna[0]), Double.parseDouble(rna[1])));
                }
            }
        }finally{
            if (br != null)
                br.close();
        }
        return poblacion;
    }
    
    //Guarda la poblacion en un archivo
    private static void guardarPoblacion(String filename, ArrayList<RNA> poblacion) throws IOException{
        if (poblacion != null && poblacion.size() > 0){
            //Se ordenan de mayor a menor
            sortRNAs(poblacion);
            PrintWriter pw = null;
            try{
                pw = new PrintWriter(new FileWriter(relativePath + filename + ext));
                for (RNA ind : poblacion) {
                    pw.println(ind.getRNA() + "," + ind.getResultado());
                }
            }finally{
                if (pw != null)
                    pw.close();
            }
        }
    }
    
    //Guarda la poblacion en dos archivo
    private static void guardarPoblacion(String filename, ArrayList<RNA> poblacion, int gen) throws IOException{
        guardarPoblacion(filename.concat("1_gen" + gen), new ArrayList<>(poblacion.subList(0, poblacion.size()/2)));
        guardarPoblacion(filename.concat("2_gen" + gen), new ArrayList<>(poblacion.subList(poblacion.size()/2, poblacion.size())));
    }
    
    /**Genera descendencia
     * Solo se agregaran a la descendencia si no existen en la poblacion actual
     * ni en la descendencia ya generada
     * @param rnas
     * @return idealmente crea una nueva poblacin de <pobl>
     */
    public static ArrayList<RNA> generarDescendencia(ArrayList<RNA> rnas){
        ArrayList<RNA> desc = new ArrayList<>();
        //Se genera del 25% de la poblacion actual
        for (int i = 0; i < pobl/4 ; i++){ 
            ArrayList<RNA> descPulso = tipoPulso(rnas.get(i), rnas.get(pobl/2 + i));
            if (!rnas.contains(descPulso.get(0)) && !desc.contains(descPulso.get(0)))
                desc.add(descPulso.get(0));
            if (!rnas.contains(descPulso.get(1)) && !desc.contains(descPulso.get(1)))
                desc.add(descPulso.get(1));
            
            ArrayList<RNA> descX = tipoX(rnas.get(i), rnas.get(pobl/2 + i));
            if (!rnas.contains(descX.get(0)) && !desc.contains(descX.get(0)))
                desc.add(descX.get(0));
            if (!rnas.contains(descX.get(1)) && !desc.contains(descX.get(1)))
                desc.add(descX.get(1));
        }
       return desc; 
    }
    
    /**Genera un desceniente resultado de la mutacion del parametro rna
     * @param rna RNA a la cual se le va a aplicar la mutacion
     * @return retorna la RNA ya mutada
     */
    public static RNA generarMutacion(RNA rna, int changes){
        RNA mut = new RNA(rna.getRNA());
        Random ram = new Random(System.currentTimeMillis());
        for (int i = 1, num ; i < changes ; i++){
            num = ram.nextInt(100) + 1; //1-100
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
    
    /**Genera dos hijos utilizando la estrategia de cruce Pulso
     * Solo se agregaran a la descendencia si no existen en la poblacion actual
     */
    private static ArrayList<RNA> tipoPulso(RNA rna1, RNA rna2){
        ArrayList<RNA> descPulso = new ArrayList<>();
        descPulso.add(new RNA((rna1.getRNA() & P1_MASK) + (rna2.getRNA() & P2_MASK)));
        descPulso.add(new RNA((rna1.getRNA() & P2_MASK) + (rna2.getRNA() & P1_MASK)));        
        return descPulso;
    }
    
    /**Genera dos hijos utilizando la estrategia de cruce X
     * Solo se agregaran a la descendencia si no existen en la poblacion actual
     */
    private static ArrayList<RNA> tipoX(RNA rna1, RNA rna2){
        ArrayList<RNA> descX = new ArrayList<>();
        descX.add(new RNA((rna1.getRNA() & X1_MASK) + (rna2.getRNA() & X2_MASK)));
        descX.add(new RNA((rna1.getRNA() & X2_MASK) + (rna2.getRNA() & X1_MASK)));
        return descX;
    }
    
    /**
     * 
     * ALGORITMO GENETICO
     * 
     */
    
    /**
     * Este metodo solo debe ser llamado al inicio y una unica vez durante todo el proceso de entrenamiento
     * @param filename nombre del archivo a generar
     */
    public static void primerPoblacion(String filename) throws Exception{
        ArrayList<RNA> p  = generarPoblacion();
        guardarPoblacion(filename, p, 1); //De esta forma se guardan dos archivos diferentes de la 1er generacion
    }
    
    /**
     * Este metodo debe ser ejecutado cuando se tenga al menos una  
     * RNA en el archivo que no haya sido evaluada en la poblacion. 
     * @param filename archivo donde se encuentra la poblacion
     * @param poblN numero del archivo de la poblacion
     * @param gen generacion de la poblacion
     * @throws Exception 
     */
    public static void evaluarPoblacion(String filename, int poblN, int gen) throws Exception{
        //Cargar poblacion
        ArrayList<RNA> p = cargarPoblacion(filename + poblN + "_gen" + gen);
        if (!p.isEmpty()){
            //Evaluar poblacion
            evaluarPoblacion(p, poblN, gen);
            //Guardar poblacion
            guardarPoblacion(filename, p);
        }
    }
    
    /**
     * Este metodo es el encargado de unir dos poblaciones descendientes
     * y unirlas al alchivo que contiene todas las RNAs
     * 
     * Las poblaciones descendientes son agregadas aunque estas ya se encuntren la poblacion
     * 
     * @param filename archivo que contiene TODAS las generaciones de rnas
     * @param gen generacion de descedientes que se debe de unir a la poblacion
     * @throws NumberFormatException
     * @throws IOException 
     */
    public static void unirPoblaciones(String filename, int gen) throws NumberFormatException, IOException{
        ArrayList<RNA> p = new ArrayList<>();
        try{
            p.addAll(cargarPoblacion(filename));
        }catch(FileNotFoundException ex){}
        ArrayList<RNA> p1 = cargarPoblacion(filename.concat("1_gen" + gen));
        ArrayList<RNA> p2 = cargarPoblacion(filename.concat("2_gen" + gen));
        p.addAll(p1); p.addAll(p2);

        guardarPoblacion(filename, p);
    }
  
    /**
     * Este metodo es el encargado de crear la descendencia de <pobl> rnas de una poblacion
     * Si no se logran crear <pobl> descendientes, se crearan los restantes (si es posible) apartir de mutaciones
     * 
     * 
     * @param filename archivo que contiene TODAS las generaciones de rnas
     * @param gen generacion que va a ser creada de poblacion actual
     * @throws NumberFormatException
     * @throws IOException 
     */
    public static void Alg_Gen(String filename, int gen) throws NumberFormatException, IOException{
        //Carga TODA la poblacion que ha sido creada a traves de generaciones 
        ArrayList<RNA> rnas = cargarPoblacion(filename);
        Random ram = new Random(System.currentTimeMillis());
        
        //Genera descendencia
        ArrayList<RNA> desc = generarDescendencia(rnas);
        
        //Genera mutaciones hasta que haya <pobl> hijos o hasta llegar al limite de intentos (5)
        for (int i = 0 ; (i < 5 && desc.size() < pobl) ; i++){
            for (int j = 0, size = desc.size() ; j < (pobl - size) ; j++){
                RNA rna = generarMutacion(rnas.get(ram.nextInt(5)), 3); //Se toma aleatoriamente 1 rna de las 5 mejores
                if (!rnas.contains(rna) && !desc.contains(rna))
                    desc.add(rna);
            }
        }
        //Guarda la descendencia para ser evaluada con evaluarPoblacion()
        guardarPoblacion(filename, desc, gen);
    }
    
    public static void main(String[] args) throws Exception {
        
    }
}
