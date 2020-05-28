import java.io.BufferedReader;
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
    private static final String relativePath = "./poblacion";
    private static final String ext = ".pobl";
    private static final int crossValidation = 10;
    public static final int pobl = 20;
    
    private Instances instances;
    private Evaluation evaluation;
    
    public RNA_AG(Instances instances) throws Exception{
        this.instances = instances;
        evaluation = new Evaluation(instances);
    }

    public void evaluarPoblacion(ArrayList<RNA> poblacion) throws Exception{
        MultilayerPerceptron mlp = new MultilayerPerceptron();
        
        //Configuracion de los parametros de la RNA
        for (RNA ind : poblacion){
            
            //Si se ha realizado el experimento
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
                System.out.println("TERMINO");

                //Modificar el resultado de la RNA dado en el experimento
                ind.setResultado(evaluation.pctCorrect());

                /*NO SE GUARDA NINGUN OTRA COSA AUN*/
            }
        }
    }
    
    /**
     * Ordena rnas de mayor a menor
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
    
    public static Instances cargarInstancias(String path) throws Exception {
        //Carga las instancias al programa
        Instances data = ConverterUtils.DataSource.read(path);
        if (data.classIndex() == -1) 
            data.setClassIndex(data.numAttributes()-1);
        
        return data;
    }
    
    /**
     * Este metodo solo debe ser llamado al inicio y una unica vez durante todo el proceso de entrenamiento
     */
    public static void primerPoblacion(String filename) throws Exception{
        ArrayList<RNA> p  = AG.generarPoblacion();
        guardarPoblacion(filename, p, 1); //De esta forma se guardan dos archivos diferentes de la 1er generacion
    }
    
    /**
     * Este metodo debe ser ejecutado cuando se tenga al menos una  
     * RNA en el archivo que no haya sido evaluada en la poblacion. 
     * @param instPath ruta donde se encuentra el archivo .arff
     * @param poblFilename ruta donde se encuentra la poblacion a evaluar
     * @throws Exception 
     */
    public static void evaluarPoblacion(String instPath, String poblFilename) throws Exception{
        //Cargar instancias
        RNA_AG ag = new RNA_AG(cargarInstancias(instPath));
        //Cargar poblacion
        ArrayList<RNA> p = cargarPoblacion(poblFilename);
        if (!p.isEmpty()){
            //Evaluar poblacion
            ag.evaluarPoblacion(p);
            //Guardar poblacion
            guardarPoblacion(poblFilename, p);
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
     * Si no se logran crear <pobl> descendientes, se crearan los restantes apartir de mutaciones, de acuerdo a la regla
     * 
     * 
     * @param poblFilename archivo que contiene TODAS las generaciones de rnas
     * @param gen generacion que va a ser creada de poblacion actual
     * @throws NumberFormatException
     * @throws IOException 
     */
    public static void Alg_Gen(String poblFilename, int gen) throws NumberFormatException, IOException{
        //Carga TODA la poblacion que ha sido creada a traves de generaciones 
        AG ag = new AG(cargarPoblacion(poblFilename));
        ArrayList<RNA> rnas = ag.getRNAs(); //Toda la poblacion
        
        Random ram = new Random(System.currentTimeMillis());
        
        //Genera descendencia
        ArrayList<RNA> desc = ag.generarDescendencia();
        
        //Genera mutaciones hasta que haya <pobl> hijos o hasta llegar al limite de intentos (5)
        for (int i = 0 ; (i < 5 && desc.size() < pobl) ; i++){
            for (int j = 0, size = desc.size() ; j < pobl - size ; j++){
                RNA rna = ag.generarMutacion(rnas.get(ram.nextInt(5))); //Se toma aleatoriamente 1 rna de las 5 mejores
                if (!rnas.contains(rna) && !desc.contains(rna))
                    desc.add(rna);
            }
        }
        //Guarda la descendencia para ser evaluada con evaluarPoblacion()
        guardarPoblacion(poblFilename, desc, gen);
    }
    
    public static void main(String[] args) throws Exception {
        
        
        /*
        Instances data = ConverterUtils.DataSource.read(args[0]);
        if (data.classIndex() == -1) 
            data.setClassIndex(data.numAttributes()-1);
        
        RNA_AG ag = new RNA_AG(data);*/
        
        //primerPoblacion("poblacion");
        //evaluarPoblacion(args[0], "poblacion2_gen1");
        //unirPoblaciones("poblacion", 1);
        
        //Alg_Gen("poblacion", 2);
        
        
        primerPoblacion("poblacion");
    }
    
    
}
