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

package com.epam.grid.engine.provider.utils.sge;

import java.util.Collections;
import java.util.List;

public class TestSgeConstants {
    public static final String ONE = "1";

    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";

    public static final String STANDARD_WARN = "standard warn";

    public static final List<String> EMPTY_LIST = Collections.emptyList();
    public static final List<String> SINGLETON_LIST_WITH_STANDARD_WARN = Collections.singletonList(STANDARD_WARN);

    public static final String PENDING_STRING = "pending";
    public static final String RUNNING_STRING = "running";
    public static final String SUSPENDED_STRING = "suspended";
    public static final String ZOMBIE_STRING = "zombie";

    public static final String TYPE_XML = "-xml";
}
