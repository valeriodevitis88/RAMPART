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
package uk.ac.tgac.rampart.conan.conanx.exec.task;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;

import java.util.Collection;

/**
 * Represents an enhanced/extended {@link ConanProcess} to be execute in a Conan environment.  This could be a Process
 * to execute as part of a Conan Pipeline, or a sub task to be executed as part of a number of other sub-processes within
 * a conan pipeline.  The key extension provided by this interface is that a task can return it's command to execute,
 * which enables it to be run in a variety of different environments. Therefore the caller may decide to return the command
 * to execute using {@code String getCommand()} or {@code String getFullCommand()} rather than directly executing the task
 * using the {@code boolean execute(Map<ConanParameter, String> parameters)} method.  In addition to returning the command
 * to execute it is possible to supplement that command with pre or post commands in order to build a compound command.
 * The compound command can be retrieved using the {@code String getFullCommand()} method.  This functionality is particularly
 * useful if there are additional commands required to bring a tool onto the path, or if certain clean-up functionality
 * is required after a tool has run.
 * @author maplesod
 */
public interface ConanExternalTask {

    /**
     * Returns the name of this external task.  This should be something human-readable, as this process name is what the
     * graphical interface will present to users.
     *
     * @return the name of this process
     */
    String getName();

    /**
     * Returns a collection of strings representing the names of the parameters that must be supplied in order to
     * execute this process.
     *
     * @return the parameter names required to generate a task
     */
    Collection<ConanParameter> getParameters();

    /**
     * Get the command to execute for this {@link ConanExternalTask}
     * @return The command to execute
     */
    String getCommand();

    /**
     * Get the command to execute including and pre and post commands as a single compound command to execute
     * @return The full compound command to execute for this {@link ConanExternalTask}
     */
    String getFullCommand();

    /**
     * Add an additional command to execute before running the main task.  The order in which the command is added is
     * down to the implementation.
     * @param preCommand The command to execute before running this task.
     */
    void addPreCommand(String preCommand);

    /**
     * Add an additional command to execute after running the main task.  The order in which the command is added is
     * down to the implementation.
     * @param postCommand The command to execute after running this task.
     */
    void addPostCommand(String postCommand);
}
