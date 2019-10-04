import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class test2 {


    public static String handleStructure(FileWriter fw, Node nd) {

        Node nameNode = nd.getFirstChild();
        String modelName = nameNode.getTextContent().strip();

        modelName = modelName.replace("-", "_");

        String importPL = new String("import Physiolibrary;");
        String importPM = new String("import Physiomodel;\n\n");



        try {
            fw.write("model " + modelName + "\n");
            fw.write("\n" + importPL + "\n" + importPM + "\n");
            //fw.close();

        } catch (Exception e) {
            System.out.println(e);
        }

        return modelName;


    }

    public static void handleVariable(FileWriter fw, Node nd) throws IOException {

        Node nameNode = nd.getFirstChild();
        String varName = nameNode.getTextContent();
        varName = varName.strip();

        //TO DO
        varName = varName.replace("-]", "minus]");
        varName = varName.replace("+]", "plus]");


        varName = varName.replace("(", "_");
        varName = varName.replace(")", "_");

        if (varName.startsWith("[")) {
            char[] charName = varName.toCharArray();
            charName[0] = '_';
            charName[charName.length - 1] = '_';
            varName = String.valueOf(charName);
        }


        String type;

        if (varName.contains("Vol")) {
            type = new String("Physiolibrary.Types.Volume ");
        } else if (varName.contains("vol")) {
            type = new String("Physiolibrary.Types.Volume ");
        } else if (varName.contains("Mass")) {
            type = new String("Physiolibrary.Types.Mass ");
        } else if (varName.contains("mass")) {
            type = new String("Physiolibrary.Types.Mass ");
        } else if (varName.contains("Density")) {
            type = new String("Physiolibrary.Types.Density ");
        } else if (varName.contains("density")) {
            type = new String("Physiolibrary.Types.Density ");
        } else {
            type = new String("Real ");
        }



        Boolean isParameter = false;
        String parameter = null;
        if (varName.contains("Initial")) {
            isParameter = true;
            parameter= new String("parameter ");
        }


        // guarda la sintassi dell'operatore '?'
        if (isParameter) {
            fw.write("\n" + parameter + type + varName + ";\n");
        } else {
            fw.write("\n" + type + varName +  ";\n");
        }




    }

    public static void handleParameter(FileWriter fw, Node nd, NodeList nL, int i, List param) throws IOException {

        Node nameNode = nL.item(i+1);//nd.getFirstChild();
        String parmName = nameNode.getTextContent();
        parmName = parmName.strip();

        //TO DO
        parmName = parmName.replace("-", "_");
        parmName = parmName.replace("-]", "minus]");
        parmName = parmName.replace("+]", "plus]");


        parmName = parmName.replace("(", "_");
        parmName = parmName.replace(")", "_");

        if (parmName.startsWith("[")) {
            char[] charName = parmName.toCharArray();
            charName[0] = '_';
            charName[charName.length - 1] = '_';
            parmName = String.valueOf(charName);
        }


        //aggiungo il nome del parametro alla lista dei parametri trovati
        param.add(parmName);

        Node valNode = nL.item(i+2);
        String parmVal = valNode.getTextContent();

        String type = null;

        if (parmName.contains("Vol")) {
            type = new String("Physiolibrary.Types.Volume ");
        } else if (parmName.contains("vol")) {
            type = new String("Physiolibrary.Types.Volume ");
        } else if (parmName.contains("Mass")) {
            type = new String("Physiolibrary.Types.Mass ");
        } else if (parmName.contains("mass")) {
            type = new String("Physiolibrary.Types.Mass ");
        } else if (parmName.contains("Density")) {
            type = new String("Physiolibrary.Types.Density ");
        } else if (parmName.contains("density")) {
            type = new String("Physiolibrary.Types.Density ");
        } else if (parmVal.contains("FALSE")) {
            type = new String("Boolean");
        } else if (parmVal.contains("TRUE")) {
            type = new String("Boolean");
        } else {
            type = new String("Real ");
        }


        fw.write("\nparameter" + type + parmName + ";\n");



    }

    public static void handleConstant(FileWriter fw, Node nd, NodeList nL, int i, List cost) throws IOException {

        Node nameNode = nL.item(i+1);
        String constName = nameNode.getTextContent();
        constName = constName.strip();

        //TO DO
        constName = constName.replace("-", "_");
        constName = constName.replace("-]", "minus]");
        constName = constName.replace("+]", "plus]");


        constName = constName.replace("(", "_");
        constName = constName.replace(")", "_");

        if (constName.startsWith("[")) {
            char[] charName = constName.toCharArray();
            charName[0] = '_';
            charName[charName.length - 1] = '_';
            constName = String.valueOf(charName);
        }


        cost.add(constName);


        //sistemo il tipo
        String type;

        if (constName.contains("Vol")) {
            type = new String("Physiolibrary.Types.Volume ");
        } else if (constName.contains("vol")) {
            type = new String("Physiolibrary.Types.Volume ");
        } else if (constName.contains("Mass")) {
            type = new String("Physiolibrary.Types.Mass ");
        } else if (constName.contains("mass")) {
            type = new String("Physiolibrary.Types.Mass ");
        } else if (constName.contains("Density")) {
            type = new String("Physiolibrary.Types.Density ");
        } else if (constName.contains("density")) {
            type = new String("Physiolibrary.Types.Density ");
        } else {
            type = new String("Real ");
        }


        Node valNode = nL.item(i+2);
        String constVal = valNode.getTextContent();

        fw.write("constant " + type + constName + "=" + constVal + ";\n");
    }

    public static void handleTimeVar(FileWriter fw, Node nd, NodeList nL, int i) throws IOException {

        Node nameNode = nL.item(i+1);
        String timerVarName = nameNode.getTextContent();
        timerVarName = timerVarName.strip();


        //TO DO
        timerVarName = timerVarName.replace("-", "_");
        timerVarName = timerVarName.replace("-]", "minus]");
        timerVarName = timerVarName.replace("+]", "plus]");


        timerVarName = timerVarName.replace("(", "_");
        timerVarName = timerVarName.replace(")", "_");

        if (timerVarName.startsWith("[")) {
            char[] charName = timerVarName.toCharArray();
            charName[0] = '_';
            charName[charName.length - 1] = '_';
            timerVarName= String.valueOf(charName);
        }


        String type = "Real";

        //NON SEMPRE ESISTE IL NODO VAL PER QUESTA VARIABILE: DEVO FARE UN CONTROLLO
        Node valNode = nL.item(i+2);
        String timerVal = valNode.getTextContent().strip();

        //scrivo le variabile sul file
        fw.write("\n" + type + timerVarName + " = " + timerVal + ";\n");


    }

    public static void handleWhiteNoise(FileWriter fw, Node nd, NodeList nL, int i) throws IOException {

        Node nameNode = nL.item(i+1);
        String whiteNoiseName = nameNode.getTextContent();
        whiteNoiseName = whiteNoiseName.strip();


        //TO DO
        whiteNoiseName = whiteNoiseName.replace("-", "_");
        whiteNoiseName = whiteNoiseName.replace("-]", "minus]");
        whiteNoiseName = whiteNoiseName.replace("+]", "plus]");


        whiteNoiseName = whiteNoiseName.replace("(", "_");
        whiteNoiseName = whiteNoiseName.replace(")", "_");

        if (whiteNoiseName.startsWith("[")) {
            char[] charName = whiteNoiseName.toCharArray();
            charName[0] = '_';
            charName[charName.length - 1] = '_';
            whiteNoiseName= String.valueOf(charName);
        }


        String type = "Real";

        Node lowLimNode = nL.item(i+2);
        Node upLimNode = nL.item(i+3);

        String Limits = String.format("(min = %.02f, max = %0.2f);", lowLimNode, upLimNode);

        fw.write("\n" + type + whiteNoiseName + Limits);

    }

    public static void handleInitialEquation(FileWriter fw, boolean hasDiffeq) throws IOException {

        String initialEquation = new String("initial equation");
        if (hasDiffeq) {
        fw.write("\n" + initialEquation + "\n");
        }
    }

    public static void handleEquation(FileWriter fw, boolean hasDiffeq) throws IOException {

        if (hasDiffeq) {
        String equation = new String("\nequation");
        fw.write("\n" + equation + "\n");
        }
    }

    public static void handleDefinition(FileWriter fw, Node nd, NodeList nL, List classInstances, int i, List variables) throws IOException {

        Node nameNode = nL.item(i+1);
        String defName = nameNode.getTextContent();
        defName = defName.strip();

        Node valNode = nL.item(i+2);
        String valDef = valNode.getTextContent().strip();

        // devo assicurarmi che i nomi delle classi siano quelli corretti per modelica: devo sostituire i "-" (dash) con "_" (underscore)
        if (valDef.contains("-")) {
            int indexOfDash = valDef.indexOf("-");
            char whatIsNext = valDef.charAt(indexOfDash+1);

            // devo assicurarmi che non sia l'operatore "meno"
            if (whatIsNext != ' ') {

                valDef = valDef.replace("-", "_");
            }

        }


        if (valDef.contains("(")) {
            for (int p=0; p < valDef.length(); p++ ) {
                if (valDef.charAt(p) == '(') { // devo assicurarmi che non siano parentesi per indicare la precedenza delle operazioni
                    if (valDef.charAt(p+1) == ' ') {}
                    else {
                        char[] charVal = valDef.toCharArray();
                        charVal[p] = '_';
                        valDef = String.valueOf(charVal);
                    }

                } else if (valDef.charAt(p) == ')') {
                    if (valDef.charAt(p-1) == ' ') {}
                    else {
                        char[] charVal = valDef.toCharArray();
                        charVal[p] = '_';
                        valDef = String.valueOf(charVal);
                    }

                }
            }
        }


        // è una variabile che modifico successivamente dentro un ciclo for
        // by default considero ogni "variabile" come parametro
        boolean isVariable = false;


        // una "variabile" è variabile se definita da un'altra variabile
        String[] terms = valDef.split("\\s");
        for (int k=0; k<terms.length; k++) {
            if (variables.contains(terms[k])) {
                isVariable = true;
            }
        }


        // devo assicurarmi che i parametri cui accedo non siano di altre classi, se sì, devo ridurre a lower case l'istanza della classe esterna
        if (valDef.contains(".")) {
            String[] words = valDef.split("\\s"); //separo utilizzando gli spazi
            for (int j=0; j<words.length; j++) {
                if (words[j].contains(".")) {

                    int dotIndex = words[j].indexOf(".");
                    //System.out.println(dotIndex);

                    if (Character.isLetter(words[j].charAt(dotIndex+1))) { //allora sto accedendo al campo di un altro modello

                        String containsDot = words[j];
                        //System.out.println(containsDot);
                        String[] splitsy = containsDot.split("\\.");    // ricorda l'escape per caratteri speciali

                        String toDeclare = splitsy[0];

                        // aggiorno l'elenco delle istanze da creare
                        classInstances.add(toDeclare + " " +  Character.toString(toDeclare.charAt(0)).toLowerCase() + toDeclare.substring(1) + ";\n");
                        //Character.toString(words[j].charAt(0)).toLowerCase() + words[j].substring(1) + ";\n");


                        //System.out.println(toDeclare);

                        words[j] = Character.toString(words[j].charAt(0)).toLowerCase()+words[j].substring(1);

                        // controllo come si chiama il campo cui accedo: se contiene nel nome "Initial" potrei considerarlo come parametro, se no variabile
                        // come trovo un campo che non contiene Initial setto a "variabile" il termine a sinistra e non lo modifico più:
                        //meglio avere una variabile che un parametro a sx dell'equzazione (per via del grado di libertà)
                        if (!splitsy[1].contains("Initial")) {
                            isVariable = true;
                        }

                    }

                }
            }
            valDef = String.join(" ", words);
        }






        // CONTROLLO IL TIPO
        String type = null;

        if (defName.toLowerCase().contains("vol")) {
            type = "Physiolibrary.Types.Volume ";
        } else if (defName.toLowerCase().contains("mass")) {
            type = new String("Physiolibrary.Types.Mass ");
        } else if (defName.contains("mass")) {
            type = new String("Physiolibrary.Types.Mass ");
        } else if (defName.toLowerCase().contains("density")) {
            type = new String("Physiolibrary.Types.Density ");
        } else {
            type = new String("Real ");
        }



        //SCRIVO SU FILE
        if (isVariable) {
            fw.write(type + String.format(" %s(start=%s);\n", defName, valDef));
        } else {
            fw.write("parameter " + type + defName + " = " + valDef + ";\n");
        }


    }






    public static void handleDiffeq(FileWriter fw, Node nd, NodeList nL, int i) throws IOException {

        //name (in modelica non viene utilizzato)
        Node diffeqName = nL.item(i+1);
        String name = diffeqName.getTextContent();

        //integralName
        Node integralName = nL.item(i+3);
        String integral = integralName.getTextContent();

        //dervName
        Node dervName = nL.item(i+2);
        String derv = dervName.getTextContent();

        //errorLim
        Node errLim = nL.item(i+4);
        String err = errLim.getTextContent();

        String eqToWrite = String.format("%s = der(%s);", integral.strip(), derv.strip());

        fw.write(eqToWrite);
     }

public static void main(String args[]) {


    //WIN VERSION
    //String destinationPath = new String("D:\\ProgettoTronci\\XmlModelica\\traduzioni\\");

    //MAC VERSION
    String destinationPath = new String("/Users/arl3cchino/IdeaProjects/XmlModelica/traduzioni/");

    //il fileName dev'essere preso in modo automatico dai file ".DES"
    String fileName = new String("Bone_Size.mo");

    //il fileName dei file DES dev''essere preso in automatico
    String fileNameDES = new String("Bone/Bone-Size.DES");


    try {
        FileWriter fw = new FileWriter(destinationPath+fileName);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(new File(fileNameDES) );
        document.normalizeDocument();


        NodeList nodeList = document.getElementsByTagName("*");

        // di default una struttura non ha equazioni differenziali: questo rende tutte le variabili "parametri"
        Boolean hasDiffeq = false;

        for (int j =0; j <nodeList.getLength(); j++) {
            Node nodo = nodeList.item(j);
            String name = nodo.getNodeName();
            //System.out.println(name);
            if (name.equals("diffeq")) {    //se ne ha, setto a true la variabile
                hasDiffeq = true;
            }
        }


        String modelName = null;

        List<String> classInstances = new ArrayList<>();

        // preparo qui 3 liste tra costanti, parametri e variabili, per fare inferenza mentre traduco
        List<String> listaVariabili = new ArrayList<>();
        List<String> listaCostanti = new ArrayList<>();
        List<String> listaParametri = new ArrayList<>();


        for (int i = 0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);


            if (node.getNodeName() == "structure") {
                modelName = handleStructure(fw, node);
            }


            //else if (node.getNodeName() == "var") {
             //   handleVariable(fw,node);
            //}

            else if (node.getNodeName() == "parm") {
                handleParameter(fw,node, nodeList, i, listaParametri);
            }

            else if (node.getNodeName() == "constant") {
                handleConstant(fw, node, nodeList, i, listaCostanti);
            }

            else if (node.getNodeName() == "timevar") {
                handleTimeVar(fw, node, nodeList, i);
            }

            else if (node.getNodeName() == "whitenoise") {
                handleWhiteNoise(fw, node, nodeList, i);

            }

            //qui inizia la gestione dei vari blocchi a seconda del nome

            else if (node.getNodeName() == "block") {
                Node nameNode = node.getFirstChild();
                String blockName = nameNode.getTextContent();



                if (blockName.equals(" Initialize ")) {
                    handleInitialEquation(fw, hasDiffeq);
                }

                else if (blockName.equals(" Calc ")) {
                    handleEquation(fw, hasDiffeq);
                }
            }


            // qui finisce la parte di gestione dei blocchi


            else if (node.getNodeName() == "equations") {
                handleEquation(fw, hasDiffeq);
            }

            else if (node.getNodeName() == "diffeq") {
                handleDiffeq(fw, node, nodeList, i);
            }

            else if (node.getNodeName() == "def") {
                handleDefinition(fw, node, nodeList, classInstances, i, listaVariabili);
            }













        }


        fw.write("\nend " + modelName + ";");
        fw.close();


        //MAC VERSION
        Path path = Paths.get("/Users/arl3cchino/IdeaProjects/XmlModelica/traduzioni/Bone_Size.mo");

        //WIN VERSION
//        Path path = Paths.get("D:\\ProgettoTronci\\XmlModelica\\traduzioni\\Bone_Size.mo");


        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        System.out.println(lines.size());

        //all'interno dell'elenco delle classi da importare potrebbero esserci dei doppioni: li elimino
        List<String> newClassInstances = classInstances.stream().distinct().collect(Collectors.toList());

        int position = 5;
        while (!newClassInstances.isEmpty()) {

        //for (int position=6; position<classInstances.size(); position++) {


            //System.out.println("pippo");
            String extraLine = newClassInstances.remove(0);
            lines.add(position, extraLine);
            Files.write(path, lines, StandardCharsets.UTF_8);

        }





    } catch (Exception e) {
        System.out.println(e);
    }

}
}
