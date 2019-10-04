import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class test3Old {

//  QUESTA FUNZIONE PRENDE IN INPUT UNA STRINGA E RITORNA VERO SSE IL TESTO CORRISPONDE A UN NUMERO (FLOAT, DOUBLE, INT...)
    public static boolean isNumeric(String num) {
        try{
            double d = Double.parseDouble(num);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

//  QUI SCRIVO LA FUNZIONE CHE RITORNA TRUE SE LA STRINGA PASSATA è UN CAMPO DI UN MODULO DA IMPORTARE
    public static Boolean isField(String possibleField) {

        if (!isNumeric(possibleField)) return true;
        return false;
    }


//  QUI SCRIVO LA FUNZIONE CHE MI FORMATTA IN AUTOMATICO I NOMI DELLE VARIABILI/PARAMETRI/COSTANTI
    public static String ModelicaNameFormat(String name) {

        name = name.replace("-","_");
        name = name.replace("-]", "minus]");
        name = name.replace("+]", "plus]");

        name = name.replace("(", "_");
        name = name.replace(")", "_");

        name = name.replace("/", "_");

        if (name.startsWith("[")) {
            char[] charName = name.toCharArray();   // per modificare un carattere in una posizoine devo trasformare la stringa in array...
            charName[0] = '_';
            charName[charName.length-1] = '_';
            name = String.valueOf(charName);        //... e poi far tornare tutto stringa
        }

        return name;
    }

//  QUI SCRIVO IL METODO PER INFERIRE AUTOMATICAMENTE IL TIPO DEL PARAMETRO/COSTANTE/VARIABILE CHE STO ANALIZZANDO
    public static String ModelicaInferType(String name, String val) {

        String type;

        if (name.toLowerCase().contains("vol")) type = "Physiolibrary.Types.Volume";
        else if (name.toLowerCase().contains("mass")) type = "Physiolibrary.Types.Mass";
        else if (name.toLowerCase().contains("mass")) type = "Physiolibrary.Types.Density";
        else type = "Real";

        if (val.toLowerCase().contains("true")) type = "Boolean";
        else if (val.toLowerCase().contains("false")) type = "Boolean";

        return type;
    }



//  QUI SCRIVO TUTTI I METODI PER GESTIRE SINGOLARMENTE I TAG PRESENTI NEL FILE DES
    public static void handleParameter(Map params, Node nd, NodeList nList, int i, String identifier) {

        Node nameNode = nList.item(i+1); // il file XML è mal formattato: non posso accedere ai figli con i normali metodi
        String paramName = nameNode.getTextContent().strip();

        Node valNode = nList.item(i+2);
        String paramVal = valNode.getTextContent().strip();


        //TO DO: REFORMAT NAME ACCORDING TO MODELICA RULES
        paramName = ModelicaNameFormat(paramName);


        String type = ModelicaInferType(paramName, paramVal);

        if (type == "Boolean")
            paramVal = paramVal.toLowerCase();

//      STO PROVANDO A CAMBIARE MODO DI SALVARE I NOMI E I VALORI DELLE COSTANTI:
        // PER ORA COMMENTO LE 2 RIGHE SUCCESSIVE


//      QUI FORMATTO LA STRINGA CHE DOVRO' ANDARE A SCRIVERE NELL'OFA
        //String toWrite = String.format("parameter %s %s_%s = %s;\n",type, identifier, paramName,paramVal);

//      QUI AGGIUNGO LA STRINGA FORMATTATA ALLA LISTA DEI PARAMETRI DELLA SOTTOCARTELLA
        //params.add(toWrite);

        params.put(paramName, paramVal);

    }


    public static void handleConstant(Map consts, Node nd, NodeList nList, int i, String identifier) {

        Node nameNode = nList.item(i+1); // il file XML è mal formattato: non posso usare metodi canonici per accedere a figli/fratelli
        String constName = nameNode.getTextContent().strip();

        Node valNode = nList.item(i+2);
        String constVal = valNode.getTextContent().strip();

        //TO DO: REFORMAT NAME ACCORDING TO MODELICA RULES
        constName = ModelicaNameFormat(constName);

        String type = ModelicaInferType(constName, constName);

//      STO PROVANDO A CAMBIARE MODO DI SALVARE I NOMI E I VALORI DELLE COSTANTI:
        // PER ORA COMMENTO LE 2 RIGHE SUCCESSIVE


//      QUI FORMATTO LA STRINGA CHE DOVRO' ANDARE A SCRIVERE NELL'OFA
//        String toWrite = String.format("constant %s %s_%s = %s;\n", type, identifier, constName, constVal);

//      QUI AGGIUNGO LA STRINGA ALLA LISTA OPPORTUNA
//        consts.add(toWrite);

        consts.put(constName, constVal);


    }


    public static void handleTimerVar(Map vars, Node nd, NodeList nList, int i, String identifier) {

        Node nameNode = nList.item(i+1);
        String timerName = nameNode.getTextContent().strip();


//      NON SEMPRE ESISTE IL NODO VAL PER QUESTA VARIABILE: DEVO FARE UN CONTROLLO
        Node valNode = nList.item(i+2);

        String timerVal = "";


//      PER MODELLARE LO STATO DI UNA VARIABILE CHE DESCRESCE O CRESE LUNGO IL TEMPO , POTREI PRENDERE LA VARIABILE time DI MODELICA E SOTTARRE/AGGIUNGERE(default) 1
        if (valNode.getNodeName() == "val") {
            timerVal = valNode.getTextContent().strip();
        } else {
            timerVal = "time";
        }

//      NON SEMPRE ESISTE IL NODE STATE PER QUESTA VARIABILE: DEVO FARE UN CONTORLLO


        Node stateNode = nList.item(i+3);

        String stateVal = "";

        if (stateNode.getNodeName() == "state") {
            stateVal = stateNode.getTextContent();
        } else {

//          IN MODELICA NON SO COME TRADURLO
            stateVal = "OFF";
        }


        //TO DO: REFORMAT NAME ACCORDING TO MODELICA RULES
        timerName = ModelicaNameFormat(timerName);

        String type = "Real";


//      STO PROVANDO A CAMBIARE MODO DI SALVARE I NOMI E I VALORI DELLE COSTANTI:
        // PER ORA COMMENTO LE 2 RIGHE SUCCESSIVE


//      QUI FORMATTO LA STRINGA CHE DOVRO' INSERIRE NELL'OFA
        //String toWrite = String.format("%s %s_%s = %s;\n", type, identifier, timerName, timerVal);

//      QUI AGGIUNGO LA STRINGA FORMATTATA ALL'INTERNO DELLA LISTA OPPORTUNA
        //vars.add(toWrite);

        vars.put(timerName, timerVal);

    }


    public static void handleWhiteNoise(Map vars, Node nd, NodeList nList, int i, String identifier) {

        Node nameNode = nList.item(i+1);
        String whiteNoiseName = nameNode.getTextContent().strip();

//  TO DO: REFORMAT NAME ACCORDING TO MODELICA RULES
        whiteNoiseName = ModelicaNameFormat(whiteNoiseName);


        String type = "Real";

        Node lowLimNode = nList.item(i+2);
        Node upLimNode = nList.item(i+3);

        String lowLim = lowLimNode.getTextContent();
        String upLim = upLimNode.getTextContent();


//      STO PROVANDO A CAMBIARE MODO DI SALVARE I NOMI E I VALORI DELLE COSTANTI:
//      PER ORA COMMENTO LA RIGHE SUCCESSIVE


//      QUI FORMATTO LA STRINGA DA INSERIRE NELLA LISTA
        //String toWrite = String.format("%s %s_%s(min = %s, max = %s)",type, identifier, whiteNoiseName,lowLim,upLim);


//      QUI AGGIUNGO LA STRINGA ALLA LISTA OPPURTUNA
        //vars.add(toWrite);

        //FORMATTO LA STRINGA CONTENENTE I VALORI DEI LIMITI
        String limits = String.format("(min = %s, max = %s)", lowLim, upLim);

        vars.put(whiteNoiseName, limits);

    }


//  SONO ARRIVATO QUI:
    public static void handleDefinition(Map vars, Map params, Map consts, List instances, Node nd, NodeList nList, int i, String identifier, Map container, String OFA) {

        Node nameNode = nList.item(i+1);
        String defName = nameNode.getTextContent().strip();

//      TO DO: FORMAT VAR/PAR NAME ACCORDING TO MODELICA RULE
//      PER SALVARE LA STRUTTURA, AGGIUNGO IL NOME DELLA CLASSE DAVANTI AL NOME


        defName = ModelicaNameFormat(defName);
        defName = String.format("%s_%s", identifier, defName);





        Node valNode = nList.item(i+2);
        String valDef = valNode.getTextContent().strip();

//       DELLE VOLTE IL NODO VAL CONTIENE LA PARTE DESTRA DELL'EQUAZIONE SU PIU' LINEE: LA RIPORTO SU UN'UNICA LINEA
        valDef = valDef.lines().collect(Collectors.joining(" "));

//      DEVO ASSICURARMI CHE I NOMI DELLE CLASSI RIFERITE SIANO COERENTI CON LA NOMENCLATURA DI MODELICA:
//      DEVO SOSTITUIRE I DASH (-) CON GLI UNDERSCORE (_) IFF NON SONO DEI MENO (-)
//      LA SOSTITUZIONE PUO' AVVENIRE PERCHE' I SIMBOLI MATEMATICI SONO SEMPRE PRECEDUTI E SEGUITI DA UN WHITESPACE (" ")

        char[] valDef2Arr = valDef.toCharArray();

        if (valDef.contains("-")) {
            for (int j=0; j<valDef2Arr.length-1; j++) {
                if (valDef2Arr[j] == '-') {
                    char whatIsPrec = valDef2Arr[j-1]; //sono sicuro che nessun valDef inizi con un "-"

                    if (whatIsPrec != ' ') { //non è un meno -> devo sosituire il dash con un underscore
                        valDef2Arr[j] = '_';
                    }
                }
            }

        }



//      DEVO ASSICURARMI CHE LE PARENTESI NELLA VALDEF SIANO DELLE PARENTESI CHE INDICHINO LA PRECEDENZA E NON ALTRO:
//      SE TOVO DELLE PARENTESI CHE INVECE SONO PARTE DEL NOME DI UNA CLASSE (MODULO) O ATTRIBUTO (CAMPO) -> DEVO SOSTITUIRLA CON UN UNDERSCORE

        

        if (valDef.contains("(")) {
            for (int p=0; p<valDef.length(); p++) {
                if (valDef2Arr[p] == '(') { // mi assicuro che non siano parentesi matematiche
                    if (valDef2Arr[p+1] == ' ') {}
                    else {
                        valDef2Arr[p] = '_';
                    }
                } else if (valDef2Arr[p] == ')') {
                    if (valDef2Arr[p-1] == ' ') {}
                    else {
                        valDef2Arr[p] = '_';
                    }
                }
            }
        }

//      RIPORTO TUTTO IN FORMA DI STRINGA
        valDef = String.valueOf(valDef2Arr);


//      ORA DEVO CAPIRE SE IL TERMINE CHE STO DEFINENDO SIA UNA VARIABILE O UN PARAMETRO:
//      Rules:
//      se nel nome contiene la parola "Initial"    -> è un parametro
//      se è definito (a destra) da soli parametri  -> è un parametro
//      se è definito da sole costanti              -> è un parametro (costante)
//      se è definito da ALMENO UNA VARIABILE       -> è una variabile

//      SEPARO IL LATO DESTRO DELL'EQUAZIONE PER ANALIZZARE LE SINGOLE PARTI
        String[] rightSideToken = valDef.split(" "); //per eliminare più spazi bianchi vicini, posso usare "\\s+"
        boolean undefined = true;

        for (int t=0; t <= rightSideToken.length-1;) {
            while (undefined && t <= rightSideToken.length-1) { //se non incontro mai una variabile, undefined non verrà mai settato a false: devo fare un controllo interno sul # delle iterazioni
                if (rightSideToken[t].length() > 1) { //ignoro i simboli matematici:  +, -, (, ), ...
                    if (isField(rightSideToken[t])) {
//                  COME TROVO A DESTRA UN CAMPO ACCEDUTO, CONSIDERO LA PARTE SINISTRA UNA VARIABILE

//                  RICORDO CHE STO FACENDO UNA PROVA, UTILIZZANDO LE MAPPE:vars.add(defName);
                    //per ora commento la riga qui sopra e non aggiungo nulla, poi alla fine, quando ho formatto tutto, aggiorno
                    //la hashMap delle variabili


//                  COME "CAPISCO" CHE E' UNA VARIABILE, DEVO FERMARMI: NON PUO' PIU' CAMBIARE (IL CONTRARIO NON E' VERO)
                    undefined = false;

                    } else if (params.containsKey(rightSideToken[t].split("\\.")[0])) {           //params.contains(rightSideToken[t])) {

                        // non posso ancora dire nulla

                    } else if ( consts.containsKey(rightSideToken[t].split("\\.")[0])) {          //consts.contains(rightSideToken[t])) {

                        // non posso ancora dire nulla
                    }

                };
                t++; // incremento qui, altrimenti vado in loop infinito
            }
            break;
        }

//      SE A QUESTO PUNTO NON SONO ANCORA RIUSCITO A DEFINIRE LA PARTE SINISTRA COME VARIABILE -> E' UN PARAMETRO

//        STO FACENDO UNA PROVA CON LE MAPPE: AGGIUNGO LA CHIAVE E IL VALORE ALLA FINE
//        if (undefined) params.add(defName);


        //           GUARDA LA RIGA QUI SOPRA            //


//
//      SE LA PARTE DESTRA CONTIENE L'IMPORT DI UN CAMPO DI UN'ALTRA CLASSE:
//      devo importare la classe di riferimento (quella che si trova a sinistra del punto (dot))
//      devo ricordarmi di mappare la classe presente nel file DES con quella di riferimento che la contiene (il suo OFA)

        if (valDef.contains(".")) {
            for (int j=0; j<= rightSideToken.length-1; j++) {
                if (rightSideToken[j].contains(".") && isField(rightSideToken[j])) {

                    int dotIndex = rightSideToken[j].indexOf(".");

                    if (Character.isLetter(rightSideToken[j].charAt(dotIndex+1))) { //allora sto accedendo al campo di un altro modello

                        String containsDot = rightSideToken[j];
                        String[] splitsy = containsDot.split("\\.");    //il punto è un carattere speciale: devo fare l'escape

                        String toDeclare = splitsy[0];
                        String field = splitsy[1];

//                  ORA DEVO MAPPARE LA CLASSE (MODELLO) DA IMPORTARE: NON è QUELLA CHE TROVO, MA QUELLA CHE LA CONTIENE



//                  QUI INSERISCO UN TRY CATCH TEMPORANE: STO PROVANDO SU UNA SOLA SOTTOCARTELLA E NON HO TRADOTTO TUTIT I MODELLI:
                        //QUELLI NON TRADOTTO LI GESTISCO SEPARATAMENTE


//                  PRIMA ERA: String OFAtoDeclare = container.get(toDeclare).toString();

                        String OFAtoDeclare = null;

                        try {
                            OFAtoDeclare = container.get(toDeclare).toString();
                        } catch (NullPointerException npt) {

                            OFAtoDeclare = toDeclare;
                        }

//                  AGGIORNO L'ELENCO DELLE ISTANZE DA CREARE:
//                  LO AGGIORNO SOLO QUANDO L'OFA DI "toDeclare" non coincide con quello del modello DES attuale
//                  (altrimenti non è un vero import: i campi cui sto accedendo sono già nell'OFA)


                        String thisOFA = OFA;

                        if (!thisOFA.equals(OFAtoDeclare)) { //devo importare:
//                          RICORDO CHE DEVO AGGIUNGERE _mod ALLA FINE DEL NOME DEL MODULO
//                          E IL NOME DELL'IDENTIFICATORE DAVANTI AL NOME DEL CAMPO CHE IMPORTO

                            // creo la riga per l'import di un modello esterno all'OFA corrente
                            //instances.add(String.format("%s_mod  %s_%s;", OFAtoDeclare, toDeclare, Character.toString(toDeclare.charAt(0)).toLowerCase() + toDeclare.substring(1))); Ora penso sia superfluo questo approccio
                            instances.add(String.format("%s_mod  %s;", OFAtoDeclare, toDeclare));

                            //rightSideToken[j] = Character.toString(rightSideToken[j].charAt(0)).toLowerCase() + rightSideToken[j].substring(1); Ora penso sia superfluo questo approccio

                            valDef = String.join(" ", rightSideToken);

                        } else {    //gli OFA coincidono -> devo accedere ad un campo della classe in cui mi trovo: non ho bisogno di "."

                            rightSideToken[j] = String.format("%s_%s;", toDeclare, field);

                            valDef = String.join(" ", rightSideToken);




                        }



                    }

                }
            }


        }



//      STO USANDO LE MAPPE: IL CONTROLLO DEL TIPO QUI E' INUTILE: DOVRO' CONTROLLARLO UN MOMENTO PRIMA DI SCRIVERE SUL FILE

//      ORA CONTROLLO IL TIPO
        String type = null;

//        if (defName.toLowerCase().contains("vol")) {
//            type = "Physiolibrary.Types.Volume ";
//        } else if (defName.toLowerCase().contains("mass")) {
//            type = new String("Physiolibrary.Types.Mass ");
//        } else if (defName.contains("mass")) {
//            type = new String("Physiolibrary.Types.Mass ");
//        } else if (defName.toLowerCase().contains("density")) {
//            type = new String("Physiolibrary.Types.Density ");
//        } else {
//            type = new String("Real ");
//        }



        // RICORDO CHE STO USANDO LE MAPPE E NON LE LISTE:

        // gestisco l'inserimento del "left side of equation" in base al campo "undefined"
        if (undefined) {
            params.put(defName, valDef);
        } else {
            vars.put(defName, valDef);
        }

    }


    public static void cloneFolder(String source, String target) {
        File targetFile = new File(target);
        if (!targetFile.exists()) {
            targetFile.mkdir();
        }
        for (File f : new File(source).listFiles()) {
            if (f.isDirectory()) {
                String append = "/" + f.getName();
                System.out.println("Creating '" + target + append + "': " + new File(ModelicaModuleFormat(target) + ModelicaModuleFormat(append)).mkdir());
                cloneFolder(source + append, target + append);
            }
        }


    }

    static String ModelicaModuleFormat(String module) {

        //I nomi che contengono '-' devono essere ridotti a '_'
        module = module.replace("-","_").replace(".DES","");

        return module;

    }


    public static void main(String args[]) throws IOException {

        //WIN VERSION
        //String destinationPath = new String("D:\\ProgettoTronci\\XmlModelica\\traduzioni\\");

        //MAC VERSION
        String destinationPath = new String("/Users/arl3cchino/IdeaProjects/XmlModelica/traduzioneStruttura");


        //MAC VERSION
        Path structuresPath = Paths.get("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateBis");

        //preparo la struttura in cui accoglierò le coppie <modello, contenitore> per gestire gli import
        Map<String, String> container_Map = new HashMap<>();
        //preparo la struttura per iterare sulle chiavi e sui valori
        Set<Map.Entry<String, String>> st = container_Map.entrySet();

        try (Stream<Path> walk = Files.walk(Paths.get(structuresPath.toString()))) {

            List<String> DESfiles = walk.map(x -> x.toString()).filter(f -> f.endsWith("DES")).collect(Collectors.toList());

            DESfiles.forEach(file -> {
                //System.out.println(file); // è il percorso intero di ciascun file DES
                String[] fileName = file.split("/");
                String module = ModelicaModuleFormat(fileName[fileName.length - 1]);
                String container = ModelicaModuleFormat(fileName[fileName.length - 2]);
                //System.out.println(String.format("%s è contenuto in: %s", module, container));
                container_Map.put(module, container);

            });

        }


        //PROVO SU TUTTA LA STRUTTURA
        //leggo i file e la struttura da qui
        Path dirPath = Paths.get("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateBis/");


        //PRIMA MI RICREO LA STRUTTURA DELLE CARTELLE PARI A QUELLA DI HUMOD
        //FATTO
        //cloneFolder(dirPath.toString(),destinationPath);


        try (Stream<Path> walkDirectories = Files.walk(Paths.get(dirPath.toString()))) {

            List<String> directories = walkDirectories.filter(Files::isDirectory).map(x -> x.toString()).collect(Collectors.toList());

            directories.remove(0); //il primo file è la root: la rimuovo

            directories.forEach(directory -> {

                //cattura anche le sottocartelle

                /*
                - creo le liste per: VARIABLES, CONSTANTS, PARAMETERS, EQUATIONS, INITIAL EQUATIONS
                - creo un file ".mo" in cui scrivere tutti i dati raccolti nelle sottocartelle
                 */


                String[] pathDirectory = directory.split("/");
                String nameOFADirectory = ModelicaModuleFormat(pathDirectory[pathDirectory.length - 1]); //sfrutto il nome della directory anche per creare il file OFA stesso
                //System.out.println(nameOFADirectory);  // la traduzione del nome dell'OFA va a buon fine

                //System.out.println(directory);
                //QUESTO SI PUO' AUTOMATIZZARE

                String newPath = directory.replace("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateBis/", "/Users/arl3cchino/IdeaProjects/XmlModelica/traduzioneStruttura/");

                //System.out.println(newPath);
                //System.out.println(newPath);

                //questo lo posso fare perché sul mio percorso non ho cartelle con "-", altrimenti devo modificare solo l'ultima parte del Path
                newPath = ModelicaModuleFormat(newPath);


                try {


//                  QUI RICORDO CHE IL NOME DI CIASCUN ".mo" DEV'ESSERE SEGUITO DA UN SUFFISO "_mod". ex: "Bone_Size_mod.mo"
//                  FACCIO QUESTO PERCHE' I NOMI DELLE CLASSI CHE IMPORTO POSSONO SOVRAPPORSI CON I NOMI DELLE VARIABILI PRESENTI NELLA CLASSE IN CUI IMPORTO
                    // gli OFA vengono creati nella posizione giusta
                    FileWriter fwOFA = new FileWriter(newPath + "/" + nameOFADirectory + "_mod" + ".mo");
                    fwOFA.write(String.format("model %s_mod\n", nameOFADirectory));
                    fwOFA.write("\nimport Physiolibrary;\n");
                    fwOFA.write("import Physiomodel;\n");

                    //fwOFA.close(); per ora non lo chiudo

                    // VERIFICO CHE LE CARTELLE CAMBINO ITERAZIONE DOPO ITERAZIONE - CHECKED
                    //System.out.println(String.format("sono nell cartella: %s", directory.toString()));

//                  PER ORA COMMENTO LE LISTE DI VARIABILI, COSTANTI ETC.: PROVO A USARE DELLE MAP
//                  LASCIO SOLO LE ISTANTE: NON MI INTERESSA SAPERE LA LORO "CHIAVE" O "VALORE"

//                    List<String> Variabili = new ArrayList<>();
//                    List<String> Costanti = new ArrayList<>();
//                    List<String> Parametri = new ArrayList<>();
//                    List<String> EquazioniDifferenziali = new ArrayList<>();
//                    List<String> EquazioniIniziali = new ArrayList<>();
                    List<String> Instances = new ArrayList<>();


                    Map<String, String> MapVariabili = new HashMap<>();
                    Map<String, String> MapCostanti = new HashMap<>();
                    Map<String, String> MapParametri = new HashMap<>();
                    Map<String, String> MapEquazioniDifferenziali = new HashMap<>();
                    Map<String, String> MapEquazioniIniziali = new HashMap<>();
                    Map<String, String> MapWhiteNoises = new HashMap<>();
//                    Map<String,String> MapIstanze = new HashMap<>();  NON DOVREBBE SERVIRE


                    // qui ora devo iterare su tutti i file della sottocartella
                    try (Stream<Path> walkFilesDES = Files.walk(Paths.get(directory))) {

                        List<String> DESfiles = walkFilesDES.map(x -> x.toString()).filter(f -> f.endsWith("DES")).collect(Collectors.toList());

                        DESfiles.forEach(file -> {

                            //I FILE DES VENGONO STAMPATI TUTTI - CHECKED
                            //System.out.println(file);
//                          QUI STO PRENDENDO IL NOME DEL FILE CHE STO PER PARSARE: IN QUESTO MODO QUANDO SCRIVERO' LE
//                          VAR/PARAM/CONST ALL'INTERNO DELL'OFA, POTRO' INDICARE LA LORO PROVENIENZA

                            String[] fileName2Arr = file.split("/");
                            String fileNameDES = ModelicaModuleFormat(fileName2Arr[fileName2Arr.length - 1]);

                            //INIZIA QUI IL PARSING DEL FILE DES


                            try {

                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                DocumentBuilder builder = factory.newDocumentBuilder();
                                Document document = builder.parse(file);
                                document.normalizeDocument();
                                NodeList nodeList = document.getElementsByTagName("*");

                                for (int i = 0; i < nodeList.getLength(); i++) {
                                    Node node = nodeList.item(i);

                                    if (node.getNodeName() == "parm") {
                                        handleParameter(MapParametri, node, nodeList, i, fileNameDES);
                                    } else if (node.getNodeName() == "constant") {
                                        handleConstant(MapCostanti, node, nodeList, i, fileNameDES);
                                    } else if (node.getNodeName() == "timervar") {
                                        handleTimerVar(MapVariabili, node, nodeList, i, fileNameDES);
                                    } else if (node.getNodeName() == "whitenoise") {
                                        handleWhiteNoise(MapWhiteNoises, node, nodeList, i, fileNameDES);
                                    } else if (node.getNodeName() == "def") {
                                        handleDefinition(MapVariabili, MapParametri, MapCostanti, Instances, node, nodeList, i, fileNameDES, container_Map, nameOFADirectory);
                                    }


//                                  QUI INIZIA LA PARTE DELLA GESTIONE DEI BLOCCHI


//                                  QUI FINISCE LA PARTE DELLA GESTIONE DEI BLOCCHI


                                }


                            } catch (ParserConfigurationException | SAXException | IOException e) {
                                e.printStackTrace();
                            }


//                          QUESTA PARTE NON CREDO SIA VERA - UNCHECKED

//                            // di default una struttura non ha equazioni differenziali: questo rende tutte le variabili "parametri"
//                            Boolean hasDiffeq = false;
//
//                            for (int j =0; j <nodeList.getLength(); j++) {
//                                Node nodo = nodeList.item(j);
//                                String name = nodo.getNodeName();
//                                //System.out.println(name);
//                                if (name.equals("diffeq")) {    //se ne ha, setto a true la variabile
//                                    hasDiffeq = true;
//                                }
//                            }


                        });
//


                    }

                    // QUI SCRIVO DENTRO ALL'OFA TUTTO QUELLO CHE HO RACCOLTO


                    //int numParams = MapParametri.size();
                    //int numConsts = MapCostanti.size();

//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");

                    for (Map.Entry<String,String> entry : MapCostanti.entrySet()) {

                        String constName = entry.getKey();
                        String constVal = entry.getValue();
                        String type = ModelicaInferType(constName, constVal);

                        String toWrite = String.format("constant %s %s = %s;\n", type, constName, constVal);

                        fwOFA.write(toWrite);



                    }

//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");

                    for (Map.Entry<String,String> entry : MapParametri.entrySet()) {

                        String paramName = entry.getKey();
                        String paramVal = entry.getValue();
                        String type = ModelicaInferType(paramName, paramVal);

                        String toWrite = String.format("parameter %s %s = %s;\n", type, paramName, paramVal);

                        fwOFA.write(toWrite);


                    }


//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");




                    fwOFA.write(String.format("end %s_mod", nameOFADirectory));
                    fwOFA.close();


                } catch (Exception e) {
                    System.out.println(e);
                }


                //System.out.println(dirPath.toString() + "/" + pathDirectory[pathDirectory.length-1]);


//                try (Stream<Path> walkFiles = Files.walk(Paths.get(dirPath.toString() + pathDirectory[pathDirectory.length-1]   ))) {
//
//                    List<String> DESfiles = walkFiles.map(x -> x.toString()).filter(f -> f.endsWith("DES")).collect(Collectors.toList());
//
//                    DESfiles.forEach( file -> {
//
//                        System.out.println(file);
//
//
//                    });
//
//
//
//
//                } catch (IOException e) {
//                  e.printStackTrace();
//                }


            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}