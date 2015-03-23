/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lowesssmoother;

import file.BedAbstract;
import file.BedMap;
import file.BedSimple;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author bickhart
 */
public class RefFasta extends FastaFile{
    private BedMap coordIntersect = new BedMap();
    
    @Override
    public void readFasta(String file, String bedfile){
        // create the bedfile coordinates
        ArrayList<Character> seq = new ArrayList<Character>();
        seq.ensureCapacity(100000000);
        String line, chrname;
        try(BufferedReader input = Files.newBufferedReader(Paths.get(bedfile), Charset.defaultCharset())){
            while((line = input.readLine()) != null){
                line = line.trim();
                String[] segs = line.split("[\t:-]");
                //segs[0] = segs[0].replaceAll("BTA", "chr");
                this.coordIntersect.addBedData(new BedSimple(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2])));
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        
        // Populate the data array with the necessary values
        
        line = chrname = "";
        int counter = 0;
        try(BufferedReader input = Files.newBufferedReader(Paths.get(file), Charset.defaultCharset())){
            while((line = input.readLine()) != null){
                line = line.trim();
                if (line.startsWith(">") && counter != 0){
                    System.out.println("SubStringing " + chrname);
                    this.subStringOutValues(chrname, seq);                    
                    seq.clear();
                    System.gc();
                    System.out.println("[DOC CHR]Working on " + line);
                    chrname = line.replaceAll(">", "");
                } else if(line.startsWith(">") && counter == 0){
                    System.out.println("[DOC CHR]Working on " + line);
                    chrname = line.replaceAll(">", "");
                } else{
                    char[] holder = line.toCharArray();
                    for (char ch : holder){
                        seq.add(ch);
                    }
                }
                counter++;
            }
        }catch(IOException ex){
            System.err.println("Error with file input!");
            ex.printStackTrace();
        }
        
        
        if(!seq.isEmpty()){
            this.subStringOutValues(chrname, seq);
            seq = null;
            System.gc();
        }
        
        
    }
    
    private void subStringOutValues(String chr, ArrayList<Character> seq){
        for(BedAbstract b : this.coordIntersect.getSortedBedAbstractList(chr)){
            StringBuilder s = new StringBuilder();
            if(b.End() > seq.size() && b.Start() < seq.size()){
                b.setEnd(seq.size());
            }else if(b.End() > seq.size()){
                continue;
            }
            for(char c : seq.subList(b.Start() -1, b.End() -1)){
                s.append(c);
            }
            String subseq = s.toString();
            double gc = this.getGCPerc(subseq);
            this.addBedData(new GCBed(chr, b.Start(), b.End(), gc));
        }
    }
}
