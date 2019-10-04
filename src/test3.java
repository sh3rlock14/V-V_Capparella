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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class test3 {

    static List<String> ListFunctions = new ArrayList<>(Arrays.asList("InterpolateVector"));
    static List<String> ListBinaryOperator = new ArrayList<>(Arrays.asList("+ - * / ^ MIN MAX ".split("\\s+")));
    static List<String> ListUnaryOperator = new ArrayList<>(Arrays.asList("INVERT EXP LOG LOG10 SQRT SIN COS TAN ABS ROUND ROUNDUP ROUNDDOWN".split("\\s+")));

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E LADDOVE INCONTRA UN "+" (OPERTORE MATEMATICO) DA LA PRECEDENZA AI DUE ADDENDI COINVOLTI:
//  QUESTO PERCHE' STANDO ALLE SPECIFICHE DI HUMMOD, NON ESISTE UNA PRECEDENZA TRA SEGNI. PER CUI: NON POTENDO "TOGLIERE LA PRECEDENZA" ALLA MOLTIPLICAZIONE E ALLA DIVISIONE
//  INSERISCO FRA PARENTESI GLI ADDENDI, IN QUESTO MODO:
    //SE AVEVANO GIA' DA PRIMA LA PRECEDENZA (C'ERANO PARENTESI) LA SITUAZIONE NON CAMBIA
    //ALTRIMENTI ORA HANNO LE PRECEDENZA, E DI CONSEGUENZA TUTTI GLI OPERATORI HANNO LA STESSA PRECENZA -> COME RISULTATO OTTENGO CHE LE OPERAZIONI
    //VERRANNO SVOLTE DA SX A DX


//  COME PRIMA COSA DEVO SISTEMARE GLI OPERATORI UNARI
    static public String fixUnaryOperator(String valdef) {

        List<String> ListUnaryOperator = new ArrayList<>(Arrays.asList("INVERT EXP LOG LOG10 SQRT SIN COS TAN ABS ROUND ROUNDUP ROUNDDOWN SINDEG".split("\\s+")));
        List<String> ListValDef = new ArrayList<>(Arrays.asList(valdef.split("\\s+")));

        //L'OBIETTIVO E' QUELLO DI RACCHIUDERE TRA PARENTESI L'OPERATORE E IL SUO ARGOMENTO

        for (int i=0; i <= ListValDef.size()-1; i++) {

            if (ListUnaryOperator.contains(ListValDef.get(i))) { // sto trattando un operatore unario

                if (ListValDef.get(i+1).equals("(")) { // probabilmente ha più di un argomento

                    int closingParen = findClosingParen(ListValDef, i+1);
                    ListValDef.add(closingParen, ")");
                    ListValDef.add(i, "(");         //ho racchiuso tra parentesi il logaritmo e il suo argomento: ( LOG ( ARG ) ).
                    i++;

                }

                else { // SE NON HO PARENTESI SUBITO DOPO IL LOG, VUOL DIRE CHE L'ARGOMENTO E' UNICO. LOG A.
                    //IO DEVO AGGIUNGERE LE PARENTESI ATTORNO ALL'ARGOMENTO E ANCHE ATTORNO AL LOGARITMO: ( LOG ( A ) )

                    if (ListValDef.get(i).contains("DEG")) {
                        ListValDef.add(i+2, ") ) )");
                        ListValDef.add(i+1, "( (");
                    }
                    else {
                        ListValDef.add(i + 2, ") )");
                        ListValDef.add(i + 1, "(");

                    }

                    ListValDef.add(i, "(");
                    i++;

                }


            }


        }


        return String.join(" ",ListValDef).strip();
    }

//  COME SECONDA COSA SISTEMA LE PARENTESI DEGLI OPERATORI BINARI
    static public String fixBinaryOperator(String valdef) {

        List<String> ListBinaryOperator = new ArrayList<>(Arrays.asList("+ - * / ^ MIN MAX ".split("\\s+")));
        List<String> ListUnaryOperator = new ArrayList<>(Arrays.asList("INVERT EXP LOG LOG10 SQRT SIN COS TAN ABS ROUND ROUNDUP ROUNDDOWN SINDEG".split("\\s+")));

        List<String> ListValDef = new ArrayList<>(Arrays.asList(valdef.split("\\s+")));

//      UN'IDEA BUONA PARE ESSERE QUESTA: QUANDO INCONTO UN OPERATORE, VEDO SE PRIMA O DOPO CI SONO PARENTESI:
        //SE NON CI SONO -> LE METTO IO
        //ALTRIMENTI NIENTE

        for (int i=0; i <= ListValDef.size()-1; i++) {



//      HO 4 CASI:
//      I      ) OP (
//      II       OP (
//      III    ) OP
//      IV       OP


            if (ListBinaryOperator.contains(ListValDef.get(i))) {
                if ( !ListValDef.get(i+1).contains("(") ) {     //SUBITO DOPO UN OPERATORE BINARIO NON POSSO TROVARE PARENTESI CHIUSE




                    //  CASO IV

                    if ( !ListValDef.get(i-1).contains(")") ) { //SUBITO PRIMA DI UN OPERATORE BINARION NON POSSO TROVARE PARENTESI APERTE

                        //NON HO PARENTESI NE' PRIMA NE' DOPO L'OPERATORE -> HO SOLAMENTE I DUE OPERANDI CHE LO COINVOLGONO


                        if (!ListUnaryOperator.contains(ListValDef.get(i+1))) { //SE HO UN OPERATORE UNARIO NON POSSO METTERGLI DAVANTI UNA PARENTESI CHIUSA


//                      RICORDA: PRIMA SI AGGIUNGE A DESTRA, POI A SINISTA: ALTIRMENTI SLITTANO GLI INDICI
                            ListValDef.add(i + 2, ")");
                            ListValDef.add(i - 1, "(");
                            i++;

                        }


                    }

                    //  CASO III

                    else {  //HO SOLO UNA PARENTESI CHIUSA PRIMA DELL'OPERATORE BINARIO

                        //DEVO CHIUDERE UNA PARENTESI DUBITO DOPO L'OPERANDO CHE SEGUE L'OPERATORE
                        //DEVO APRIRE UNA PARENTESI SUBITO PRIMA DELL'OPERANDO CHIUSO TRA PARENTESI
//                        String secondOp = ListValDef.get(i+1);


                        int openPar = findOpenParen(ListValDef, i-1);

                        if (!ListUnaryOperator.contains(ListValDef.get(i+1))) { //SE HO UN OPERATORE UNARIO NON POSSO METTERGLI DAVANTI UNA PARENTESI CHIUSA

                            ListValDef.add(i+2,")");
                            ListValDef.add(openPar,"(");
                            i++;

                        }

                    }
                }
                else {

                    //CASO II

                    if ( !ListValDef.get(i-1).contains(")") ) {

                        //DEVO APRIRE UNA PARENTESI SUBITO PRIMA DELL'OPERANDO A SINISTRA DELL'OPERATORE
                        ListValDef.add(i-1, "(");       //ho cambiato qui
                        i++;

                        //DEVO CHIUDERE UNA PARENTESI SUBITO DOPO QUELLA CHE CHIUDE L'OPERANDO TRA PARENTESI, A DESTRA DELL'OPERATORE
                        int closePar = findClosingParen(ListValDef, i+1)+1;
                        ListValDef.add(closePar, ")");

                    }

                    //CASO I

                    else {

                        //DEVO TROVARE DOVE SI CHIUDE LA PARENTESI IMMEDIATAMENTE SUCCESSIVA ALL'OPERATORE E AGGIUNGERNE UN'ALTRA SUBITO DOPO
                        int closePar = findClosingParen(ListValDef, i+1)+1;
                        ListValDef.add(closePar, ")");

                        //DEVO TROVARE DOVE SI APRE LA PARENTESI IMMEDIATAMENTE PRECEDENTE ALL'OPERATORE E AGGIUNGERNE UNA PRIMA
                        int openPar = findOpenParen(ListValDef, i-1);
                        ListValDef.add(openPar, "(");
                        i++;

                    }




                }

            }

        }

        String toReturn = String.join(" ", ListValDef).strip();

        return  toReturn;

    }


//  VERSIONE PER UN ARRAYLIST DI STRINGHE
    static public int findClosingSBParen(List<String> text, int openPos) {
    int closePos = openPos;
    int counter = 1;
    while (counter > 0) {
        String s = text.get(++closePos);
        if (s .equals("[")) {
            counter++;
        }
        else if (s.equals("]")) {
            counter--;
        }
    }
    return closePos;
}


//  VERSIONE PER UN ARRAYLIST DI STRINGHE
    static public int findOpenParen(List<String> text, int closePos) {
        int openPos = closePos;
        int counter = 1;
        while (counter > 0) {
            String s = text.get(--openPos);
            if (s.equals("(")) {
                counter--;
            }
            else if (s.equals(")")) {
                counter++;
            }
        }
        return openPos;
    }

//  VERSIONE PER UN ARRAYLIST DI STRINGHE
    static public int findClosingParen(List<String> text, int openPos) {
        int closePos = openPos;
        int counter = 1;
        while (counter > 0) {
            String s = text.get(++closePos);
            if (s .equals("(")) {
                counter++;
            }
            else if (s.equals(")")) {
                counter--;
            }
        }
        return closePos;
    }

// QUESTE FUNZIONI PRENDONO IN INPUT LA STRINGA "VALDEF" E FORMATTANO CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONI DES SIN/COS/TAN DEG
    public static String handleSINDEG(String valdef) {

        valdef = valdef.replaceAll("SINDEG \\(", "sin ( from_deg");

        return valdef;
    }

    public static String handleCOSDEG(String valdef) {

        valdef = valdef.replaceAll("COSDEG \\(", "cos ( from_deg");

        return valdef;

    }

    public static String handleTANDEG(String valdef) {

        valdef = valdef.replaceAll("TANDEG \\(", "tan ( from_deg");

        return valdef;

    }

// QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE sqrt(a)
    public static String handleSQRT(String valdef) {

        valdef = valdef.replaceAll("SQRT", "sqrt");

        return valdef;

    }

// QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE sin(a)
    public static String handleSIN(String valdef) {

        valdef = valdef.replaceAll("SIN", "sin");

        return valdef;

    }

// QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE cos(a)
    public static String handleCOS(String valdef) {

        valdef = valdef.replaceAll("COS", "cos");

        return valdef;

    }

// QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE tan(a) (a espresso in radianti)
    public static String handleTAN(String valdef) {

        valdef = valdef.replaceAll("TAN", "tan");

        return valdef;

    }


//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE log(a) (logaritmo naturale)
//  ATTENZIONE: IL LOG NATURALE NEI FILE DES  CONTIENE SEMPRE FRA PARENTESI IL SUO ARGOMENTO
    public static String handleLOGN(String valdef) {

        valdef = valdef.replaceAll("LOG", "log");

        return valdef;
    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE log10(a) (logaritmo in base 10)
//  ATTENZIONE: IL LOG IN BASE DIESCI NEI FILE DES NON CONTIENE SEMPRE IL SUO ARGOMENTO FRA PARENTESI
    private static String handleLOG10(String valdef) {

        valdef = valdef.replaceAll("LOG10", "log10");

        return valdef;
    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE "INVERT" (ovvero dividere 1 per il valore di VALDEF)
    private static String handleUnaryINVERT(String valdef) {

        valdef = valdef.replaceAll("INVERT", "1 / ");

        return valdef;
    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE exp(a)
    private static  String handleUnaryEXP(String valdef) {

        valdef.replaceAll("EXP", "exp");

        return valdef;
    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE min(a,b)
    public static String handleBinaryMIN(String valdef) {


        List<String> valDef2Arr = new ArrayList<>(Arrays.asList(valdef.split("\\s+")));


        for (int i = 0; i <= valDef2Arr.size() - 1; i++) {

            if (valDef2Arr.get(i).equals("MIN")) {


                if (!valDef2Arr.get(i + 1).equals("(")) {

                    if (!valDef2Arr.get(i - 1).equals(")")) { //CASO I:  A MIN B

                        String firstTerm = valDef2Arr.get(i - 1);
                        String secondTerm = valDef2Arr.get(i + 1);

                        String format = String.format("min(%s, %s)", firstTerm, secondTerm);

                        valDef2Arr.set(i + 1, format);
                        valDef2Arr.set(i, "");
                        valDef2Arr.set(i - 1, "");

                    } else {  //CASO II: A ) MIN B

                        //IL SECONDO TERMINE LO POSSO PRENDERE IMMEDIATAMENTE, IL PRIMO DEVO RINTRACCIARE LA PARENTESI APERTA A SX
                        String secondTerm = valDef2Arr.get(i + 1);

                        int openingPar = findOpenParen(valDef2Arr, i - 1);

                        StringBuilder format = new StringBuilder("min(");

                        for (int sub = openingPar; sub <= i - 1; sub++) {

                            format.append(valDef2Arr.get(sub));
                            valDef2Arr.set(sub, "");

                        }

                        format.append(", " + secondTerm + ")");
                        String toReturn = format.toString();
                        valDef2Arr.set(i, toReturn);
                        valDef2Arr.set(i + 1, "");


                    }
                }

                else {

                    if ( !valDef2Arr.get(i-1).equals(")")) {    //CASO III: A MIN ( B

                        //IL SECONDO TERMINE LO POSSO PRENDERE IMMEDIATAMENTE, IL PRIMO DEVO RINTRACCIARE LA PARENTESI APERTA A SX
                        String firstTerm = valDef2Arr.get(i - 1);

                        int closingPar = findClosingParen(valDef2Arr, i + 1);

                        StringBuilder format = new StringBuilder("min( " + firstTerm + ", ");

                        for (int sub = i+1; sub <= closingPar; sub++) {

                            format.append(valDef2Arr.get(sub));
                            valDef2Arr.set(sub, "");

                        }

                        format.append(")");
                        String toReturn = format.toString();
                        valDef2Arr.set(i, toReturn);
                        valDef2Arr.set(i - 1, ""); //TOLGO A DALLA LISTA: L'HO MESSO NELLO SBUILDER



                    }

                    else {  //CASO IV: A ) MIN ( B


                        int openingPar = findOpenParen(valDef2Arr, i - 1);

                        StringBuilder format = new StringBuilder("min(");

                        for (int sub = openingPar; sub <= i - 1; sub++) {

                            format.append(valDef2Arr.get(sub));
                            valDef2Arr.set(sub, "");

                        }




                        //valDef2Arr.set(i + 1, ""); //TOLGO B DALLA LISTA
                        format.append(", ");


                        int closingPar = findClosingParen(valDef2Arr, i + 1);

                        for (int sub = i+1; sub <= closingPar; sub++) {

                            format.append(valDef2Arr.get(sub));
                            valDef2Arr.set(sub, "");

                        }

                        format.append(")");
                        String toReturn = format.toString();
                        valDef2Arr.set(i, toReturn);
                        //valDef2Arr.set(i - 1, ""); //TOLGO A DALLA LISTA: L'HO MESSO NELLO SBUILDER



                    }

                }


            }


        }



        return String.join(" ", valDef2Arr).replaceAll("  ", "");


    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E FORMATTA CON LE REGOLE DI MODELICA IL TESTO DELLA FUNZIONE max(a,b)
    public static String handleBinaryMAX(String valdef) {


        List<String> valDef2Arr = new ArrayList<>(Arrays.asList(valdef.split("\\s+")));


        for (int i = 0; i <= valDef2Arr.size() - 1; i++) {

            if (valDef2Arr.get(i).equals("MAX")) {


                if (!valDef2Arr.get(i + 1).equals("(")) {

                    if (!valDef2Arr.get(i - 1).equals(")")) { //CASO I:  A MAX B

                        String firstTerm = valDef2Arr.get(i - 1);
                        String secondTerm = valDef2Arr.get(i + 1);

                        String format = String.format("max(%s, %s)", firstTerm, secondTerm);

                        valDef2Arr.set(i + 1, format);
                        valDef2Arr.set(i, "");
                        valDef2Arr.set(i - 1, "");

                    } else {  //CASO II: A ) MAX B

                        //IL SECONDO TERMINE LO POSSO PRENDERE IMMEDIATAMENTE, IL PRIMO DEVO RINTRACCIARE LA PARENTESI APERTA A SX
                        String secondTerm = valDef2Arr.get(i + 1);

                        int openingPar = findOpenParen(valDef2Arr, i - 1);

                        StringBuilder format = new StringBuilder("max(");

                        for (int sub = openingPar; sub <= i - 1; sub++) {

                            format.append(valDef2Arr.get(sub));
                            valDef2Arr.set(sub, "");

                        }

                        format.append(", " + secondTerm + ")");
                        String toReturn = format.toString();
                        valDef2Arr.set(i, toReturn);
                        valDef2Arr.set(i + 1, "");


                    }
                }

                else {

                    if ( !valDef2Arr.get(i-1).equals(")")) {    //CASO III: A MAX ( B

                        //IL SECONDO TERMINE LO POSSO PRENDERE IMMEDIATAMENTE, IL PRIMO DEVO RINTRACCIARE LA PARENTESI APERTA A SX
                        String firstTerm = valDef2Arr.get(i - 1);

                        int closingPar = findClosingParen(valDef2Arr, i + 1);

                        StringBuilder format = new StringBuilder("max( " + firstTerm + ", ");

                        for (int sub = i+1; sub <= closingPar; sub++) {

                            format.append(valDef2Arr.get(sub));
                            valDef2Arr.set(sub, "");

                        }

                        format.append(")");
                        String toReturn = format.toString();
                        valDef2Arr.set(i, toReturn);
                        valDef2Arr.set(i - 1, ""); //TOLGO A DALLA LISTA: L'HO MESSO NELLO SBUILDER



                    }

                    else {  //CASO IV: A ) MAX ( B


                        int openingPar = findOpenParen(valDef2Arr, i - 1);

                        StringBuilder format = new StringBuilder("max(");

                        for (int sub = openingPar; sub <= i - 1; sub++) {

                            format.append(valDef2Arr.get(sub));
                            valDef2Arr.set(sub, "");

                        }




                        //valDef2Arr.set(i + 1, ""); //TOLGO B DALLA LISTA
                        format.append(", ");


                        int closingPar = findClosingParen(valDef2Arr, i + 1);

                        for (int sub = i+1; sub <= closingPar; sub++) {

                            format.append(valDef2Arr.get(sub));
                            valDef2Arr.set(sub, "");

                        }

                        format.append(")");
                        String toReturn = format.toString();
                        valDef2Arr.set(i, toReturn);
                        //valDef2Arr.set(i - 1, ""); //TOLGO A DALLA LISTA: L'HO MESSO NELLO SBUILDER



                    }

                }


            }


        }



    return String.join(" ", valDef2Arr).replaceAll("  ", "");


}


//  QUESTE FUNZIONI PRENDONO IN INPUT LA STRINGA "VALDEF" E LA FORMATTANO CON LE REGOLE DI MODELICA, TRADUCENDO GLI OPERATORI LOGICI BINARI
    public static String handleLogicalGT(String valdef) {

        valdef = valdef.replaceAll("GT", ">");

        return valdef;
    }

    public static String handleLogicalGE(String valdef) {

        valdef = valdef.replaceAll("GE", ">=");

        return valdef;
    }

    public static String handleLogicalLT(String valdef) {

        valdef = valdef.replaceAll("LT", "<");

        return valdef;
    }

    public static String handleLogicalLE(String valdef) {

        valdef = valdef.replaceAll("LE", "<=");

        return valdef;
    }

    public static String handleLogicalNE(String valdef) {

        valdef = valdef.replaceAll("NE", "<>");

        return valdef;
    }

    public static String handleLogicalEQ(String valdef) {

        valdef = valdef.replaceAll("EQ", "==");

        return valdef;
    }

    public static String handleLogicalOR(String valdef) {

        valdef = valdef.replaceAll("OR", "or");

        return valdef;
    }

    public static String handleLogicalAND(String valdef) {

        valdef = valdef.replaceAll("AND", "and");

        return valdef;
    }

    public static String handleUnaryNOT(String valdef) {

        valdef = valdef.replaceAll("NOT", "not");

        return valdef;
    }


//  QUESTA FUNZIONE GESTISCE I BLOCCHI "CURVE"
    private static void handleCurve(Map<String, String> MapCurve, Node node, NodeList nodeList, int i, String fileNameDES, Map<String, String> container_map, String nameOFADirectory) {

        Node nodeName = nodeList.item(i+1);
        String nameCurve = nodeName.getTextContent().strip();

        //TO DO:
        //FORMATTO IL NOME SECONDO LE REGOLE DI MODELICA
        nameCurve = ModelicaNameFormat(nameCurve);

        //AGIUNGO QUI IL MODULO DES, COSì NON HO BISOGNO DI UNA LISTA DI STRINGHE COME VALUE PER LA HASHMAP "MAPCURVE"
        nameCurve = fileNameDES + "_" + nameCurve;


        StringBuilder sb = new StringBuilder("[");


        int j=2; //E' L'ALTRO INDICE CHE MI PORTA AVANTI CON LA LETTURA DEI NODI

        List<String> validComponents = new ArrayList<>(Arrays.asList("point x y slope".split("\\s+")));

        //DA QUI SCANDISCO I NODI FIN QUANDO NON INCONTRO IL NODO </curve>
        while(true) {

            Node nextNode = nodeList.item(i+j);
            if (!validComponents.contains(nextNode.getNodeName())) {
                sb.replace(sb.length()-1, sb.length(), "]"); //TOLGO L'ULTIMO ";" E CHIUDO LA MATRICE DEI PUNTI
                break;
            } else if (nextNode.getNodeName().equals("x")) {
                String xVal = nextNode.getTextContent();
                sb.append(xVal + ",");
            } else if (nextNode.getNodeName().equals("y")) {
                String yVal = nextNode.getTextContent();
                sb.append(yVal);
                sb.append(";"); //PER ORA NON LO INSERISCO ASSIEME A YVAL
            }
            j++;

        }

        String points = sb.toString();

        MapCurve.put(nameCurve, points);


    }


//  QUESTA FUNZIONE GESTISCE I BLOCCHI "CONDIZIONALI"
    public static void handleConditional(Map<String, List<String>> MapConditional, Map<String, List<String>> MapVariabili, Map<String, String> Instances, Node node, NodeList nodeList, int i, String fileNameDES, Map<String, String> container_map, String nameOFADirectory ) {

        Node nodeName = nodeList.item(i+1);
        String namedVariable = nodeName.getTextContent().strip();

        //TO DO:
        //FORMATTO IL NOME SECONDO LE REGOLE DI MODELICA
        namedVariable = ModelicaNameFormat(namedVariable);

        Node nodeTest = nodeList.item(i+2);
        String testIf = nodeTest.getTextContent().strip();

        //TO DO:
        //FORMATTO I NOMI DEI VALORI
        //IMPORTO LE CLASSI CHE OCCORRONO
        testIf = testIf.lines().collect(Collectors.joining(" "));

        testIf = handleLogicalOR(testIf);
        testIf = handleLogicalAND(testIf);
        testIf = handleLogicalEQ(testIf);
        testIf = handleLogicalGE(testIf);
        testIf = handleLogicalGT(testIf);
        testIf = handleLogicalLE(testIf);
        testIf = handleLogicalLT(testIf);
        testIf = handleLogicalNE(testIf);
        testIf = handleUnaryNOT(testIf);



        if ( testIf.contains("-") ) {
            testIf = FormatDashValDef(testIf);
        }

        testIf = FormatSignsValDef(testIf);

        if ( testIf.contains("(")) {
            testIf = FormatParValDef(testIf);
        }

        if (testIf.contains("[")) {
            testIf = FormatSqBrackValDef(testIf);
        }

        testIf = ManageImport(testIf, Instances, fileNameDES, container_map, nameOFADirectory);


//      NON SEMBRA DEBBA FARE ALTRI CONTROLLI

//      LE STRINGHE DEI NODI "TRUE" E "FALSE" LE DEVO TRATTARE COSE SE FOSSERE DELLE NORMALI "VALDEF" PER I NODI "DEF"

        Node nodeTrue = nodeList.item(i+3);
        String retTrue = nodeTrue.getTextContent().strip();


//      GESTISCO LA "TRUE"

        retTrue = retTrue.lines().collect(Collectors.joining());

        if (retTrue.contains("-")) {
            retTrue = FormatDashValDef(retTrue);
        }

        retTrue = FormatSignsValDef(retTrue);

        if (retTrue.contains("(")) {
            retTrue = FormatParValDef(retTrue);
        }

        if (retTrue.contains("[")) {
            retTrue = FormatSqBrackValDef(retTrue);
        }

        retTrue = ManageImport(retTrue, Instances, fileNameDES, container_map, nameOFADirectory);

        retTrue = fixUnaryOperator(retTrue);
        retTrue = fixBinaryOperator(retTrue);

        if (retTrue.contains("MIN")) {
            retTrue = handleBinaryMIN(retTrue);
        }

        if (retTrue.contains("MAX")) {
            retTrue = handleBinaryMAX(retTrue);
        }

        if (retTrue.contains("EXP")) {
            retTrue = handleUnaryEXP(retTrue);
        }

        if (retTrue.contains("INVERT")) {
            retTrue = handleUnaryINVERT(retTrue);
        }

        if (retTrue.contains("LOG")) {
            retTrue = handleLOGN(retTrue);
        }

        if (retTrue.contains("LOG10")) {
            retTrue = handleLOG10(retTrue);
        }

        if (retTrue.contains("SQRT")) {
            retTrue = handleSQRT(retTrue);
        }

        if (retTrue.contains("COS")) {
            retTrue = handleCOS(retTrue);
        }

        if (retTrue.contains("TAN")) {
            retTrue = handleTAN(retTrue);
        }

        if (retTrue.contains("SINDEG")) {
            retTrue = handleSINDEG(retTrue);
        }

        if (retTrue.contains("COSDEG")) {
            retTrue = handleCOSDEG(retTrue);
        }

        if (retTrue.contains("TANDEG")) {
            retTrue = handleTANDEG(retTrue);
        }


//      GESTISCO LA "FALSE"


        Node nodeFalse = nodeList.item(i+4);
        String retFalse = nodeFalse.getTextContent().strip();



        retFalse = retFalse.lines().collect(Collectors.joining());
        if (retFalse.contains("-")) {
            retFalse = FormatDashValDef(retFalse);
        }

        retFalse = FormatSignsValDef(retFalse);

        if (retFalse.contains("(")) {
            retFalse = FormatParValDef(retFalse);
        }

        if (retFalse.contains("[")) {
            retFalse = FormatSqBrackValDef(retFalse);
        }

        retFalse = ManageImport(retFalse, Instances, fileNameDES, container_map, nameOFADirectory);

        retFalse = fixUnaryOperator(retFalse);
        retFalse = fixBinaryOperator(retFalse);

        if (retFalse.contains("MIN")) {
            retFalse = handleBinaryMIN(retFalse);
        }

        if (retFalse.contains("MAX")) {
            retFalse = handleBinaryMAX(retFalse);
        }

        if (retFalse.contains("EXP")) {
            retFalse = handleUnaryEXP(retFalse);
        }

        if (retFalse.contains("INVERT")) {
            retFalse = handleUnaryINVERT(retFalse);
        }

        if (retFalse.contains("LOG")) {
            retFalse = handleLOGN(retFalse);
        }

        if (retFalse.contains("LOG10")) {
            retFalse = handleLOG10(retFalse);
        }

        if (retFalse.contains("SQRT")) {
            retFalse = handleSQRT(retFalse);
        }

        if (retFalse.contains("COS")) {
            retFalse = handleCOS(retFalse);
        }

        if (retFalse.contains("TAN")) {
            retFalse = handleTAN(retFalse);
        }

        if (retFalse.contains("SINDEG")) {
            retFalse = handleSINDEG(retFalse);
        }

        if (retFalse.contains("COSDEG")) {
            retFalse = handleCOSDEG(retFalse);
        }

        if (retFalse.contains("TANDEG")) {
            retFalse = handleTANDEG(retFalse);
        }


        List<String> valuesVariable = new ArrayList<>();
        valuesVariable.add(fileNameDES);
        valuesVariable.add(""); //QUANDO SCRIVO UNA VARIABILE MI SERVE ANCHE UN VALORE: IN QUESTO CASO NE PASSO UNO "DUMMY"

        List<String> valuesConditional = new ArrayList<>();
        valuesConditional.add(fileNameDES);
        valuesConditional.add(testIf);     // prima inserisco l'espressione da valutare
        valuesConditional.add(retTrue);    // poi il risultato se l'espressione ritorna TRUE
        valuesConditional.add(retFalse);   // alla fine se ritorna FALSE



        MapVariabili.put(namedVariable, valuesVariable);
        MapConditional.put(namedVariable, valuesConditional);




    }

//  QUESTA FUNZIONE GESTISCE LE EQUAZIONI DIFFERENZIALI
    private static void handleDiffeq(Map<String, String> mapEquazioniDifferenziali, Map<String, String> mapEquazioniIniziali ,  Map<String, List<String>> mapParametri, Map<String, List<String>> mapCostanti, Map<String, String> mapIstanze, Node node, NodeList nodeList, int i, String fileNameDES, Map<String, String> container_map, String nameOFADirectory) {

    Node diffeqName = nodeList.item(i+1);
    String name = diffeqName.getTextContent();  // non mi dovrebbe servire, comunque lo salvo qui dentro


    //integralName (è cio' che va dentro la funzione der() di Modelica)
    Node integralName = nodeList.item(i+2);
    String integral = integralName.getTextContent().strip();


//  TO DO: DEVO INSERIRE L'IDENTIFICATORE OFA PRIMA DELLA VARIABILE
//         DEVO FORMATTARE IL NOME DELLA VARIABILE SECONDO LE REGOLE DI MODELICA

    integral = String.format("%s_%s", fileNameDES, integral);
    integral = FormatParValDef(FormatSqBrackValDef(FormatSignsValDef(FormatDashValDef(integral))));





    //dervName (è la parte destra dell'equazione differenziale in Modelica)
    Node unKnown = nodeList.item(i+3);
    String unKnownName = unKnown.getNodeName();

    switch (unKnownName) {

        case "initialval":

            //  DEVO GESTIRE UNA INITIAL EQUATION
            String initialVal = unKnown.getTextContent();

            handleInitialEquation(mapEquazioniIniziali, integral, initialVal);

        //  NON INSERISCO IL BREAK: GESTITO IL VALORE INIZIALE, POI DEVO PROEGUIRE...
            // ... MA DEVO PASSARE AL NODO SUCCESSIVO...
            unKnown = nodeList.item(i+4);

        case "dervname":

            String derv = FormatParValDef(FormatSqBrackValDef(FormatSignsValDef(FormatDashValDef(unKnown.getTextContent())))).strip();
            derv = ManageImport(derv, mapIstanze, fileNameDES, container_map, nameOFADirectory);

            mapEquazioniDifferenziali.put(integral,derv);


    }










    }

//  QUESTA FUNZIONE GESTISCE LE INITIAL CONDITION DELLE EQUAZIONI DIFFERENZIALI
    private static void handleInitialEquation(Map<String, String> mapEquazioniIniziali,  String integral, String initialVal) {

        mapEquazioniIniziali.put(integral, initialVal);



    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E SOSTITUISCE I SIMBOLI DI "+" E "-" CHE FANNO PARTE NON DELL'ESPRESSIONE ARITMETICA, MA DEL NOME DI UNA VARIABILE/PARAMETRO
    public static String FormatSignsValDef(String valdef) {

        valdef = valdef.replaceAll("-]", "minus]");
        valdef = valdef.replaceAll("\\+]", "plus]");



        char[] valDef2Arr = valdef.toCharArray();

        for (int j=0; j <= valDef2Arr.length-1; j++) {
            if (valDef2Arr[j] == '/') {
                char whatIsPrec = valDef2Arr[j-1];
                if (whatIsPrec != ' ') {// ALLORA FA PARTE DI UN NOME DI UNA VARIABILE/PARAMETRO: VA SOSTITUITO
                    valDef2Arr[j] = '_';
                }
            }
        }

        for (int j=0; j <= valDef2Arr.length-1; j++) {
            if (valDef2Arr[j] == '/') {
                char whatIsPrec = valDef2Arr[j-1];
                if (whatIsPrec != ' ') {// ALLORA FA PARTE DI UN NOME DI UNA VARIABILE/PARAMETRO: VA SOSTITUITO
                    valDef2Arr[j] = '_';
                }
            }
        }




        valdef = String.valueOf(valDef2Arr);
        return valdef;
    }


//  QUESTA FUNZIONE GESTISCE I SEGNI "+" E "-" ALL'INTERNO DI VALDEF
    public static String formatPlusMinus(String valdef) {

        valdef = valdef.replaceAll("-", "minus");
        valdef = valdef.replaceAll("\\+", "plus");


        return valdef;
    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E SOSTITUISCE IL SIMBOLO SPECIALE "-" CHE MODELICA NON GESTISCE
    public static String FormatDashValDef(String valdef) {

        boolean bool = false;       //by default considero che ci siano "." nel nome

        if (!valdef.contains(".")) {
            bool = true;
        }


        if (bool) { //NON CI SONO PUNTI NEL NOME

//      PER PRIMA COSA DEVO ACCERTARMI CHE I "-" (DASH) SIANO DEI MENO E NON DEI TRATTINI CHE COMPONGONO IL NOME DI UN QUALCHE MODULO

            List<Character> valDef2List = new ArrayList<>();
            for (char ch : valdef.toCharArray()) {

                valDef2List.add(ch);

            }

            for (int j = 0; j <= valDef2List.size() - 1; j++) {
                if (valDef2List.get(j).equals("_")) {
                    char whatIsPrec = valDef2List.get(j - 1);
                    if (whatIsPrec != ' ') {
                        valDef2List.set(j, 'm');
                        valDef2List.add(j + 1, 'i');
                        valDef2List.add(j + 1, 'n');
                        valDef2List.add(j + 1, 'u');
                        valDef2List.add(j + 1, 's');
                        j = j + 5;
                    }
                }
            }

/*        char[] valDef2Arr = valdef.toCharArray();

        for (int j=0; j <= valDef2Arr.length-1; j++) {
            if (valDef2Arr[j] == '-') {
                char whatIsPrec = valDef2Arr[j-1];
                if (whatIsPrec != ' ') {

                    valDef2Arr[j] = '_';
                }
            }
        }


        valdef = String.valueOf(valDef2Arr);
*/
            StringBuilder result = new StringBuilder(valDef2List.size());
            for (Character c : valDef2List) {
                result.append(c);
            }
            String toReturn = result.toString();


            return toReturn;

        } else {    //contiene dei "." (è possibile che si tratti di campi di moduli esterni)

            String[] valDef2Arr = valdef.split("\\s+");

            for (int t=0; t <= valDef2Arr.length-1; t++) {

                if (valDef2Arr[t].contains(".") && isField(valDef2Arr[t])) {

                    String[] splitsy = valDef2Arr[t].split("\\.");
                    String model = splitsy[0].replace("-", "_");
                    String field = formatPlusMinus(splitsy[1]);
                    valDef2Arr[t] = model + "." + field;

                }
            }


            String toReturn = String.join(" ",valDef2Arr);

            return toReturn;
        }


    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E SOSTITUISCE I SIMBOLI SPECIALI "(" ")" CHE MODELICA NON GESTISCE
    public static String FormatParValDef(String valdef) {

        char[] valdef2Arr = valdef.toCharArray();

        for (int p=0; p <= valdef2Arr.length-1; p++) {
            if (valdef2Arr[p] == '(') {
                char whatIsNext = valdef2Arr[p+1];
                if (whatIsNext == ' ') {} // è una parentesi aritmetica
                else{
                    valdef2Arr[p] = '_';
                }
            } else if (valdef2Arr[p] == ')') {
                char whatIsPrec = valdef2Arr[p-1];
                if (whatIsPrec == ' ') {} // è una parentesi matematica
                else{
                    valdef2Arr[p] = '_';
                }
            }
        }

        valdef = String.valueOf(valdef2Arr);

        return  valdef;


    }

//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF" E SOSTITUISCE I SIMBOLI SPECIALI "[" "]" CHE MODELICA NON GESTISCE
    public static String FormatSqBrackValDef(String valdef) {

        char[] valdef2Arr = valdef.toCharArray();

        for (int sb=0; sb <= valdef2Arr.length-1; sb++) {
            if (valdef2Arr[sb] == '[') {
                char whatIsNext = valdef2Arr[sb + 1];
                if (whatIsNext == ' ')
                {

                    // PER ORA NON LO TRADUCO CON NULLA, MA IN REALTA' QUELLO CHE VIENE TRA LE [ PARAM  ] E' L'ARGOMENTO SU CUI VIENE INVOCATA LA FUNZIONE CHE PRECEDE LA '['

                } else valdef2Arr[sb] = '_';

            } else if (valdef2Arr[sb] == ']') {
                char whatIsPrev = valdef2Arr[sb - 1];
                if (whatIsPrev == ' ') {

                    // PER ORA NON LO TRADUCO CON NULLA, MA IN REALTA' QUELLO CHE VIENE TRA LE [ PARAM  ] E' L'ARGOMENTO SU CUI VIENE INVOCATA LA FUNZIONE CHE PRECEDE LA '['

                } else valdef2Arr[sb] = '_';
            }
        }


        valdef = String.valueOf(valdef2Arr);



//      Provo qui a formattare le parentesi quadre [] che chiamano la funzione "curve" di interpolazione dei punti
        List<String> valdef2List = new ArrayList<>(Arrays.asList(valdef.split("\\s+")));

        while(valdef2List.contains("[")) {

            for (int i=0; i <= valdef2List.size()-1; i++) {
                if (valdef2List.get(i).equals("[")) {
                    int openPos = i;
                    int closePos = findClosingSBParen(valdef2List, i);

//                  SCORRO DALLA PARENTESI QUADRA DI APERTURA FINO A QUELLA DI CHIUSURA PER RENDERE TUTTO L'ARGOMENTO "XVAL"
                    StringBuilder sb = new StringBuilder("");
                    for (int j=openPos+1; j <= closePos-1; j++) {
                        sb.append(valdef2List.get(j));
                        valdef2List.set(j, "");     //tolgo ciò che prima era tra le []
                    }
                    valdef2List.set(closePos, "");  //tolgo la parentesi ]


                    String xVal = sb.toString();

//                  E' L'ELENCO DEI PUNTI (X,Y) GIA' NOTI (si trova subito prima della parentesi quadra di apertura)
                    String ybar = valdef2List.get(i-1);

                    valdef2List.set(openPos-1, ""); //ORA tolgo il nome dei Punti che vado a includere all'interno dell'argomento

                    String interpolation = String.format("InterpolateVector ( %s , %sPoints )", xVal, ybar);

                    valdef2List.set(openPos, interpolation);    //rimpiazzo la [ con la funzione di interpolazione e il suo argomento




                }
            }



        }






        String toReturn = String.join(" ", valdef2List);

        return toReturn;
    }


//  QUESTA FUNZIONE PRENDE IN INPUT LA STRINGA "VALDEF", LA FORMATTA CON I NOMI APPROPRIATI PER L'IMPORT E AGGIORNA LA LISTA DEI MODULI CHE DEVONO ESSERE INSERITI IN CIMA ALL'OFA
    public static String ManageImport(String valdef, Map instances, String identifier, Map container, String OFA ) {

        String[] valDefTokenized = valdef.split("\\s+");

        for (int t=0; t <= valDefTokenized.length-1; t++) {

            if (valDefTokenized[t].contains(".") && isField(valDefTokenized[t])) { // CI SARANNO DEGLI IMPORT, OPPURE ACCEDO ALL'OFA CORRENTE

                String possibleImport = valDefTokenized[t];

                int dotIndex = possibleImport.indexOf(".");

                char whatIsNext = possibleImport.charAt(dotIndex+1);

                if (Character.isLetter(whatIsNext) || whatIsNext == ('_') ) { // allora sto accedendo ad un campo

                    String[] splitsy = possibleImport.split("\\.");     // il 'dot' è un carattere speciale: devo fare l'escape

                    String model = splitsy[0]; // è il nome del modulo segnato sul file DES:
                    // devo rintracciare il suo OFA all'interno del Container:

                    // se è lo stesso, vuol dire che l'attributo dopo il punto si troverà all'interno del file che sto componendo ora
                    // altrimenti, devo importare il file ".mo" che il cui nome è l'OFA del modello indicato


                    // AL CAMPO DEVO ANTEPORRE IL NOME DEL MODELLO CON  "_": NOMEMODELLO_CAMPO
                    String field = splitsy[1];

                    field = formatPlusMinus(field);


                    String SecondOFA = null;

                    try {
                        SecondOFA = container.get(model).toString(); // il nome del model dovrebbe essere gia' ben formattato
                    } catch (NullPointerException npt) {

//                      NON DOVREBBE MAI ACCADERE: OGNI MODULO CONTENUTO ALL'INTERNO DELLA STRUCTURE
//                      MI ASPETTO CHE ABBIA IL SUO OFA, CALCOLATO ALL'INIZIO DEL PROCESSO DI TRADUZIONE

//                      AGGIUNGO TEMPORANEAMENTE QUESTA RIGA: POI LA DEVO ELIMINARE
                        SecondOFA = model;
                        //npt.printStackTrace();
                    }


//                  ORA: SE "OFA" (parametro) e "SecondOFA" coincidono, vuol dire che l'attributo cui sto accedendo si deve trovare
//                  proprio nell'OFA che sto componendo ora.
//                  ALTRIMENTI: il "SecondOFA" dev'essere importato all' inizio dell'OFA corrente

                    if (!OFA.equals(SecondOFA)) { // DEVO IMPORTARE

//                  QUANDO AGGIUNGO IL MODULO (OFA) DA IMPORTARE, NON GLI ACCODO QUI L'IDENTIFICATORE "mod": questo lo inserirò solamente a tempo di scrittura
                        instances.put(SecondOFA, model.toLowerCase()); // il nome dell'OFA è il riferimento per il modulo da importare, ma il nome dell'istanza rimane invariato (portato però a lowerCase)


//                  FORMATTO GIA' QUI LA SOTTO-STRIGA DI VALDEF
                        field = String.format("%s_%s", model, field);

//                      PRIMA...
                        //possibleImport = String.format("%s.%s", SecondOFA.toLowerCase(), field);

//                      DOPO...
                        possibleImport = String.format("%s.%s", model.toLowerCase(), field);

                        valDefTokenized[t] = possibleImport;
                    }

                    else if (OFA.equals(SecondOFA)) { // NON DEVO IMPORTARE

                        //DEVO TOGLIERE IL PUNTO E UNIRE IL NOME DEL MODULO E IL NOME DELL'ATTRIBUTO
                        possibleImport = String.format("%s_%s", model, field);

                        //DEVO AGGIORNARE LA STRINGA CONTENENTE LA PARTE CON IL PUNTO
                        valDefTokenized[t] = possibleImport;

                    }




                }

            } else if ( isField(valDefTokenized[t]) && valDefTokenized[t].length() > 1 && !( ListUnaryOperator.contains(valDefTokenized[t]) || ListBinaryOperator.contains(valDefTokenized[t])  || (ListFunctions.contains(valDefTokenized[t])) ) ) { //STO ACCEDENDO AI CAMPI DEL MODULO STESSO, QUINDI TROVERO' I CAMPI ALL'INTERNO DELL'OFA CORRENTE

                String operatore = valDefTokenized[t]; // SALVO QUI DENTRO L'OPERATORE SU CUI STO ITERANDO

                operatore = formatPlusMinus(operatore);

                operatore = String.format("%s_%s",identifier, operatore);

                valDefTokenized[t] = operatore;


            }
        }

        valdef = String.join(" ", valDefTokenized);

        return valdef;



    }


//  QUESTA FUNZIONE PRENDE IN INPUT UNA STRINGA E RITORNA VERO SSE IL TESTO CORRISPONDE A UN NUMERO (FLOAT, DOUBLE, INT...)
//  RITORNA FALSE SE: è UNA STRINGA, O UN OPERATORE MATEMATICO
    public static boolean isNumeric(String num) {

        if (num.length()>1) {
            try {
                double d = Double.parseDouble(num);
            } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException nfe) {
                return false;
            }

        }

        return false;
    }

//  QUI SCRIVO LA FUNZIONE CHE RITORNA TRUE SE LA STRINGA PASSATA è UN CAMPO DI UN MODULO DA IMPORTARE
    public static Boolean isField(String possibleField) {

        if (!isNumeric(possibleField)) return true;
        return false;
    }


//  QUI SCRIVO LA FUNZIONE CHE MI FORMATTA IN AUTOMATICO I NOMI DELLE VARIABILI/PARAMETRI/COSTANTI
    public static String ModelicaNameFormat(String name) {

        //name = name.replace("-","_");     //  I DASH NEI NOMI DELLE VARIABILI STANNO PER "MINUS"  -> LO RIMPIAZZO CON MINUS
        name = name.replace("-]", "minus]");
        name = name.replace("+]", "plus]");

        name = name.replaceAll("\\+", "plus");
        name = name.replaceAll("-", "minus");

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

    public static String ModelicaInferType(String name) {
        return ModelicaInferType(name, "");
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

        List<String> values = new ArrayList<>();
        values.add(identifier);
        values.add(paramVal);

        params.put(paramName, values);

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


        List<String> values = new ArrayList<>();
        values.add(identifier);
        values.add(constVal);

        consts.put(constName, values);


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
    public static void handleDefinition(Map vars, Map params, Map consts, Map instances, Map interpolation , Node nd, NodeList nList, int i, String identifier, Map container, String OFA, List Import) {

        Node nameNode = nList.item(i+1);
        String defName = nameNode.getTextContent().strip();


//      TO DO: FORMAT VAR/PAR NAME ACCORDING TO MODELICA RULE
//      PER SALVARE LA STRUTTURA, AGGIUNGO IL NOME DELLA CLASSE DAVANTI AL NOME



        //if (defName.equals("FA+GlucoseFraction")) {
        //    System.out.println("pippo");
        //}



        defName = ModelicaNameFormat(defName);  //PER ORA NON MODIFICA BENE I NOMI CONTENENTI GLI OPERATORI MATEMATICI

//      ATTENZIONE QUI: QUESTA OPERAZIONE LA STO ESEGUENDO NEL MENTRE SCRIVO (UNA PER UNA) LE VARIABILI E NON QUI,
// ALTRIMENTI L'IDENTIFICATORE SI RADDOPPIA
        //defName = String.format("%s_%s", identifier, defName);






        Node valNode = nList.item(i+2);
        String valDef = valNode.getTextContent().strip();

//       DELLE VOLTE IL NODO VAL CONTIENE LA PARTE DESTRA DELL'EQUAZIONE SU PIU' LINEE: LA RIPORTO SU UN'UNICA LINEA
        valDef = valDef.lines().collect(Collectors.joining(" "));

//      DEVO ASSICURARMI CHE I NOMI DELLE CLASSI RIFERITE SIANO COERENTI CON LA NOMENCLATURA DI MODELICA:
//      DEVO SOSTITUIRE I DASH (-) CON GLI UNDERSCORE (_) IFF NON SONO DEI MENO (-)
//      LA SOSTITUZIONE PUO' AVVENIRE PERCHE' I SIMBOLI MATEMATICI SONO SEMPRE PRECEDUTI E SEGUITI DA UN WHITESPACE (" ")



//      QUESTA PARTE LA DESTRUTTURO:

//        char[] valDef2Arr = valDef.toCharArray();
//
//        if (valDef.contains("-")) {
//            for (int j=0; j<valDef2Arr.length-1; j++) {
//                if (valDef2Arr[j] == '-') {
//                    char whatIsPrec = valDef2Arr[j-1]; //sono sicuro che nessun valDef inizi con un "-"
//
//                    if (whatIsPrec != ' ') { //non è un meno -> devo sosituire il dash con un underscore
//                        valDef2Arr[j] = '_';
//                    }
//                }
//            }
//
//        }

        if (valDef.contains("-")) {

            valDef = FormatDashValDef(valDef);

        }



        // TOLGO I SEGNI "+", "-" E "/" CHE NON SONO SEGNI ALGEBRICI
        // NON FACCIO NESSUN CONTROLLO: CHIAMO LA FUNZIONE E BASTA
        valDef = FormatSignsValDef(valDef);


//      DEVO ASSICURARMI CHE LE PARENTESI NELLA VALDEF SIANO DELLE PARENTESI CHE INDICHINO LA PRECEDENZA E NON ALTRO:
//      SE TOVO DELLE PARENTESI CHE INVECE SONO PARTE DEL NOME DI UNA CLASSE (MODULO) O ATTRIBUTO (CAMPO) -> DEVO SOSTITUIRLA CON UN UNDERSCORE


//        if (valDef.contains("(")) {
//            for (int p=0; p<valDef.length(); p++) {
//                if (valDef2Arr[p] == '(') { // mi assicuro che non siano parentesi matematiche
//                    if (valDef2Arr[p+1] == ' ') {}
//                    else {
//                        valDef2Arr[p] = '_';
//                    }
//                } else if (valDef2Arr[p] == ')') {
//                    if (valDef2Arr[p-1] == ' ') {}
//                    else {
//                        valDef2Arr[p] = '_';
//                    }
//                }
//            }
//        }
//      RIPORTO TUTTO IN FORMA DI STRINGA
//        valDef = String.valueOf(valDef2Arr);

        if (valDef.contains("(")) {
            valDef = FormatParValDef(valDef);
        }

//      IMPORANTE: MANCA LA GESTIONE DELLA CHIAMATA A FUNZIONE (curve).
//      PER ORA GESTISCO SOLAMENTE LE "[]" CHE FANNO PARTE DI NOMI

        if (valDef.contains("[")) {
            valDef = FormatSqBrackValDef(valDef);
        }




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



//      QUESTA PARTE LA RISTRUTTURO (RIGHE 478-549)

//        if (valDef.contains(".")) {
//            for (int j=0; j<= rightSideToken.length-1; j++) {
//                if (rightSideToken[j].contains(".") && isField(rightSideToken[j])) {
//
//                    int dotIndex = rightSideToken[j].indexOf(".");
//
//                    if (Character.isLetter(rightSideToken[j].charAt(dotIndex+1))) { //allora sto accedendo al campo di un altro modello
//
//                        String containsDot = rightSideToken[j];
//                        String[] splitsy = containsDot.split("\\.");    //il punto è un carattere speciale: devo fare l'escape
//
//                        String toDeclare = splitsy[0];
//                        String field = splitsy[1];
//
////                  ORA DEVO MAPPARE LA CLASSE (MODELLO) DA IMPORTARE: NON è QUELLA CHE TROVO, MA QUELLA CHE LA CONTIENE
//
//
//
////                  QUI INSERISCO UN TRY CATCH TEMPORANEo: STO PROVANDO SU UNA SOLA SOTTOCARTELLA E NON HO TRADOTTO TUTIT I MODELLI:
//                        //QUELLI NON TRADOTTO LI GESTISCO SEPARATAMENTE
//
//
////                  PRIMA ERA: String OFAtoDeclare = container.get(toDeclare).toString();
//
//                        String OFAtoDeclare = null;
//
//                        try {
//                            OFAtoDeclare = container.get(toDeclare).toString();
//                        } catch (NullPointerException npt) {
//
//                            OFAtoDeclare = toDeclare;
//                        }
//
////                  AGGIORNO L'ELENCO DELLE ISTANZE DA CREARE:
////                  LO AGGIORNO SOLO QUANDO L'OFA DI "toDeclare" non coincide con quello del modello DES attuale
////                  (altrimenti non è un vero import: i campi cui sto accedendo sono già nell'OFA)
//
//
//                        String thisOFA = OFA;
//
//                        if (!thisOFA.equals(OFAtoDeclare)) { //devo importare:
////                          RICORDO CHE DEVO AGGIUNGERE _mod ALLA FINE DEL NOME DEL MODULO
////                          E IL NOME DELL'IDENTIFICATORE DAVANTI AL NOME DEL CAMPO CHE IMPORTO
//
//                            // creo la riga per l'import di un modello esterno all'OFA corrente
//                            //instances.add(String.format("%s_mod  %s_%s;", OFAtoDeclare, toDeclare, Character.toString(toDeclare.charAt(0)).toLowerCase() + toDeclare.substring(1))); Ora penso sia superfluo questo approccio
//                            instances.add(String.format("%s_mod  %s;", OFAtoDeclare, toDeclare));
//
//                            //rightSideToken[j] = Character.toString(rightSideToken[j].charAt(0)).toLowerCase() + rightSideToken[j].substring(1); Ora penso sia superfluo questo approccio
//
//                            valDef = String.join(" ", rightSideToken);
//
//                        } else {    //gli OFA coincidono -> devo accedere ad un campo della classe in cui mi trovo: non ho bisogno di "."
//
//                            rightSideToken[j] = String.format("%s_%s;", toDeclare, field);
//
//                            valDef = String.join(" ", rightSideToken);
//
//
//
//
//                        }
//
//
//
//                    }
//
//                }
//            }
//
//
//        }

        //if (valDef.contains(".")) {
        valDef = ManageImport(valDef, instances, identifier, container, OFA);
        //}


        valDef = fixUnaryOperator(valDef);
        valDef = fixBinaryOperator(valDef);


//      DEVO CONTROLLARE SE SULLA RIGA SONO PRESENTI DEI "MIN" (TUTTO IN MAIUSCOLO). INDICANO 
        if (valDef.contains("MIN")) {
            valDef = handleBinaryMIN(valDef);
        }


        if (valDef.contains("MAX")) {
            valDef = handleBinaryMAX(valDef);
        }

        if (valDef.contains("EXP")) {
            valDef = handleUnaryEXP(valDef);
        }

        if (valDef.contains("INVERT")) {
            valDef = handleUnaryINVERT(valDef);
        }

        if (valDef.contains("LOG")) {
            valDef = handleLOGN(valDef);
        }

        if (valDef.contains("LOG10")) {
            valDef = handleLOG10(valDef);
        }

        if (valDef.contains("SQRT")) {
            valDef = handleSQRT(valDef);
        }

        if (valDef.contains("SIN")) {
            valDef = handleSIN(valDef);
        }

        if (valDef.contains("COS")) {
            valDef = handleCOS(valDef);
        }

        if (valDef.contains("TAN")) {
            valDef = handleTAN(valDef);
        }

        if (valDef.contains("SINDEG")) {

            Import.add("Modelica.SIunits.Conversions.*");
            valDef = handleSINDEG(valDef);
        }

        if (valDef.contains("COSDEG")) {

            Import.add("Modelica.SIunits.Conversions.*");
            valDef = handleCOSDEG(valDef);
        }

        if (valDef.contains("TANDEG")) {

            Import.add("Modelica.SIunits.Conversions.*");
            valDef = handleTANDEG(valDef);
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



        List<String> values = new ArrayList<>();
        values.add(identifier);
        values.add(valDef);

        if (valDef.contains("InterpolateVector")) {

            interpolation.put(defName, values);


        } else {




            // gestisco l'inserimento del "left side of equation" in base al campo "undefined"
            if (undefined) {
                params.put(defName, values);
                //params.put(defName, valDef);  QUESTO C'ERA QUANDO NON STAVO ANCORA LAVORANDO CON List<String> come "value" nelle HashMap
            } else {
                vars.put(defName, values);
                //vars.put(defName, valDef);    ...GUARDA LA RIGA SOPRA...
            }
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

//      RIDIRIGO TEMPORANEAMENTE L'OUTPUT
//        String destinationPath = new String("/Users/arl3cchino/IdeaProjects/XmlModelica/traduzioneStruttura");
        String destinationPath = new String("/Users/arl3cchino/IdeaProjects/XmlModelica/traduzioneStrutturaNew");


        //MAC VERSION
//      RIDIRIGO TEMPORANEAMENTE L'OUTPUT
//        Path structuresPath = Paths.get("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateBis");
        Path structuresPath = Paths.get("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateTris");

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
//      RIDIRIGO TEMPORANEAMENTE L'OUTPUT
//        Path dirPath = Paths.get("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateBis/");
        Path dirPath = Paths.get("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateTris/");


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


//              RIDIRIGO TEMPORANEAMENTE L'OUTPUT
//                String newPath = directory.replace("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateBis/", "/Users/arl3cchino/IdeaProjects/XmlModelica/traduzioneStruttura/");
                String newPath = directory.replace("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicateTris/", "/Users/arl3cchino/IdeaProjects/XmlModelica/traduzioneStrutturaNew/");

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

//
                    List<String> Instances = new ArrayList<>();
                    List<String> Import = new ArrayList<>();


                    Map<String,String> MapIstanze = new HashMap<>();


                    Map<String, List<String>> MapVariabili = new HashMap<>();
                    Map<String, List<String>> MapCostanti = new HashMap<>();
                    Map<String, List<String>> MapParametri = new HashMap<>();

                    Map<String, List<String>> MapConditional = new HashMap<>();

                    Map<String, String> MapCurve = new HashMap<>();
                    Map<String, String> MapInterpolation = new HashMap<>();


                    Map<String, String> MapEquazioniDifferenziali = new HashMap<>();
                    Map<String, String> MapEquazioniIniziali = new HashMap<>();
                    Map<String, String> MapWhiteNoises = new HashMap<>();



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

                                    //System.out.println(i);
                                    if (node.getNodeName().equals("parm")) {
                                        handleParameter(MapParametri, node, nodeList, i, fileNameDES);
                                    } else if (node.getNodeName().equals("constant")) {
                                        handleConstant(MapCostanti, node, nodeList, i, fileNameDES);
                                    } else if (node.getNodeName().equals("timervar")) {
                                        handleTimerVar(MapVariabili, node, nodeList, i, fileNameDES);
                                    } else if (node.getNodeName().equals("whitenoise")) {
                                        handleWhiteNoise(MapWhiteNoises, node, nodeList, i, fileNameDES);
                                    } else if (node.getNodeName().equals("def")) {
                                        handleDefinition(MapVariabili, MapParametri, MapCostanti, MapIstanze, MapInterpolation, node, nodeList, i, fileNameDES, container_Map, nameOFADirectory, Import);
                                    } else if (node.getNodeName().equals("diffeq")) {
                                        handleDiffeq(MapEquazioniDifferenziali, MapEquazioniIniziali, MapParametri, MapCostanti, MapIstanze, node, nodeList, i, fileNameDES, container_Map, nameOFADirectory);
                                    } else if (node.getNodeName().equals("conditional")) {
                                        handleConditional(MapConditional, MapVariabili, MapIstanze, node, nodeList, i, fileNameDES, container_Map, nameOFADirectory);
                                    } else if (node.getNodeName().equals("curve")) {
                                        handleCurve(MapCurve, node, nodeList, i, fileNameDES, container_Map, nameOFADirectory);
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

                    List<String> CleanImport = new ArrayList<>(Import.stream().distinct().collect(Collectors.toList()));

                    for (int i=0; i <= CleanImport.size()-1; i++) {
                        fwOFA.write(String.format("import %s;", CleanImport.get(i)));
                    }

                    //int numParams = MapParametri.size();
                    //int numConsts = MapCostanti.size();

//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");


                    for (Map.Entry<String, String> entry : MapIstanze.entrySet()) {

                        String OFAname = entry.getKey();
                        String instanceName = entry.getValue();



                        String toWrite = String.format("%s_mod %s;\n", OFAname, instanceName);

                        fwOFA.write(toWrite);

                    }


//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");





                    for (Map.Entry<String, List<String>> entry : MapCostanti.entrySet()) {

                        String constName = entry.getKey();
                        String nameDesModule = entry.getValue().get(0); // al primo indice metto il nome del modulo des
                        String constVal = entry.getValue().get(1); // al secondo indice metto il valore
                        String type = ModelicaInferType(constName, constVal);

                        String toWrite = String.format("constant %s %s_%s = %s;\n", type, nameDesModule, constName, constVal);

                        fwOFA.write(toWrite);



                    }

//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");

                    for (Map.Entry<String, List<String>> entry : MapParametri.entrySet()) {

                        String paramName = entry.getKey();
                        String nameDesModule = entry.getValue().get(0);
                        String paramVal = entry.getValue().get(1);
                        String type = ModelicaInferType(paramName, paramVal);

                        String toWrite = String.format("parameter %s %s_%s = %s;\n", type, nameDesModule, paramName, paramVal);

                        fwOFA.write(toWrite);


                    }


//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");



                    for (Map.Entry<String, List<String>> entry : MapVariabili.entrySet()) {

                        String varName = entry.getKey();
                        String nameDesModule = entry.getValue().get(0);
                        String varVal = entry.getValue().get(1);
                        String type = ModelicaInferType(varName, varVal);


                        String toWrite = null;
                        if (varVal.equals("")){

                             toWrite = String.format("%s %s_%s ;\n", type, nameDesModule, varName);

                        } else {

                             toWrite = String.format("%s %s_%s = %s;\n", type, nameDesModule, varName, varVal);

                        }



                        fwOFA.write(toWrite);


                    }


//                  AGGIUNGO LE MATRICI PER LE CURVE:
                    if (!MapCurve.isEmpty()) {

                        for (Map.Entry<String, String> entry : MapCurve.entrySet()) {

                            String varName = entry.getKey();
                            String matrix = entry.getValue();
                            String type = ModelicaInferType(varName);

                            String toWrite = String.format("%s %sPoints[:,2] = %s;\n", type, varName, matrix);

                            fwOFA.write(toWrite);


                        }

                    }



//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");



//                  SE PRESENTI, QUI SCRIVO LE PARTE DEGLI "ALGORITHM" (I BLOCCHI "CONDITIONAL" DEI FILE DES)

                    if (!MapConditional.isEmpty()) {

                        for (Map.Entry<String, List<String>> entry : MapConditional.entrySet()) {

                            String varName = entry.getKey();
                            String nameDesModule = entry.getValue().get(0);
                            String testString = entry.getValue().get(1);      // è l' if-test da eseguire
                            String trueString = entry.getValue().get(2);      // è il valore da assegnare alla variabile se il test ritorna True
                            String falseString = entry.getValue().get(3);     // è il valore da assegnare alla variabile se il test ritorna False

                            String type = ModelicaInferType(varName);

                            varName = nameDesModule + "_" + varName;


//                      NELLA SEZIONE "ALGORITMO"...
                            String toWriteAL = String.format("algorithm\n if ( %s )\n then %s := %s; \nelse %s := %s;\nend if;\n", testString, varName, trueString, varName, falseString);


//                      ... O UNA SEMPLICE IF EXPRESSION ?
                            //String toWriteIE = String.format("%s %s = if ( %s ) then %s else %s", type, varName, testString, trueString, falseString);

                            fwOFA.write(toWriteAL);


                        }
                    }



//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");



//                  PRIMA DI SCRIVERE LE CONDIZIONI INIZIALI NELL'OFA INSERISCO LA STRINGA "initial equation"
                    fwOFA.write("initial equation\n");


                    for (Map.Entry<String, String> entry : MapEquazioniIniziali.entrySet()) {

                        String integralName = entry.getKey();
                        String initialValue = entry.getValue();


                        String toWrite = String.format("%s = %s;\n", integralName, initialValue);

                        fwOFA.write(toWrite);


                    }


//                  AGGIUNGO UN PO' DI FORMATTAZIONE AL TESTO FINALE
                    fwOFA.write("\n");







//                  PRIMA DI SCRIVERE LE EQUAZIONI DIFFERENZIALI NELL'OFA INSERISCO LA STRINGA "equation"
                    fwOFA.write("equation\n");


                    for (Map.Entry<String, String> entry : MapEquazioniDifferenziali.entrySet()) {

                        String integralName = entry.getKey();
                        String dervName = entry.getValue();


                        String toWrite = String.format("der(%s) =  %s;\n", integralName, dervName);

                        fwOFA.write(toWrite);


                    }





                    fwOFA.write(String.format("\nend %s_mod;", nameOFADirectory));
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