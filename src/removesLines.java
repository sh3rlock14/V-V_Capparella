import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class removesLines {

    void delete(String filename, int startline, int numlines) throws IOException {

        try{

            BufferedReader br = new BufferedReader(new FileReader(filename));

            //String buffer to store contents of the file
            StringBuffer sb = new StringBuffer("");

            //Keep track of the line number
            int linenumber = 1;
            String line;

            while ((line=br.readLine()) != null) {

                //Store each valid line in the string buffer
                if (linenumber < startline || linenumber >= startline+numlines)
                    sb.append(line+"\n");
                linenumber++;

            }
            if (startline + numlines > linenumber)
                System.out.println(filename);
                System.out.println("EOF reached");
            br.close();

            FileWriter fw = new FileWriter(new File(filename));
            //Write the entire string buffer into the file
            fw.write(sb.toString());
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static int countLineNew(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));

        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                //bail out if nothing to read
                return 0;
            }

            int count = 0;
            while (readChars == 1024) {
                for (int i=0; i< 1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                //System.out.println(readChars);
                for (int i=0; i<readChars; ++i) {
                        if (c[i] == '\n') {
                            ++count;
                        }
            }
            readChars = is.read(c);
        }
        return count == 0 ? 1 : count;
    } finally {
            is.close();
        }
        }



    public static void main(String[] args) throws IOException {

        Path structuresPath = Paths.get("/Users/arl3cchino/Documents/HumMod-hummod-standalone-106f109/StructureDuplicate");

        try (Stream<Path> walk = Files.walk(Paths.get(structuresPath.toString()))) {

            List<String> DESfiles = walk.map(x -> x.toString()).filter(f -> f.endsWith("DES")).collect(Collectors.toList());

            DESfiles.forEach(file -> {




                int startLine = 1;
                int numLines = 8;

                removesLines now = new removesLines();
                try {
                    now.delete(file, startLine, numLines);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    int lenFile = countLineNew(file);
                    int lastLine = 1;
                    now.delete(file,lenFile,numLines);


                } catch (IOException e) {
                    e.printStackTrace();
                }


            });

        }
    }

}
