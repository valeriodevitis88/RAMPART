/*
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2015  Daniel Mapleson - TGAC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.tgac.rampart.stage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.ac.ebi.fgpt.conan.core.context.DefaultTaskResult;
import uk.ac.ebi.fgpt.conan.core.param.DefaultParamMap;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.TaskResult;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.core.data.Organism;
import uk.ac.tgac.conan.core.util.XmlHelper;
import uk.ac.tgac.conan.process.asm.stats.QuastV23;
import uk.ac.tgac.rampart.RampartCLI;
import uk.ac.tgac.rampart.stage.analyse.asm.analysers.AssemblyAnalyser;
import uk.ac.tgac.rampart.stage.analyse.asm.selector.AssemblySelector;
import uk.ac.tgac.rampart.stage.analyse.asm.selector.DefaultAssemblySelector;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStats;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by maplesod on 24/10/14.
 */
public class Select extends RampartProcess {

    private static Logger log = LoggerFactory.getLogger(Amp.class);

    public Select() {
        this(null);
    }

    public Select(ConanExecutorService ces) {
        this(ces, new Args());
    }

    public Select(ConanExecutorService ces, Args args) {
        super(ces, args);
    }

    public Args getArgs() {
        return (Args) this.getProcessArgs();
    }

    @Override
    public String getName() {
        return "Select";
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {

        log.info("Select stage is operational.");
        return true;
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public TaskResult executeSample(Mecq.Sample sample, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException, IOException {


        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Args args = this.getArgs();


        List<ExecutionResult> results = new ArrayList<>();

        // If we have a reference run quast on it to get some stats
        if (args.getOrganism().getReference() != null && args.getOrganism().getReference().getPath() != null) {

            List<File> inputFiles = new ArrayList<>();
            inputFiles.add(args.getOrganism().getReference().getPath());
            QuastV23.Args refArgs = new QuastV23.Args();
            refArgs.setInputFiles(inputFiles);
            refArgs.setFindGenes(true);
            refArgs.setThreads(args.getThreads());
            refArgs.setEukaryote(args.getOrganism().getPloidy() > 1);
            refArgs.setOutputDir(args.getRefOutDir(sample));

            QuastV23 refQuast = new QuastV23(this.conanExecutorService, refArgs);

            results.add(this.conanExecutorService.executeProcess(refQuast, args.getRefOutDir(sample), args.jobPrefix + "-refquast", args.getThreads(), args.getMemoryMb(), args.runParallel));
        }

        stopWatch.stop();

        return new DefaultTaskResult(sample.name + "-" + args.stage.getOutputDirName(), true, results, stopWatch.getTime() / 1000L);
    }

    @Override
    public boolean validateOutput(Mecq.Sample sample) throws IOException, InterruptedException, ProcessExecutionException {

        // Create the stats table with information derived from the configuration file.
        AssemblyStatsTable table = this.createTable(sample);
        boolean cegmaSelected = false;
        QuastV23.AssemblyStats refStats = null;

        Args args = this.getArgs();

        File stageOutputDir = args.getStageDir(sample);

        // If we have a reference run quast on it to get some stats
        if (args.getOrganism().getReference() != null && args.getOrganism().getReference().getPath() != null) {

            QuastV23.Report report = new QuastV23.Report(new File(args.getRefOutDir(sample), "report.txt"));
            refStats = report.getAssemblyStats(0);

            log.info("Reference genome size: " + refStats.getTotalLengthGt0());
            log.info("Reference GC%: " + refStats.getGcPc());
            log.info("Reference # Genes: " + refStats.getNbGenes());
        }

        for (AssemblyAnalyser analyser : args.analysers) {

            File massAnalysisDir = new File(args.getSampleDir(sample), RampartStage.MASS_ANALYSIS.getOutputDirName());
            File reportDir = new File(massAnalysisDir, analyser.getName().toLowerCase());

            analyser.updateTable(
                    table,
                    analyser.isFast() ? new File(reportDir, "longest") : reportDir
            );

            if (analyser.getName().equalsIgnoreCase("CEGMA")) {
                cegmaSelected = true;
            }
        }

        // Select the assembly
        AssemblySelector assemblySelector = new DefaultAssemblySelector(args.getWeightingsFile());
        AssemblyStats selectedAssembly = assemblySelector.selectAssembly(
                table,
                args.getOrganism(),
                refStats,
                cegmaSelected
        );

        File bestAssembly = new File(selectedAssembly.getFilePath());

        String bubblePath = selectedAssembly.getBubblePath();
        File bubbles = bubblePath.equalsIgnoreCase("NA") || bubblePath.isEmpty() ?
                null :
                new File(selectedAssembly.getBubblePath());

        File bestAssemblyLink = new File(stageOutputDir, "best.fa");
        File bubblesLink = new File(stageOutputDir, "best_bubbles.fa");

        log.info("Best assembly path: " + bestAssembly.getAbsolutePath());

        // Create link to "best" assembly in stats dir
        this.getConanProcessService().createLocalSymbolicLink(bestAssembly, bestAssemblyLink);
        if (bubbles != null && bubbles.exists()) {
            this.getConanProcessService().createLocalSymbolicLink(bubbles, bubblesLink);
        }

        // Save table to disk
        File finalTSVFile = new File(stageOutputDir, "scores.tsv");
        table.saveTsv(finalTSVFile);
        log.debug("Saved final results in TSV format to: " + finalTSVFile.getAbsolutePath());

        File finalSummaryFile = new File(stageOutputDir, "scores.txt");
        table.saveSummary(finalSummaryFile);
        log.debug("Saved final results in summary format to: " + finalSummaryFile.getAbsolutePath());

        return true;
    }


    protected AssemblyStatsTable createTable(Mecq.Sample sample) throws IOException {

        Args args = this.getArgs();

        AssemblyStatsTable table = new AssemblyStatsTable();

        File assemblyLinkageFile = new File(new File(args.getSampleDir(sample), RampartStage.MASS_ANALYSIS.getOutputDirName()), "assembly_linkage.txt");

        List<String> lines = FileUtils.readLines(assemblyLinkageFile);

        for(String line : lines) {

            String tl = line.trim();

            if (!tl.isEmpty()) {
                String[] parts = line.split("\t");

                AssemblyStats stats = new AssemblyStats();
                stats.setIndex(Integer.parseInt(parts[0]));
                stats.setDataset(parts[1]);
                stats.setDesc(parts[2]);
                stats.setFilePath(parts[3]);
                stats.setBubblePath(parts[4]);
                table.add(stats);
            }
        }

        return table;
    }

    public static class Args extends RampartProcessArgs {

        private static final String KEY_ATTR_WEIGHTINGS = "weightings_file";

        public static final File DEFAULT_SYSTEM_WEIGHTINGS_FILE = new File(RampartCLI.ETC_DIR, "weightings.tab");
        public static final File    DEFAULT_USER_WEIGHTINGS_FILE = new File(RampartCLI.USER_DIR, "weightings.tab");
        public static final File    DEFAULT_WEIGHTINGS_FILE = DEFAULT_USER_WEIGHTINGS_FILE.exists() ?
                DEFAULT_USER_WEIGHTINGS_FILE : DEFAULT_SYSTEM_WEIGHTINGS_FILE;

        private File weightingsFile;
        private Map<Mecq.Sample, List<MassJob.Args>> massJobs;
        private List<AssemblyAnalyser> analysers;

        public Args() {
            super(RampartStage.MASS_SELECT);

            this.weightingsFile = DEFAULT_WEIGHTINGS_FILE;
            this.massJobs = null;
            this.analysers = null;
        }

        public Args(Element element, File outputDir, String jobPrefix, Map<Mecq.Sample, List<MassJob.Args>> massJobs, Organism organism,
                    List<AssemblyAnalyser> analysers, boolean runParallel)
                throws IOException {

            super(RampartStage.MASS_SELECT, outputDir, jobPrefix, new ArrayList<>(massJobs.keySet()), organism, runParallel);

            // Check there's nothing
            if (!XmlHelper.validate(element,
                    new String[0],
                    new String[]{
                            KEY_ATTR_WEIGHTINGS,
                            KEY_ATTR_THREADS,
                            KEY_ATTR_MEMORY,
                            KEY_ATTR_PARALLEL
                    },
                    new String[0],
                    new String[0]
            )) {
                throw new IOException("Found unrecognised element or attribute in \"analyse_mass\"");
            }

            this.massJobs = massJobs;
            this.analysers = analysers;

            this.weightingsFile = element.hasAttribute(KEY_ATTR_WEIGHTINGS) ?
                    new File(XmlHelper.getTextValue(element, KEY_ATTR_WEIGHTINGS)) :
                    DEFAULT_WEIGHTINGS_FILE;

            // Optional
            this.threads = element.hasAttribute(KEY_ATTR_THREADS) ?
                    XmlHelper.getIntValue(element, KEY_ATTR_THREADS) :
                    DEFAULT_THREADS;

            this.memoryMb = element.hasAttribute(KEY_ATTR_MEMORY) ?
                    XmlHelper.getIntValue(element, KEY_ATTR_MEMORY) :
                    DEFAULT_MEMORY;

            this.runParallel = element.hasAttribute(KEY_ATTR_PARALLEL) ?
                    XmlHelper.getBooleanValue(element, KEY_ATTR_PARALLEL) :
                    runParallel;
        }

        public File getWeightingsFile() {
            return weightingsFile;
        }

        public void setWeightingsFile(File weightingsFile) {
            this.weightingsFile = weightingsFile;
        }

        public Map<Mecq.Sample, List<MassJob.Args>> getMassJobs() {
            return massJobs;
        }

        public void setMassJobs(Map<Mecq.Sample, List<MassJob.Args>> massJobs) {
            this.massJobs = massJobs;
        }

        public List<AssemblyAnalyser> getAnalysers() {
            return analysers;
        }

        public void setAnalysers(List<AssemblyAnalyser> analysers) {
            this.analysers = analysers;
        }

        public File getRefOutDir(Mecq.Sample sample) {
            return new File(this.getStageDir(sample), "ref_quast");
        }

        @Override
        protected void setOptionFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        protected void setArgFromMapEntry(ConanParameter param, String value) {

        }

        @Override
        public List<ConanProcess> getExternalProcesses() {
            return null;
        }

        @Override
        public ParamMap getArgMap() {
            ParamMap pvp = new DefaultParamMap();
            return pvp;
        }
    }
}