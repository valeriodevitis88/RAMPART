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
package uk.ac.tgac.rampart.tool.pipeline.rampart;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.tgac.rampart.tool.pipeline.RampartStage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 07/02/13
 * Time: 19:26
 */
public class RampartArgs implements ProcessArgs {

    private RampartParams params = new RampartParams();

    private File config;
    private File outputDir;
    private List<RampartStage> stages;

    public RampartArgs() {
        this.config = null;
        this.outputDir = new File("");
        this.stages = new ArrayList<RampartStage>();
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public List<RampartStage> getStages() {
        return stages;
    }

    public void setStages(List<RampartStage> stages) {
        this.stages = stages;
    }

    @Override
    public void parse(String args) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.config != null) {
            pvp.put(params.getConfig(), this.config.getAbsolutePath());
        }

        if (this.outputDir != null) {
            pvp.put(params.getOutputDir(), this.outputDir.getAbsolutePath());
        }

        if (this.stages != null && this.stages.size() > 0) {
            pvp.put(params.getStageList(), RampartStage.toString(this.stages));
        }

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {
        for (Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getConfig().getName())) {
                this.config = new File(entry.getValue());
            }
            else if (param.equals(this.params.getOutputDir().getName())) {
                this.outputDir = new File(entry.getValue());
            }
            else if (params.equals(this.params.getStageList().getName())) {
                this.stages = RampartStage.parse(entry.getValue());
            }

        }
    }
}