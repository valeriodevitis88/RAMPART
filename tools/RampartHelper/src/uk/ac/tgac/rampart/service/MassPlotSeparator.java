package uk.ac.tgac.rampart.service;

import java.io.File;

public class MassPlotSeparator {

	private static final String FILENAME_NB_CONTIGS = "Mass_NBC.pdf";	
	private static final String FILENAME_NB_BASES 	= "Mass_TB.pdf";
	private static final String FILENAME_N_PERC 	= "Mass_N.pdf";
	private static final String FILENAME_AVG_LEN 	= "Mass_AL.pdf";
	private static final String FILENAME_MAX_LEN 	= "Mass_ML.pdf";
	private static final String FILENAME_N50 		= "Mass_N50.pdf";
	
	private File plots;
	private File outputDir;
	
	private File massPlotNbContigsFile;
	private File massPlotNbBasesFile;
	private File massPlotNPcFile;
	private File massPlotAvgLenFile;
	private File massPlotMaxLenFile;
	private File massPlotN50File;
	
	public MassPlotSeparator(File plots, File outputDir) {
		this.plots = plots;
		this.outputDir = outputDir;
		
		setupFiles();
	}
	
	protected void setupFiles() {
		this.massPlotNbContigsFile = new File(this.outputDir.getPath() + "/" + FILENAME_NB_CONTIGS);
		this.massPlotNbBasesFile = new File(this.outputDir.getPath() + "/" + FILENAME_NB_BASES);
		this.massPlotNPcFile = new File(this.outputDir.getPath() + "/" + FILENAME_N_PERC);
		this.massPlotAvgLenFile = new File(this.outputDir.getPath() + "/" + FILENAME_AVG_LEN);
		this.massPlotMaxLenFile = new File(this.outputDir.getPath() + "/" + FILENAME_MAX_LEN);
		this.massPlotN50File = new File(this.outputDir.getPath() + "/" + FILENAME_N50);
	}

	public File getPlots() {
		return plots;
	}
	
	public File getOutputDir() {
		return outputDir;
	}

	public File getMassPlotNbContigsFile() {
		return massPlotNbContigsFile;
	}

	public File getMassPlotNbBasesFile() {
		return massPlotNbBasesFile;
	}

	public File getMassPlotNPcFile() {
		return massPlotNPcFile;
	}

	public File getMassPlotAvgLenFile() {
		return massPlotAvgLenFile;
	}

	public File getMassPlotMaxLenFile() {
		return massPlotMaxLenFile;
	}

	public File getMassPlotN50File() {
		return massPlotN50File;
	}
	
	public void seperatePlots() {
		
		PdfOperations.extractPage(this.plots, this.massPlotNbContigsFile, 1);
		PdfOperations.extractPage(this.plots, this.massPlotNPcFile, 6);
		PdfOperations.extractPage(this.plots, this.massPlotNbBasesFile, 7);
		PdfOperations.extractPage(this.plots, this.massPlotAvgLenFile, 10);
		PdfOperations.extractPage(this.plots, this.massPlotMaxLenFile, 9);
		PdfOperations.extractPage(this.plots, this.massPlotN50File, 11);
	}
	
	public static void seperatePlots(File in, File outDir) {
		MassPlotSeparator mps = new MassPlotSeparator(in, outDir);
		mps.seperatePlots();
	}
	
}
