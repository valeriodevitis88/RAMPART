/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
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
 **/
package uk.ac.tgac.rampart.tool.pipeline.amp;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.rampart.tool.RampartExecutor;

import java.io.File;

/**
 * User: maplesod
 * Date: 25/03/13
 * Time: 11:10
 */
public interface AmpExecutor extends RampartExecutor {

    void executeAnalysisJob(AmpArgs ampArgs)
            throws InterruptedException, ProcessExecutionException;

    /**
     * Creates the initial symbolic link between the input assembly and the output directory.  This is basically a
     * convienience for the user and helps to compare assemblies at each stage later.
     * @param sourceFile
     * @param outputDir
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    void createInitialLink(File sourceFile, File outputDir)
            throws InterruptedException, ProcessExecutionException;

}