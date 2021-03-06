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

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 09/12/13
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
public class RampartStageList extends ArrayList<RampartStage> {

    private Set<RampartStage> distinctStages;

    public RampartStageList() {
        super();
        this.distinctStages = new HashSet<>();
    }


    @Override
    public void add(int index, RampartStage stage) {
        throw new UnsupportedOperationException("Cannot insert stages");
    }


    @Override
    public boolean add(RampartStage stage) {

        if (distinctStages.contains(stage)) {
            throw new IllegalArgumentException("Cannot add duplicated stage to RAMPART stage list");
        }

        this.distinctStages.add(stage);
        boolean result = super.add(stage);

        this.sort();

        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends RampartStage> stages) {
        throw new UnsupportedOperationException("Cannot insert collection of stages");
    }

    public List<AbstractConanProcess> createProcesses(ConanExecutorService ces) {

        List<AbstractConanProcess> processes = new ArrayList<>();

        for(RampartStage stage : this) {

            if(stage.getArgs() != null) {
                processes.add(stage.create(ces));
            }
        }

        return processes;
    }

    public RampartStage get(RampartStage stageToFind) {

        for(RampartStage stage : this) {
            if (stage == stageToFind) {
                return stage;
            }
        }

        return null;
    }

    public List<ConanProcess> getExternalTools() {

        List<ConanProcess> processes = new ArrayList<>();

        for(RampartStage stage : this) {
            processes.addAll(stage.getArgs().getExternalProcesses());
        }

        return processes;
    }

    public static RampartStageList parse(String stages) {

        if (stages.trim().equalsIgnoreCase("ALL")) {
            stages = RampartStage.getFullListAsString();
        }

        String[] stageArray = stages.split(",");

        RampartStageList stageList = new RampartStageList();

        if (stageArray != null && stageArray.length != 0) {
            for(String stage : stageArray) {
                stageList.add(RampartStage.valueOf(stage.trim().toUpperCase()));
            }
        }

        return stageList;
    }


    public String toString() {

        return StringUtils.join(this, ",");
    }

    public void setArgsIfPresent(RampartStage stage, RampartStageArgs args) {

        if (args != null) {
            for(RampartStage rs : this) {
                if (rs == stage) {
                    rs.setArgs(args);
                }
            }
        }
    }

    public void sort() {

        Collections.sort(this, new Comparator<RampartStage>() {
            @Override
            public int compare(RampartStage o1, RampartStage o2) {
                return o1.ordinal() - o2.ordinal();
            }
        });
    }

}
