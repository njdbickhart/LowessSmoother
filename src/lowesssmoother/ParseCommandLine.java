/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lowesssmoother;

/**
 *
 * @author bickhart
 */
public class ParseCommandLine {
    private final String nl = System.lineSeparator();
    public String Usage = "LowessSmoother -i <input data matrix> [-b <ref genome coords> + -r <ref genome> || -f <fasta file>] -o output file name" + nl
            + "Detailed description:" + nl + nl
            + "\t-i Data matrix\tThis is a tab delimited file with sample names in the columns and chromosome locations (that correspond to entries in the bed file (-b)) in the rows. Must have a header with the sample names for each corresponding column" + nl
            + "\t\texample:\tanimal1\tanimal2\tanimal3" + nl
            + "\t\tchr1:100-200\t2\t3\t2" + nl
            + "\t\tchr1:200-300\t3\t3\t3" + nl + nl
            + "\t-o output file\tThis is the name of the output file that is bed file with GC smoothed values for each animal in columns 5 onwards" + nl + nl
            + "\tEITHER -b AND -r:" + nl
            + "\t-b Bed file\tThis is a series of genomic coordinates to divide the reference genome into windows for GC calculation. The numbers of bed coordinates MUST match the rows of your matrix file!" + nl
            + "\t\texample:\tchr\tstart\tend\tgc\tanimal1\tanimal2\tanimal3" + nl
            + "\t\t\tchr1\t100\t200\t0.23\t2.13\t3.41\t4.44" + nl
            + "\t\t\tchr1\t200\t300\t0.25\t3.56\t4.55\t4.44" + nl + nl
            + "\t-r Reference genome file\tThis is a fasta file that contains the reference genome sequence for your animal to estimate the GC percentage of each window within the bed (-b) file" + nl + nl
            + "\tOR just -f:" + nl
            + "\t-f Reference genome file\tThis is a fasta file that contains the reference genome sequence for your animal. Only select this option if you want to estimate across each chromsome and not across smaller windows" + nl
            + nl;
    public String outfile = null;
    public String inputMat = null;
    public String refgenome = null;
    public boolean isRefGen = true;
    public String coordbed = null;
    public String fastafile;
    
    public ParseCommandLine(String[] args){
        for(int i = 0; i < args.length; i++){
            switch(args[i]){
                case "-i":
                    this.inputMat = args[i+1];
                    break;
                case "-o":
                    this.outfile = args[i+1];
                    break;
                case "-b":
                    this.coordbed = args[i+1];
                    break;
                case "-r":
                    this.refgenome = args[i+1];
                    break;
                case "-f":
                    this.fastafile = args[i+1];
                    this.isRefGen = false;
                    break;
                case "-h":
                    System.out.println(Usage);
                    System.exit(0);
            }
        }
        
        if(this.isRefGen && (this.coordbed == null || this.refgenome == null)){
            System.out.println("Must designate both reference genome and coordinates simultaneously!");
            System.out.println(Usage);
            System.exit(-1);
        }else if(this.inputMat == null || this.outfile == null){
            System.out.println(Usage);
            System.exit(-1);
        }
    }
}
