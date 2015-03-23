/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lowesssmoother;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author bickhart
 */
public class gcNormList {
    private ConcurrentHashMap<Double, gcNormWin> gcWins;
    
    public gcNormList(){
        this.gcWins = new ConcurrentHashMap<Double, gcNormWin>();
        initializeVals();
    }
    
    public double normVal(float gc){
        double gcd = roundDbl(gc);
        if(this.gcWins.containsKey(gcd)){
            return this.gcWins.get(gcd).getNormVal();
        }else{
            System.out.println("Error! " + gc + " " + gcd);
            return -0.123d;
        }
    }
    public double normVal(double gc){
        double gcd = roundDbl(gc);
        if(this.gcWins.containsKey(gcd)){
            return this.gcWins.get(gcd).getNormVal();
        }else{
            System.out.println("Error! " + gc + " " + gcd);
            return -0.123d;
        }
    }
    private void initializeVals(){
        for(double x = 0.000d; x < 1.001d; x += 0.001d){
            double gcd = roundDbl(x);
            this.gcWins.put(gcd, new gcNormWin(gcd));
        }
    }
    public void setVal(double gc, double val){
        if(this.gcWins.containsKey(gc)){
            this.gcWins.get(gc).setVal(val);
            this.gcWins.get(gc).setUsed();
        }else{
            System.out.println("[gcNormList] accessing uninitialized value! " + gc);
        }
    }
    public void setVal(double gc, double val, int num){
        if(this.gcWins.containsKey(gc)){
            this.gcWins.get(gc).setVal(val, num);
            this.gcWins.get(gc).setUsed();
        }else{
            System.out.println("[gcNormList] accessing uninitialized value! " + gc);
        }
    }
    public boolean isUsed(double gc){
        if(this.gcWins.containsKey(gc)){
            return this.gcWins.get(gc).isUsed();
        }else{
            return false;
        }
    }
    /*public static double roundDbl(float gc){
    double gcd = new FloatingDecimal(gc).doubleValue();
    return (double) Math.round(gcd * 1000) / 1000;
    }*/
    public static double roundDbl(double gc){
        return (double) Math.round(gc * 1000) / 1000;
    }
    public gcNormWin getValue(double gc){
        double gcd = roundDbl(gc);
        if(this.gcWins.containsKey(gcd)){
            return this.gcWins.get(gcd);
        }else{
            return new gcNormWin(gcd, -0.123);
        }
    }
    public int getNumWins(double gc){
        double gcd = roundDbl(gc);
        if(this.gcWins.containsKey(gcd)){
            return this.gcWins.get(gcd).numWins();
        }else{
            return 0;
        }
    }
    
    public ArrayList<Double> normalizeLowess(ArrayList<GCBed> data, String animal){
        ArrayList<Double> vals = rotateThroughEntries(data);
        BedStdAvgMed prenorm = this.CreateStats(vals);
        double avg = prenorm.getAvg();
        System.out.println("Prenormalization stats for animal: " + animal + ". Avg: " + prenorm.getAvg() + " Median: " + prenorm.getMedian() + " Stdev: " + prenorm.getStdev());
        vals.clear();
        for(GCBed bed : data){
            double hits = bed.initialValue;
            double nvalue = this.normVal(bed.gc);
            //TODO: I am going to hard code male normalization of the X chromosome, but I need to change this in the future
            double norm = 0.0d;
            if(bed.Chr().equals("chrX")){
                nvalue /= 2;
                norm = hits - (nvalue - avg);
            }else if(bed.Chr().startsWith("chrU")){
                continue;
            }else{
                norm = hits - (nvalue - avg);
            }
            if(norm < 0){
                norm = 0.0d;
            }
            bed.normValue = norm;
            
            if(!bed.Chr().startsWith("chrU")){
                vals.add(norm);
            }
        }
        BedStdAvgMed postnorm = this.CreateStats(vals);
        System.out.println("Postnormalization stats for animal: " + animal + ". Avg: " + postnorm.getAvg() + " Median: " + postnorm.getMedian() + " Stdev: " + postnorm.getStdev());
        return vals;
    }
    
    public void fillValues(ArrayList<GCBed> windows){
        for(double x : this.gcWins.keySet()){
            ArrayList<Double> gcvals = this.rotateThroughEntries(windows, x);
            double avg = StdevAvg.convertDblAvg(gcvals);
            if(gcvals.size() > 2){
                double median = Median.DMedian(gcvals);
                double stdev = StdevAvg.stdevDBL(gcvals);
                
                if(avg - (2 * stdev) < 0){
                    System.out.println("GC value: " + x + " was too heavy with a median of : " + median + " and a stdev of " + stdev);
                    double upper = Median.DUpperQuintile(gcvals);
                    gcvals = this.removeThreshold(upper, gcvals);
                    avg = StdevAvg.convertDblAvg(gcvals);
                }
            }
            
            
            this.setVal(x, avg, gcvals.size());
        }
    }
    private ArrayList<Double> removeThreshold(double threshold, ArrayList<Double> values){
        ArrayList<Double> holder = new ArrayList<>();
        for(int x = 0; x < values.size(); x++){
            double val = values.get(x);
            if(val < threshold){
                holder.add(val);
            }
        }
        return holder;
    }
    
    private BedStdAvgMed CreateStats(ArrayList<Double> values) {
        double avg = StdevAvg.convertDblAvg(values);
        double median = Median.DMedian(values);
        double stdev = StdevAvg.stdevDBL(avg, values);
        return new BedStdAvgMed(avg, median, stdev);
        
    }
    
    private ArrayList<Double> rotateThroughEntries(ArrayList<GCBed> windows, double gc) {
        ArrayList<Double> values = new ArrayList<Double>();
            for(GCBed bed : windows){
                if(bed.gc == this.roundDbl(gc)){
                    values.add(bed.initialValue);
                    if(Double.isNaN(bed.initialValue)){
                        System.out.println(bed.toString() + "is NaN theadwinstats");
                    }
                }
            }
        return values;
    }
    private ArrayList<Double> rotateThroughEntries(ArrayList<GCBed> windows) {
        ArrayList<Double> values = new ArrayList<Double>();
            for(GCBed bed : windows){
                values.add(bed.initialValue);
                if(Double.isNaN(bed.initialValue)){
                    System.out.println(bed.toString() + "is NaN theadwinstats");
                }
            }
        return values;
    }
}
