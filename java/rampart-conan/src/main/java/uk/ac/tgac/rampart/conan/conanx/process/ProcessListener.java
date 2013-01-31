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
package uk.ac.tgac.rampart.conan.conanx.process;

import uk.ac.ebi.fgpt.conan.lsf.LSFProcessEvent;

/**
 * An {@link uk.ac.tgac.rampart.conan.conanx.process.ProcessListener} that encapsulates the state of each invocation of a process and updates flags for
 * completion and success.  Processes using this listener implementation can block on {@link #waitFor()}, which only
 * returns once the process being listened to is complete.
 *
 * User: maplesod
 * Date: 24/01/13
 * Time: 16:50
 */
public interface ProcessListener {
    /**
     * Called whenever a monitored output File indicates that the underlying process has finished
     *
     * @param evt an event reporting the change to the file
     */
    void processComplete(ProcessEvent evt);

    /**
     * Called whenever a monitored output file indicates the the underlying process wrote output.
     *
     * @param evt an event reporting the change to the file
     */
    void processUpdate(ProcessEvent evt);

    /**
     * Called whenever an error occurs in monitoring this process (for example, if the output file being monitored is
     * deleted by an external process).
     *
     * @param evt an event reporting the change to the file
     */
    void processError(ProcessEvent evt);

    /**
     * Returns the success of the Process being listened to, only once complete.  This method blocks until
     * completion or an interruption occurs.
     *
     * @return the exit value of the underlying process
     * @throws InterruptedException if the thread was interrupted whilst waiting
     */
    int waitFor() throws InterruptedException;
}
