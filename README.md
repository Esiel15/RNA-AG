# RNA-AG
Proyecto Final de Mineria de Datos.
Conseguir el mejor resultado de precisión global posible de una Red Neuronal Artificial a partir de un proceso de entrenamiento y experimentación automatizado por Algoritmos Genéticos.

# Como Compilar
MacOS y Linux
>javac -cp ".:weka.jar" -d class src/*.java

Windows
>javac -cp ".;weka.jar" -d class src/*.java

# Como ejecutar
MacOS y Linux
>java --add-opens=java.base/java.lang=ALL-UNNAMED -cp ".:weka.jar:class" RNA_AG

Windows
>java --add-opens=java.base/java.lang=ALL-UNNAMED -cp ".;weka.jar;class" RNA_AG
