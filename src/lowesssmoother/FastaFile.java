/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lowesssmoother;

import file.BedAbstract;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author derek.bickhart
 */
public abstract class FastaFile{

    protected ArrayList<GCBed> data = new ArrayList<>();
    
    
    public abstract void readFasta(String file, String bedfile);
    
    public ArrayList<GCBed> getDataArray(){
        return this.data;
    }
    
    protected double getGCPerc(String seq){
        double gc = 0.0d;
        int n = 0, gcnus = 0;
        char[] proc = seq.toCharArray();
        for(int x = 0; x < proc.length; x++){
            switch(proc[x]){
                case 'N' :
                case 'n' :
                    n++;
                    break;
                case 'G' :
                case 'g' :
                case 'C' :
                case 'c' :
                    gcnus++;
                    break;
            }
        }
        gc = (double) gcnus / (proc.length - n);
        return gc;
    }
    
    protected void addBedData(GCBed bed){
        data.add(bed);
    }
}
