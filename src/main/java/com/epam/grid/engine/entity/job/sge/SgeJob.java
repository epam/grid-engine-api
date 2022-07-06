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

package com.epam.grid.engine.entity.job.sge;

import com.epam.grid.engine.entity.util.LocalDateTimeAdapter;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * This class represents SGE job as XML elements.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SgeJob {

    /**
     * The state of the job.
     */
    @XmlAttribute(name = "state")
    private String state;

    /**
     * The id of the job.
     */
    @XmlElement(name = "JB_job_number")
    private long id;

    /**
     * The priority of the job.
     */
    @XmlElement(name = "JAT_prio")
    private double priority;

    /**
     * The name of the job.
     */
    @XmlElement(name = "JB_name")
    private String name;

    /**
     * The owner of the job.
     */
    @XmlElement(name = "JB_owner")
    private String owner;

    /**
     * The state code {@link com.epam.grid.engine.entity.job.JobState} of the job.
     */
    @XmlElement(name = "state")
    private String stateCode;

    /**
     * The submission time of the job.
     */
    @XmlElements({
            @XmlElement(name = "JAT_start_time"),
            @XmlElement(name = "JB_submission_time")
    })
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime submissionTime;

    /**
     * Name of the queue in which the job is started.
     */
    @XmlElement(name = "queue_name")
    private String queueName;

    /**
     * The number of slots that the job takes up.
     */
    @XmlElement(name = "slots")
    private int slots;
}
