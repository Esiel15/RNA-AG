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
    private static final String relativePath = "./";
    private static final String ext = ".pobl";
    private static final int crossValidation = 10;
    
    private Instances instances;
    private Evaluation evaluation;
    
    public RNA_AG(Instances instances) throws Exception{
        this.instances = instances;
        evaluation = new Evaluation(instances);
    }
    
    //Carga la poblacion de un archivo
    public ArrayList<RNA> cargarPoblacion(String filename) throws FileNotFoundException, IOException, NumberFormatException{
        ArrayList<RNA> poblacion = new ArrayList<>();
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(new FileReader(relativePath + filename + ext));
            String l;
            while ((l = br.readLine()) != null){
                String[] rna = l.split(",");
                poblacion.add(new RNA(Integer.parseInt(rna[0]), Double.parseDouble(rna[1])));
            }
        }finally{
            if (br != null)
                br.close();
        }
        return poblacion;
    }
    
    //Guarda la poblacion en un archivo
    public void guardarPoblacion(String filename, ArrayList<RNA> poblacion) throws IOException{
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

                //Modificar el resultado de la RNA dado en el experimento
                ind.setResultado(evaluation.pctCorrect());

                /*NO SE GUARDA NINGUN OTRA COSA AUN*/
            }
        }
        
        //Se ordenan los resultados de mayor a menor
        Collections.sort(poblacion, Collections.reverseOrder());
    }
    
    
    public static void main(String[] args) throws Exception {
        
        //Carga las instancias al programa
        Instances data = ConverterUtils.DataSource.read(args[0]);
        if (data.classIndex() == -1) 
            data.setClassIndex(data.numAttributes()-1);
        
        RNA_AG ag = new RNA_AG(data);
        
        
        
        /*ArrayList<RNA> p = ag.cargarPoblacion("POBLACION_NUEVA");
        for (RNA i : p){
            System.out.println("Resultado : " + i.getResultado());
        }*/
        
        /*ArrayList<RNA> p = new ArrayList<>();
        p.add(new RNA(1)); p.add(new RNA(4)); p.add(new RNA(135));
        
        ag.evaluarPoblacion(p);
        ag.guardarPoblacion("POBLACION_NUEVA", p);*/
        
        
    }
}
