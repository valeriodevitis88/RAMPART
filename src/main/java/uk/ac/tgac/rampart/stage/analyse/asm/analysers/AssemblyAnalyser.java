package uk.ac.tgac.rampart.stage.analyse.asm.analysers;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.stage.analyse.asm.AnalyseAssembliesArgs;
import uk.ac.tgac.rampart.stage.analyse.asm.stats.AssemblyStatsTable;
import uk.ac.tgac.rampart.util.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
public interface AssemblyAnalyser extends Service {

    /**
     * Checks whether or not this assembly analyser looks properly configured for the system
     * @param executionContext
     * @return
     */
    boolean isOperational(ExecutionContext executionContext);

    /**
     * Execute this assembly analysis
     * @param args
     * @param ces
     * @return A list of job ids from the executed jobs
     * @throws InterruptedException
     * @throws ProcessExecutionException
     * @throws ConanParameterException
     * @throws IOException
     */
    List<Integer> execute(List<File> assemblies, File outputDir, String jobPrefix, AnalyseAssembliesArgs args, ConanExecutorService ces)
            throws InterruptedException, ProcessExecutionException, ConanParameterException, IOException;

    /**
     * Updates the provided table with information from this analysis
     * @param table
     * @throws IOException
     */
    void updateTable(AssemblyStatsTable table, List<File> assemblies, File reportDir, String subGroup) throws IOException;

    /**
     * If this assembly analysis runs quickly, then we might to this information to consider doing more work with it.
     * @return True if this process runs quickly, false if not.
     */
    boolean isFast();
}
