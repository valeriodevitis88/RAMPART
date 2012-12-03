package uk.ac.tgac.rampart.data;

import java.io.File;
import java.io.IOException;

public class RampartJobFileStructure {

	// Root directory
	private File jobDir;
	
	// Directories
	private File readsDir;
	private File massDir;
	private File massStatsDir;
	private File improverDir;
	private File improverStatsDir;
	private File reportDir;
	private File reportImagesDir;
	private File logDir;
	
	// Important Files
	private File configFile;
	private File configRawFile;
	private File configQtFile;
	private File qtLogFile;
	private File massPlotsFile;
	private File massStatsFile;
	private File massLogFile;
	private File improverPlotsFile;
	private File improverStatsFile;
	private File improverLogFile;
	private File reportTemplateFile;
	private File reportMergedFile;
	private File settingsFile;
	
	
	public RampartJobFileStructure(File jobDir) {
		this.jobDir = jobDir;
		
		setupFileStructure();
	}


	public void setupFileStructure() {
		
		// Record all important directories and make sure they exist
		this.readsDir = new File(jobDir.getPath() + "/reads");
		this.massDir = new File(jobDir.getPath() + "/mass");
		this.massStatsDir = new File(massDir.getPath() + "/stats");
		this.improverDir = new File(jobDir.getPath() + "/improver");
		this.improverStatsDir = new File(improverDir.getPath() + "/stats");
		this.reportDir = new File(jobDir.getPath() + "/report");
		this.reportImagesDir = new File(reportDir.getPath() + "/images");
		this.logDir = new File(jobDir.getPath() + "/log");
		
		this.configFile = new File(jobDir.getPath() + "/rampart.cfg");
		this.configRawFile = new File(this.readsDir.getPath() + "/raw.cfg");
		this.configQtFile = new File(this.readsDir.getPath() + "/qt.cfg");
		this.qtLogFile = new File(this.readsDir.getPath() + "/qt.log");
		this.massPlotsFile = new File(this.massStatsDir.getPath() + "/plots.pdf");
		this.massStatsFile = new File(this.massStatsDir.getPath() + "/score.tab");
		this.massLogFile = new File(this.massDir.getPath() + "/mass.log");
		this.improverPlotsFile = new File(this.improverStatsDir.getPath() + "/stats.pdf");
		this.improverStatsFile = new File(this.improverStatsDir.getPath() + "/stats.txt");
		this.improverLogFile = new File(this.improverDir.getPath() + "/improver.log");
		this.reportTemplateFile = new File(this.reportDir.getPath() + "/template.tex");
		this.reportMergedFile = new File(this.reportDir.getPath() + "/report.tex");
		this.settingsFile = new File(jobDir.getPath() + "/rampart_settings.log");
	}
	
	
	public void validate(boolean includeReport, boolean includeFiles) throws IOException {
		
		if (!this.readsDir.exists() || !this.massDir.exists() || !this.massStatsDir.exists() || !this.improverDir.exists()) {
			throw new IOException("RAMPART job directory structure is not valid.");
		}
		
		if (includeReport && (!this.reportDir.exists() || !this.reportImagesDir.exists())) {
			throw new IOException("RAMPART report directory structure is not valid.");
		}
		
		if (includeFiles && (!this.massPlotsFile.exists() || !this.improverPlotsFile.exists())) {
			throw new IOException("Not all RAMPART files are present");
		}
	}
	
	public void makeReportDirs() throws IOException {
		this.reportDir.mkdir();
		this.reportImagesDir.mkdir();
		
		if (!this.reportDir.exists() || !this.reportImagesDir.exists()) {
			throw new IOException("Could not create reports directories.");
		}
	}


	// ****** Dirs ******
	
	public File getJobDir() {
		return jobDir;
	}


	public File getReadsDir() {
		return readsDir;
	}


	public File getMassDir() {
		return massDir;
	}


	public File getMassStatsDir() {
		return massStatsDir;
	}


	public File getImproverDir() {
		return improverDir;
	}


	public File getReportDir() {
		return reportDir;
	}


	public File getReportImagesDir() {
		return reportImagesDir;
	}
	
	public File getLogDir() {
		return logDir;
	}
	
	
	// ****** Files ******

	public File getConfigFile() {
		return configFile;
	}
	
	public File getSettingsFile() {
		return settingsFile;
	}
	
	public File getConfigRawFile() {
		return configRawFile;
	}
	
	public File getConfigQtFile() {
		return configQtFile;
	}
	
	public File getQtLogFile() {
		return qtLogFile;
	}

	public File getMassPlotsFile() {
		return massPlotsFile;
	}

	public File getMassStatsFile() {
		return massStatsFile;
	}

	public File getMassLogFile() {
		return massLogFile;
	}
	
	public File getReportTemplateFile() {
		return reportTemplateFile;
	}
	
	public File getReportMergedFile() {
		return reportMergedFile;
	}
	
	public File getImproverPlotsFile() {
		return improverPlotsFile;
	}

	public File getImproverStatsDir() {
		return improverStatsDir;
	}

	public File getImproverStatsFile() {
		return improverStatsFile;
	}

	public File getImproverLogFile() {
		return improverLogFile;
	}

}
