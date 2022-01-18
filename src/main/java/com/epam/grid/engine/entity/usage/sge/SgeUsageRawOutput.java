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

package com.epam.grid.engine.entity.usage.sge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * This class represents an object containing usage summary information read directly
 * from the SGE output and separated into a header and a data line.
 * More information about SGE report content:
 * <p><a href="http://gridscheduler.sourceforge.net/htmlman/htmlman1/qacct.html">
 * http://gridscheduler.sourceforge.net/htmlman/htmlman1/qacct.html</a>
 */
@Builder
@Data
@AllArgsConstructor
public class SgeUsageRawOutput {
    private String header;
    private String accountingData;
}
