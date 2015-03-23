/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lowesssmoother;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author bickhart
 */
public class DeNovoFasta extends FastaFile{

    
    @Override
    public void readFasta(String file, String bedfile){
        // Populate the data array with the necessary values
        try(BufferedReader input = Files.newBufferedReader(Paths.get(file), Charset.defaultCharset())){
            String header, seq;
            while((header = input.readLine()) != null){
                header = header.trim();
                header = header.replaceFirst(">", "");
                String[] segs = header.split("[\\|,;]");
                seq = input.readLine();
                seq = seq.trim();
                int end = seq.length();
                double gc = this.getGCPerc(seq);
                
                this.addBedData(new GCBed(segs[segs.length -2], 1, end, gc));
            }
        }catch(IOException ex){
            System.err.println("Error with file input!");
            ex.printStackTrace();
        }
    }
    
}
