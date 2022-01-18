/*
 *
 *  * Copyright 2022 EPAM Systems, Inc. (https://www.epam.com/)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.epam.grid.engine.mapper.queue.sge;

import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.SlotsDescription;
import com.epam.grid.engine.entity.queue.sge.SgeQueue;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.grid.engine.utils.TextConstants.COMMA;
import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static com.epam.grid.engine.utils.TextConstants.EQUAL_SIGN;
import static com.epam.grid.engine.utils.TextConstants.NONE;
import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * This interface consists of the configuration of a mapper for implementation by the MapStruct processor
 * for creation a {@link Queue} object out of a {@link SgeQueue} object.
 */
@Mapper(componentModel = "spring")
public interface SgeQueueMapper {

    /**
     * The actual mapping method expects the source object as parameter and returns the target object.
     *
     * @param sgeQueue the source object
     * @return The mapped object
     */
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "loadThresholds", ignore = true)
    @Mapping(target = "suspendThresholds", ignore = true)
    Queue sgeQueueToQueue(SgeQueue sgeQueue);

    /**
     * Maps raw output to {@link SgeQueue} object.
     * To get more information about field formats of queue configuration report see
     * <a href="http://gridscheduler.sourceforge.net/htmlman/htmlman5/queue_conf.html">
     * Sun Grid Engine queue configuration file format</a>.
     * <p>
     * Note: there are inaccuracies in the description of list fields in the manual - SGE in queue
     * configuration report returns space separated lists, except for the "load_thresholds",
     * "suspend_thresholds" and "processors" fields, where the lists are separated by commas.
     *
     * @param queueDescription Map of Strings containing full information about each SgeQueue
     * @return {@link SgeQueue}
     */
    @Mapping(target = "name", source = "qname")
    @Mapping(target = "hostList",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"hostlist\")))")
    @Mapping(target = "numberInSchedulingOrder", source = "seq_no")
    @Mapping(target = "loadThresholds",
            expression = "java(parseQueueDescriptionValuesWithComma(queueDescription.get(\"load_thresholds\")))")
    @Mapping(target = "suspendThresholds",
            expression = "java(parseQueueDescriptionValuesWithComma(queueDescription.get(\"suspend_thresholds\")))")
    @Mapping(target = "numOfSuspendedJobs", source = "nsuspend")
    @Mapping(target = "interval", source = "suspend_interval")
    @Mapping(target = "jobPriority", source = "priority")
    @Mapping(target = "minCpuInterval", source = "min_cpu_interval")
    @Mapping(target = "processorNumber",
            expression = "java(parseProcessorDescriptionValue(queueDescription.get(\"processors\")))")
    @Mapping(target = "checkpointNames",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"ckpt_list\")))")
    @Mapping(target = "parallelEnvironmentNames",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"pe_list\")))")
    @Mapping(target = "tmpDir", source = "tmpdir")
    @Mapping(target = "prolog",
            expression = "java(parseQueueDescriptionStringValuesWithNone(queueDescription.get(\"prolog\")))")
    @Mapping(target = "epilog",
            expression = "java(parseQueueDescriptionStringValuesWithNone(queueDescription.get(\"epilog\")))")
    @Mapping(target = "shellStartMode", source = "shell_start_mode")
    @Mapping(target = "starterMethod",
            expression = "java(parseQueueDescriptionStringValuesWithNone(queueDescription.get(\"starter_method\")))")
    @Mapping(target = "suspendMethod",
            expression = "java(parseQueueDescriptionStringValuesWithNone(queueDescription.get(\"suspend_method\")))")
    @Mapping(target = "resumeMethod",
            expression = "java(parseQueueDescriptionStringValuesWithNone(queueDescription.get(\"resume_method\")))")
    @Mapping(target = "terminateMethod",
            expression = "java(parseQueueDescriptionStringValuesWithNone(queueDescription.get(\"terminate_method\")))")
    @Mapping(target = "ownerList",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"owner_list\")))")
    @Mapping(target = "allowedUserGroups",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"user_lists\")))")
    @Mapping(target = "forbiddenUserGroups",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"xuser_lists\")))")
    @Mapping(target = "subordinateList",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"subordinate_list\")))")
    @Mapping(target = "complexValues",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"complex_values\")))")
    @Mapping(target = "allowedProjects",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"projects\")))")
    @Mapping(target = "forbiddenProjects",
            expression = "java(parseQueueDescriptionValuesWithSpace(queueDescription.get(\"xprojects\")))")
    @Mapping(target = "calendar",
            expression = "java(parseQueueDescriptionStringValuesWithNone(queueDescription.get(\"calendar\")))")
    @Mapping(target = "initialState", source = "initial_state")
    @Mapping(target = "secRealTime", source = "s_rt")
    @Mapping(target = "hourRealTime", source = "h_rt")
    @Mapping(target = "secCpu", source = "s_cpu")
    @Mapping(target = "hourCpu", source = "h_cpu")
    @Mapping(target = "secFSize", source = "s_fsize")
    @Mapping(target = "hourFSize", source = "h_fsize")
    @Mapping(target = "secData", source = "s_data")
    @Mapping(target = "hourData", source = "h_data")
    @Mapping(target = "secStack", source = "s_stack")
    @Mapping(target = "hourStack", source = "h_stack")
    @Mapping(target = "secCore", source = "s_core")
    @Mapping(target = "hourCore", source = "h_core")
    @Mapping(target = "secRss", source = "s_rss")
    @Mapping(target = "hourRss", source = "h_rss")
    @Mapping(target = "secVmem", source = "s_vmem")
    @Mapping(target = "hourVmem", source = "h_vmem")
    SgeQueue mapRawOutputToSgeQueue(Map<String, String> queueDescription);

    /**
     * The method maps the slots attribute of the source object to the target object.
     *
     * @param sgeQueue the source object
     * @param queue    the target object
     */
    @AfterMapping
    default void fillSlots(final SgeQueue sgeQueue, final @MappingTarget Queue queue) {
        queue.setSlots(mapSgeSlotsToSlots(sgeQueue.getSlots()));
    }

    /**
     * The method maps the loadThreshold and suspendedThreshold attribute of the source object to the target object.
     *
     * @param sgeQueue the source object
     * @param queue    the target object
     */
    @AfterMapping
    default void fillThresholds(final SgeQueue sgeQueue, final @MappingTarget Queue queue) {
        queue.setLoadThresholds(mapSgeThresholdsToThresholds(sgeQueue.getLoadThresholds()));
        queue.setSuspendThresholds(mapSgeThresholdsToThresholds(sgeQueue.getSuspendThresholds()));
    }

    default SlotsDescription mapSgeSlotsToSlots(final String sgeSlots) {
        final SlotsDescription slotsDescription = new SlotsDescription();
        final String[] slots = sgeSlots.split(COMMA);
        slotsDescription.setSlots(Integer.parseInt(slots[0]));
        if (slots.length > 1) {
            final String slotsDescriptionString = slots[1].substring(1, slots[1].length() - 1);
            final Map<String, Integer> slotsDescriptionMap = Stream.of(slotsDescriptionString.split(COMMA))
                    .map(descriptionEntry -> descriptionEntry.split(EQUAL_SIGN))
                    .map(pair -> Map.entry(pair[0], Integer.parseInt(pair[1])))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            slotsDescription.setSlotsDetails(slotsDescriptionMap);
        } else {
            slotsDescription.setSlotsDetails(Collections.emptyMap());
        }
        return slotsDescription;
    }

    default Map<String, Double> mapSgeThresholdsToThresholds(final List<String> sgeThresholds) {
        return sgeThresholds.stream()
                .filter(threshold -> !threshold.startsWith(NONE))
                .map(threshold -> threshold.split(EQUAL_SIGN))
                .map(pair -> Map.entry(pair[0], Double.parseDouble(pair[1])))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    default List<String> parseQueueDescriptionValuesWithSpace(final String values) {
        if (!StringUtils.hasText(values) || values.equals("NONE")) {
            return Collections.emptyList();
        }
        return List.of(values.split(SPACE));
    }

    default List<String> parseQueueDescriptionValuesWithComma(final String values) {
        if (!StringUtils.hasText(values) || values.equals("NONE")) {
            return Collections.emptyList();
        }
        return List.of(values.split(COMMA));
    }

    default List<String> parseProcessorDescriptionValue(final String values) {
        if (!StringUtils.hasText(values) || values.equals("UNDEFINED")) {
            return Collections.emptyList();
        }
        return List.of(values.split(COMMA));
    }

    default String parseQueueDescriptionStringValuesWithNone(final String value) {
        if (!StringUtils.hasText(value) || value.equals("NONE")) {
            return EMPTY_STRING;
        }
        return value;
    }
}
