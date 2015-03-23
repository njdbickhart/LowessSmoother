/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lowesssmoother;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
public class LowessSmoother {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Retrieve and parse the command line options
        ParseCommandLine cmd = new ParseCommandLine(args);
        
        // Read in the fasta
        FastaFile fasta = null;
        if(cmd.isRefGen){
            // if fasta list, calculate the gc coords
            fasta = new RefFasta();
            fasta.readFasta(cmd.refgenome, cmd.coordbed);
        }else{
            // if coordinate list, calculate the gc coords from a reference genome
            fasta = new DeNovoFasta();
            fasta.readFasta(cmd.fastafile, cmd.coordbed);
        }
        // Take the windows and assign count values per animal
        HashMap<String, ArrayList<GCBed>> windows = createBeds(cmd.inputMat, fasta.getDataArray());
        HashMap<String, gcNormList> gcnormers = new HashMap<>();
        
        for(String an : windows.keySet()){
            gcnormers.put(an, new gcNormList());
            gcnormers.get(an).fillValues(windows.get(an));
            gcnormers.put(an, distributeGCVals(gcnormers.get(an)));
            
            gcnormers.get(an).normalizeLowess(windows.get(an), an);
        }
        
        // Print out results
        try(BufferedWriter output = Files.newBufferedWriter(Paths.get(cmd.outfile), Charset.defaultCharset())){
            String n = System.lineSeparator();
            output.write("chr\tstart\tend\tgc");
            ArrayList<String> list = new ArrayList<String>(windows.keySet());
            for(String an : list){
                output.write("\t" + an);
            }
            output.write(n);
            
            for(int x = 0; x < fasta.getDataArray().size(); x++){
                output.write(fasta.getDataArray().get(x).toString());
                for(String an : list){
                    GCBed working = windows.get(an).get(x);
                    output.write(String.format("\t%.4f", working.normValue));
                }
                output.write(n);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    private static HashMap<String, ArrayList<GCBed>> createBeds(String datafile, ArrayList<GCBed> beds){
        HashMap<String, ArrayList<GCBed>> windows = new HashMap<>();
        try(BufferedReader input = Files.newBufferedReader(Paths.get(datafile), Charset.defaultCharset())){
            String line = input.readLine();
            line = line.trim();
            int arrayIt = 0;
            String[] header = line.split("\t");
            
            for(int x = 1; x < header.length; x++){
                windows.put(header[x], new ArrayList<GCBed>());
            }
            
            while((line = input.readLine()) != null){
                line = line.trim();
                String[] segs = line.split("\t");
                if(arrayIt >= beds.size()){
                    System.out.println("Iterator over length for chr: " + segs[0]);
                    continue;
                }
                for(int x = 1; x < segs.length; x++){
                    windows.get(header[x]).add(new GCBed(beds.get(arrayIt), Double.valueOf(segs[x])));
                }
                arrayIt++;
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return windows;
    }
    
    private static gcNormList distributeGCVals(gcNormList list){
        double lastval;
        
        // Get average number of window values per GC entry
        ArrayList<Integer> winvals = new ArrayList<Integer>();
        for(double i = 0.000d; i < 1.000d; i += 0.001d){
            winvals.add(list.getNumWins(i));
        }
        int avgwins = StdevAvg.IntAvg(winvals);
        
        // Distribute to lower values if possible
        for(double i = 0.000d; i < 1.000d; i += 0.001d){
            double current = gcNormList.roundDbl(i);
            if(!list.isUsed(current)){
                continue;
            }else if(list.getNumWins(i) < avgwins / 5){
                continue;
            }
            lastval = list.normVal(current);
            if(Double.isNaN(lastval)){
                System.out.println( current + "is NaN distributevals");
            }
            for(double j = current - 0.001d; j >= 0.000d; j -= 0.001d){
                double test = gcNormList.roundDbl(j);
                if(!list.isUsed(test) || list.getNumWins(j) < avgwins / 5){
                    list.setVal(test, lastval, list.getNumWins(i));
                }
            }
        }
        
        // Now get the higher values that were likely missed
        for(double i = 0.998d; i > 0.000d; i -= 0.001d){
            double current = gcNormList.roundDbl(i);
            if(!list.isUsed(current)){
                continue;
            }else if(list.getNumWins(i) < avgwins / 5){
                continue;
            }
            lastval = list.normVal(current);
            if(Double.isNaN(lastval)){
                System.out.println( current + "is NaN distribute vals");
            }
            for(double j = current + 0.001d; j < 1.000d; j += 0.001d){
                double test = gcNormList.roundDbl(j);
                if(!list.isUsed(test) || list.getNumWins(j) < avgwins / 5){
                    list.setVal(test, lastval, list.getNumWins(i));
                }
            }
        }
        return list;
    }
}
