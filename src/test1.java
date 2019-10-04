import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class test1 {
public static void main(String args[]) throws ParserConfigurationException, IOException, SAXException {
    //step1: create document builder

    try {
        FileWriter fw = new FileWriter("/Users/arl3cchino/Desktop/traduzioniAutomatiche/Bone_Ph.mo");


    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();

    // step2: read the xml file to Docment object

    Document document = builder.parse(new File("Bone/Bone-Ph.DES"));
    document.getDocumentElement().normalize();


    //stampa tutti i nodi del file xml
    //System.out.println(document.getDocumentElement().getTextContent().); //.getFirstChild().getTextContent());


    NodeList nodeList = document.getElementsByTagName("*");

    //in Bone-Size ci sono 143 nodi (conta i tag aperti)
        //System.out.println(nodeList.getLength());



    // ALCUNI SETTAGGI INIZIALI:

    String modelName = null;
    List<String> classInstances = new ArrayList<>();



    for (int i = 0; i<nodeList.getLength(); i++) {// i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);



        if (node.getNodeName() == "structure") {
            //System.out.println("sono qui");
            Node nameNode = node.getFirstChild();
            modelName = nameNode.getTextContent(); //prendo il nome del modello //get Value ritorna null...
            if (modelName.contains("-")) {
                modelName = modelName.replace("-","_");
            }
            String importingPm = new String("import Physiomodel;");
            String importingPl =  new String("import Physiolibrary;");

            //TO DO

            //scrivo il nome del modello
            fw.write("model" + modelName+"\n");

            //importo le due librerire
            fw.write("\n" + importingPl + "\n" + importingPm + "\n");

        }

        else if (node.getNodeName() == "var") {
            //System.out.println("sono qua");
            Node nameNode = node.getFirstChild();
            String varName = nameNode.getTextContent();

            //TO DO
            // sistemo il nome
            //System.out.println(varName);
            // i nomi delle variabil hanno un heading e un trailing "white space": prima di controllare se inizino o finiscano
            // in un certo "modo", devo togliere gl spazi bianchi
            varName = varName.strip();


            // così non va bene: i  ' - ' (meno) nei nomi delle variabili possono indicare anioni
//            if (varName.contains("-")) {
//                varName = varName.replace("-", "_");
//            }

            varName = varName.replace("-]", "minus]");
            varName = varName.replace("+]", "plus]");


//            if (varName.contains("(")) {
                varName = varName.replace("(","_");
                varName = varName.replace(")", "_");
//            }


            if (varName.startsWith("[")) { //non prevedo strani annidamenti di parentesi quadre: per ora sono all'inizio e alla fine del nome della variabile
                char[] charName = varName.toCharArray();
                charName[0] = '_';
                charName[charName.length-1] = '_';
                varName = String.valueOf(charName);
            }





            //sistemo il tipo
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


            //scrivo le variabile sul file
            fw.write("\n" + type+varName + ";\n");

        }


        else if (node.getNodeName() == "parm") {

            //System.out.println("paperoga");


            Node nameNode = nodeList.item(i+1);


            // ottengo e sistemo il nome
            String parmName = nameNode.getTextContent();


            if (parmName.contains("-")) {
                parmName = parmName.replace("-", "_");
            }

            parmName = parmName.replace("(","_");
            parmName = parmName.replace(")", "_");


            // ottengo il valore
            Node valNode = nodeList.item(i+2);
            String parmVal = valNode.getTextContent();

            String type;

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



        }





        else if (node.getNodeName() == "constant") {

            //questo è giusto, ma hard coded
            Node nameNode = nodeList.item(i+1);
            String constName = nameNode.getTextContent(); //prendo il nome della costante

            //non funziona sui tag che vanno a capo
            //Node nameNode = node.getFirstChild();
            //String constName = nameNode.getTextContent();

            //System.out.println(constName);  ok


            //sistemo il nome
            if (constName.contains("-")) {
                constName = constName.replace("-", "_");
            }

            if (constName.contains("(")) {
                constName = constName.replace("(","_");
                constName = constName.replace(")", "_");
            }

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

            Node valNode = nodeList.item(i+2);
            String constVal = valNode.getTextContent(); //prendo il valore della costante


            //TO DO

            //scrivo la costante sul file
            fw.write("\nconstant "+type+constName+"="+constVal+";\n");
        }



        else if (node.getNodeName() == "timervar") {

            //System.out.println("topolino");


            Node nameNode = nodeList.item(i+1);
            String timerVarName = nameNode.getTextContent().strip();

            //TO DO
            // sistemo il nome
            //System.out.println(varName);

            timerVarName = timerVarName.replace("-", "_");
            timerVarName = timerVarName.replace("(","_");
            timerVarName = timerVarName.replace(")", "_");

            String type = "Real";

            Node valNode = nodeList.item(i+2);
            String timerVal = valNode.getTextContent().strip();

            //scrivo le variabile sul file
            fw.write("\n" + type + timerVarName + " = " + timerVal + ";\n");

        }


        else if (node.getNodeName() == "whitenoise") {

            Node nameNode = nodeList.item(i+1);
            String whitenName = nameNode.getTextContent().strip();


            whitenName = whitenName.replace("-", "_");
            whitenName = whitenName.replace("(","_");
            whitenName = whitenName.replace(")", "_");

            String type = "Real";

            Node lowlimNode = nodeList.item(i+2);
            Node uplimNode = nodeList.item(i+3);

            String Limits = String.format("(min = %.02f , max = %0.2f);",lowlimNode, uplimNode);

            fw.write("\n" + type + whitenName +  Limits);

        }





        else if (node.getNodeName() == "block") {
            Node nameNode = node.getFirstChild();
            String blockName = nameNode.getTextContent();
            //System.out.println(blockName);


            if (blockName.equals(" Initialize ")) { // devo usare equals, e occhio agli spazi all'inizio e fine
                String initialEquation = new String("\ninitial equation");
                fw.write("\n" + initialEquation + "\n");

            }
        }




        else if (node.getNodeName() == "def") {

            //System.out.println("paperino");

            Node nameNode = nodeList.item(i+1);
            String defName = nameNode.getTextContent();

            Node valNode = nodeList.item(i+2);
            String valDef = valNode.getTextContent();

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
                System.out.println("paperone");
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

            // devo assicurarmi che i parametri cui acceso non siano di altre classi, se sì, devo ridurre a lower case l'istanza della classe esterna
            if (valDef.contains(".")) {
                String[] words = valDef.split("\\s");
                for (int j=0; j<words.length; j++) {
                    if (words[j].contains(".")) {

                        int dotIndex = words[j].indexOf(".");
                        //System.out.println(dotIndex);

                        if (Character.isLetter(words[j].charAt(dotIndex+1))) {

                            String containsDot = words[j];
                            //System.out.println(containsDot);
                            String[] splitsy = containsDot.split("\\.");    // ricorda l'escape per caratteri speciali

                            String toDeclare = splitsy[0];

                            // aggiorno l'elenco delle istanze da creare
                            classInstances.add(toDeclare + " " +  Character.toString(toDeclare.charAt(0)).toLowerCase() + toDeclare.substring(1) + ";\n");
                                    //Character.toString(words[j].charAt(0)).toLowerCase() + words[j].substring(1) + ";\n");


                            //System.out.println(toDeclare);

                            words[j] = Character.toString(words[j].charAt(0)).toLowerCase()+words[j].substring(1);

                        }

                    }
                }
                valDef = String.join(" ", words);
            }


            fw.write(defName + " = " + valDef + ";\n" );
        }


        else if (node.getNodeName() == "diffeq") {
            //System.out.println("pluto");
            fw.write("\nequation\n");

            Node nameNode = node.getNextSibling();

            //name
            Node diffeqName = nodeList.item((i+1));
            String name = diffeqName.getTextContent();

            //integralName
            Node integralName = nodeList.item((i+3));
            String integral = integralName.getTextContent();

            //dervName
            Node dervName = nodeList.item((i+2));
            String derv = dervName.getTextContent();

            //errorLim
            Node errLim = nodeList.item((i+4));
            String err = errLim.getTextContent();   //NON CREDO SI POSSA SETTARE LA TOLLERANZA PER UNA SOLA EQUAZIONE


            //System.out.println(name + integral + derv + err);
            String eqToWrite = String.format("%s = der(%s);", integral.strip(), derv.strip()); //ricordo che i nomi delle variabili hanno gli spazi di testa e coda

            //System.out.println(eqToWrite);
            fw.write(eqToWrite);
        }





    }



    Path path = Paths.get("/Users/arl3cchino/Desktop/traduzioniAutomatiche/Bone_AlphaReceptors.mo");
    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

    for (int position=6; position<classInstances.size(); position++) {

        String extraLine =  classInstances.remove(0);
        lines.add(position, extraLine);
        Files.write(path, lines, StandardCharsets.UTF_8);

    }



    fw.write("\nend " + modelName + ";");
    fw.close();


    /*
    Path path = Paths.get("/Users/arl3cchino/Desktop/Bone_Mineral.mo");
    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);


    // qui potrei fare qualche controllo sulla presenza o meno di elementi da sistemare

    List<String> newClassInstances = classInstances.stream().distinct().collect(Collectors.toList());


    int position = 5;
    int size = newClassInstances.size();
    for (int m=0; m < size; m++) {

        String extraLine =  newClassInstances.remove(0);
        lines.add(position, extraLine);
        Files.write(path, lines, StandardCharsets.UTF_8);

    }


    */

    } catch (Exception e) {
        System.out.println(e);
    }





}
public static void printTags(Node nodes){
    if(nodes.hasChildNodes() || nodes.getNodeType() != 3){
        System.out.println(nodes.getNodeName()+" : "+nodes.getTextContent());
        NodeList nl = nodes.getChildNodes();
        for(int j=0; j<nl.getLength(); j++) printTags(nl.item(j));
    }
}

}
