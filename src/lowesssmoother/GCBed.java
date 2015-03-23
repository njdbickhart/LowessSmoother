/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lowesssmoother;

import file.BedAbstract;

/**
 *
 * @author bickhart
 */
public class GCBed extends BedAbstract{
    public final double gc;
    public double initialValue = 0.0d;
    public double normValue = 0.0d;
    
    public GCBed(String chr, String start, String end, double gc){
        this.chr = chr;
        this.start = Integer.valueOf(start);
        this.end = Integer.valueOf(end);
        this.gc = gc;
    }
    public GCBed(String chr, int start, int end, double gc){
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.gc = gc;
    }
    public GCBed(GCBed bed, double value){
        this.chr = bed.chr;
        this.start = bed.start;
        this.end = bed.end;
        this.gc = bed.gc;
        this.initialValue = value;
    }
    
    @Override
    public int compareTo(BedAbstract t) {
        if(this.start > t.Start()){
            return 1;
        }else if(this.start == t.Start()){
            return 0;
        }else{
            return -1;
        }
    }
    
    @Override
    public String toString(){
        return String.format("%s\t%d\t%d\t%.4f", this.chr, this.start, this.end, this.gc);
    }
}
