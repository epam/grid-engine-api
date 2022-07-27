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

package com.epam.grid.engine.cmd;

import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.entity.QueueFilter;
import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.entity.job.ParallelEnvOptions;
import com.epam.grid.engine.entity.usage.UsageReportFilter;
import com.epam.grid.engine.provider.utils.sge.job.QstatCommandParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.PENDING_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.RUNNING_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SPACE;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SUSPENDED_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.ZOMBIE_STRING;
import static com.epam.grid.engine.utils.TextConstants.NEW_LINE_DELIMITER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class GridEngineCommandCompilerImplTest {

    private static final String PROPERTIES_PATH = "src/main/resources/application.properties";
    private static final String TEMPLATE_PATH_PROPERTY = "command.template.path";

    private static final int DAYS = 1;
    private static final String DAYS_KEY = "-d";
    private static final String START_DATE_TIME_KEY = "-b";
    private static final String EXPECTED_START_DATE_TIME = "202110011010.00";
    private static final String END_DATE_TIME_KEY = "-e";
    private static final String EXPECTED_END_DATE_TIME = "202110201010.00";
    private static final String QACCT_COMMAND = "qacct";
    private static final String OWNER = "sgeuser";
    private static final String OWNER_KEY = "-o";
    private static final String QUEUE = "all.q";
    private static final String QUEUE2 = "main";
    private static final String QUEUE_KEY = "-q";
    private static final String PARALLEL_ENV = "pe";
    private static final String PARALLEL_ENV_KEY = "-pe";

    private static final String QHOST_COMMAND = "qhost";
    private static final String QCONF_COMMAND = "qconf";
    private static final String SHGRPL_OPTION = "-shgrpl";
    private static final String SHGRP_OPTION = "-shgrp";
    private static final String TYPE_XML = "-xml";
    private static final String FILTER_FIELD_NAME = "filter";
    private static final String START_TIME_FIELD = "startTime";
    private static final String END_TIME_FIELD = "endTime";
    private static final String QPING_COMMAND = "qping";
    private static final String DEFINE_QMASTER_COMMAND = "define_qmaster";
    private static final String CAT_COMMAND = "cat";
    private static final String INFO_KEY = "-info";
    private static final String H_COMMAND = "-h";
    private static final String QMASTER_HOST_PATH_STRING = "qmasterHostPath";
    private static final String QMASTER_HOST_STRING = "qmasterHost";
    private static final String QMASTER_PORT_STRING = "qmasterPort";
    private static final String QMASTER_STRING = "qmaster";
    private static final String ONE = "1";
    private static final String SOME_HOST = "someHost";
    private static final String SOME_HOST2 = "someHost2";
    private static final String CURRENT_HOSTS = "current_host";
    private static final String CURRENT_HOSTS_1 = "current_host1";
    private static final String CURRENT_HOSTS_2 = "current_host2";
    private static final String ALL_HOSTS = "@allhosts";
    private static final String ANOTHER_HOSTS = "allAnotherHosts";

    private static final String QCONF_PE_DELETION = "qconf_PE_deletion";
    private static final String DELETE_PE_KEY = "-dp";
    private static final String PE_NAME_CONTEXT_VAR = "parallelEnvName";
    private static final String EXPECTED_PE_NAME = "make";
    private static final String CREATE_PE_KEY = "-Ap";
    private static final String PE_CREATION_COMMAND = "pe_registration_command";

    private static final String QCONF_SQL = "qconf_sql";
    private static final String QCONF_DQ = "qconf_dq";
    private static final String QCONF_AQ = "qconf_aq";
    private static final String QCONF_MQ = "qconf_Mq";
    private static final String SQL = "-sql";
    private static final String SQ = "-sq";
    private static final String DQ = "-dq";
    private static final String AQ = "-Aq";
    private static final String MQ = "-Mq";
    private static final String QUEUE_FIELD_NAME = "queue";
    private static final String TEMP_FILE_FIELD_NAME = "tempFile";
    private static final String QNAME_FILED = "qname";
    private static final String HOST_LIST_FIELD = "hostList";
    private static final String PE_LIST_FIELD = "peList";
    private static final String OWNER_LIST_FIELD = "ownerList";
    private static final String USER_LIST_FIELD = "userList";

    private static final String QDEL_COMMAND = "qdel";
    private static final String FORCED_QDEL = "-f";
    private static final String USER_QDEL = "-u";

    private static final String TEMP_FILE_PATH = "some/temp/file/path";
    private static final String QUEUE_ENTITY = "queue";
    private static final String SOME_HOSTLIST = SOME_HOST + " " + SOME_HOST2;
    private static final String PE_LIST_DEFAULT = "make smp mpi";
    private static final String OWNER_LIST_DEFAULT = "NONE";
    private static final String USER_LIST_DEFAULT = "arusers";

    private static final String CREATED_QUEUE =
                     "qname                 " + QUEUE + NEW_LINE_DELIMITER
                   + "hostlist              " + SOME_HOSTLIST + NEW_LINE_DELIMITER
                   + "seq_no                0" + NEW_LINE_DELIMITER
                   + "load_thresholds       np_load_avg=1.75" + NEW_LINE_DELIMITER
                   + "suspend_thresholds    NONE" + NEW_LINE_DELIMITER
                   + "nsuspend              1" + NEW_LINE_DELIMITER
                   + "suspend_interval      00:05:00" + NEW_LINE_DELIMITER
                   + "priority              0" + NEW_LINE_DELIMITER
                   + "min_cpu_interval      00:05:00" + NEW_LINE_DELIMITER
                   + "processors            1" + NEW_LINE_DELIMITER
                   + "qtype                 BATCH INTERACTIVE" + NEW_LINE_DELIMITER
                   + "ckpt_list             NONE" + NEW_LINE_DELIMITER
                   + "pe_list               " + PE_LIST_DEFAULT + NEW_LINE_DELIMITER
                   + "rerun                 FALSE" + NEW_LINE_DELIMITER
                   + "slots                 1" + NEW_LINE_DELIMITER
                   + "tmpdir                /tmp" + NEW_LINE_DELIMITER
                   + "shell                 /bin/sh" + NEW_LINE_DELIMITER
                   + "prolog                NONE" + NEW_LINE_DELIMITER
                   + "epilog                NONE" + NEW_LINE_DELIMITER
                   + "shell_start_mode      posix_compliant" + NEW_LINE_DELIMITER
                   + "starter_method        NONE" + NEW_LINE_DELIMITER
                   + "suspend_method        NONE" + NEW_LINE_DELIMITER
                   + "resume_method         NONE" + NEW_LINE_DELIMITER
                   + "terminate_method      NONE" + NEW_LINE_DELIMITER
                   + "notify                00:00:60" + NEW_LINE_DELIMITER
                   + "owner_list            " + OWNER_LIST_DEFAULT + NEW_LINE_DELIMITER
                   + "user_lists            " + USER_LIST_DEFAULT + NEW_LINE_DELIMITER
                   + "xuser_lists           NONE" + NEW_LINE_DELIMITER
                   + "subordinate_list      NONE" + NEW_LINE_DELIMITER
                   + "complex_values        NONE" + NEW_LINE_DELIMITER
                   + "projects              NONE" + NEW_LINE_DELIMITER
                   + "xprojects             NONE" + NEW_LINE_DELIMITER
                   + "calendar              NONE" + NEW_LINE_DELIMITER
                   + "initial_state         default" + NEW_LINE_DELIMITER
                   + "s_rt                  INFINITY" + NEW_LINE_DELIMITER
                   + "h_rt                  INFINITY" + NEW_LINE_DELIMITER
                   + "s_cpu                 INFINITY" + NEW_LINE_DELIMITER
                   + "h_cpu                 INFINITY" + NEW_LINE_DELIMITER
                   + "s_fsize               INFINITY" + NEW_LINE_DELIMITER
                   + "h_fsize               INFINITY" + NEW_LINE_DELIMITER
                   + "s_data                INFINITY" + NEW_LINE_DELIMITER
                   + "h_data                INFINITY" + NEW_LINE_DELIMITER
                   + "s_stack               INFINITY" + NEW_LINE_DELIMITER
                   + "h_stack               INFINITY" + NEW_LINE_DELIMITER
                   + "s_core                INFINITY" + NEW_LINE_DELIMITER
                   + "h_core                INFINITY" + NEW_LINE_DELIMITER
                   + "s_rss                 INFINITY" + NEW_LINE_DELIMITER
                   + "h_rss                 INFINITY" + NEW_LINE_DELIMITER
                   + "s_vmem                INFINITY" + NEW_LINE_DELIMITER
                   + "h_vmem                INFINITY" + NEW_LINE_DELIMITER;
    private static final String QSUB = "qsub";
    private static final String JOB_COMMAND = "simple.sh";
    private static final String BINARY_OPTION = "-b";
    private static final String IS_BINARY_OPTION = "y";
    private static final String USE_ALL_VARS_OPTION = "-V";
    private static final String NAME_OPTION = "-N";
    private static final String JOB_NAME = "myJob";
    private static final String WORK_DIR_OPTION = "-wd";
    private static final String WORK_DIR_NAME = "dir";
    private static final String ERR_PATH_OPTION = "-e";
    private static final String OUT_PATH_OPTION = "-o";
    private static final String PRIORITY_OPTION = "-p";
    private static final String PRIORITY = "1";
    private static final String QUEUES_OPTION = "-q";
    private static final String ENV_VAR_OPTION = "-v";
    private static final String ENV_VAR_KEY = "myVarKey";
    private static final String ENV_VAR_VALUE = "some value with spaces";
    private static final String ENV_VAR_MAP_ENTRY = String.format("%s=\"%s\"", ENV_VAR_KEY, ENV_VAR_VALUE);
    private static final String ENV_VAR_MAP_ONLY_KEY = ENV_VAR_KEY;
    private static final String PARALLEL_ENV_OPTION = "-pe";
    private static final String PE_NAME = "smp";
    private static final String PE_MIN_PARAM = "1";
    private static final String PE_MAX_PARAM = "100";
    private static final String PE_PARAM_SPLITTER = "-";
    private static final String ARG1 = "arg1";
    private static final String ARG2 = "arg2";
    private static final String OPTIONS = "options";
    private static final String ARGUMENTS = "arguments";
    private static final String ENV_VARIABLES = "envVariables";

    private static final String LOG_DIR = "logDir";
    private static final String SOME_PATH_TEMPLATE = "/home/%d.err";
    private static final String SOME_LOG_DIR = "/my/log/dir";
    private static final String ERR_LOG_PATH = SOME_LOG_DIR + "/$JOB_ID.err";
    private static final String OUT_LOG_PATH = SOME_LOG_DIR + "/$JOB_ID.out";

    private static final String PATH = "path";
    private static final String N_KEY = "-n";

    private static final String QCONF_SPL = "qconf_spl";
    private static final String QCONF_SP = "qconf_sp";
    private static final String SPL = "-spl";
    private static final String SP = "-sp";
    private static final String PE_MAKE = "make";
    private static final String PE_MSI = "msi";

    private static final String QSTAT_COMMAND = "qstat";
    private static final String USER = "-u";
    private static final String PENDING = "p";
    private static final String RUNNING = "r";
    private static final String SUSPENDED = "s";
    private static final String ZOMBIE = "z";
    private static final String USER_HOLD = "hu";
    private static final String OPERATOR_HOLD = "ho";
    private static final String SYSTEM_HOLD = "hs";
    private static final String DEPENDENCY_HOLD = "hd";
    private static final String ARRAY_HOLD = "ha";
    private static final String ALL_HOLDS = "h";
    private static final String ALL_STATES = "a";
    private static final String SOMEUSER = "someuser";
    private static final String ANOTHERUSER = "anotheruser";
    private static final String STATE = "-s";
    private static final String SGEUSER = "sgeuser";
    private static final String JOB_FILTER = "filter";
    private static final String JOB_STATE = "state";

    private static final GridEngineCommandCompiler commandCompiler = getCommandCompiler();

    public static GridEngineCommandCompiler getCommandCompiler() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(getResolverByCommandPath(getPropertyValue(TEMPLATE_PATH_PROPERTY)));

        return new GridEngineCommandCompilerImpl(templateEngine);
    }

    @ParameterizedTest
    @MethodSource("provideQconfCommands")
    public void shouldReturnCommandWithSelectedHostGroup(final Context context,
                                                         final String[] sample) {
        final String[] command = commandCompiler.compileCommand(CommandType.SGE, QCONF_COMMAND, context);
        Assertions.assertEquals(Arrays.toString(sample), Arrays.toString(command));
    }

    static Stream<Arguments> provideQconfCommands() {
        final Context noArguments = new Context();

        final Context oneArgument = new Context();
        oneArgument.setVariable(FILTER_FIELD_NAME, new HostGroupFilter(List.of(ALL_HOSTS)));

        final Context twoArguments = new Context();
        twoArguments.setVariable(FILTER_FIELD_NAME, new HostGroupFilter(List.of(ALL_HOSTS, ANOTHER_HOSTS)));

        return Stream.of(
                Arguments.of(noArguments, new String[]{QCONF_COMMAND, SHGRPL_OPTION}),
                Arguments.of(oneArgument, new String[]{QCONF_COMMAND, SHGRP_OPTION, ALL_HOSTS}),
                Arguments.of(twoArguments,
                        new String[]{QCONF_COMMAND, SHGRP_OPTION, ALL_HOSTS, SHGRP_OPTION, ANOTHER_HOSTS})
        );
    }

    @Test
    public void shouldReturnSimpleHostCommand() {
        final String[] sample = new String[]{QHOST_COMMAND, TYPE_XML};
        final String[] command = commandCompiler.compileCommand(CommandType.SGE, QHOST_COMMAND, new Context());
        Assertions.assertEquals(Arrays.toString(sample), Arrays.toString(command));
    }

    @Test
    public void shouldReturnCommandWithSelectedHost() {
        final Context hostFilter = new Context();
        hostFilter.setVariable(FILTER_FIELD_NAME, new HostFilter(List.of(CURRENT_HOSTS)));
        final String[] sample = new String[]{QHOST_COMMAND, H_COMMAND, CURRENT_HOSTS, TYPE_XML};
        final String[] command = commandCompiler.compileCommand(CommandType.SGE, QHOST_COMMAND, hostFilter);
        Assertions.assertEquals(Arrays.toString(sample), Arrays.toString(command));
    }

    @Test
    public void shouldReturnCommandWithSelectedHosts() {
        final Context hostFilter = new Context();
        hostFilter.setVariable(FILTER_FIELD_NAME, new HostFilter(List.of(CURRENT_HOSTS_1, CURRENT_HOSTS_2)));
        final String[] sample = new String[]{QHOST_COMMAND, H_COMMAND, CURRENT_HOSTS_1, CURRENT_HOSTS_2, TYPE_XML};
        final String[] command = commandCompiler.compileCommand(CommandType.SGE, QHOST_COMMAND, hostFilter);
        Assertions.assertEquals(Arrays.toString(sample), Arrays.toString(command));
    }

    @Test
    public void shouldReturnOnlyQacctCommandWhenGetEmptyRequest() {
        Assertions.assertEquals(QACCT_COMMAND,
                commandCompiler.compileCommand(CommandType.SGE, QACCT_COMMAND, new Context())[0]);
    }

    @ParameterizedTest
    @MethodSource("provideQacctJobFilterWithOneFieldAndExpectedCommand")
    public void shouldReturnQacctCommandWithSelectedKey(final Context filter, final String[] command) {
        assertArrayEquals(command, commandCompiler.compileCommand(CommandType.SGE, QACCT_COMMAND, filter));
    }

    static Stream<Arguments> provideQacctJobFilterWithOneFieldAndExpectedCommand() {
        final Context reportFilterOwner = new Context();
        reportFilterOwner.setVariable(FILTER_FIELD_NAME, UsageReportFilter.builder().owner(OWNER).build());

        final Context reportFilterQueue = new Context();
        reportFilterQueue.setVariable(FILTER_FIELD_NAME, UsageReportFilter.builder().queue(QUEUE).build());

        final Context reportFilterStart = new Context();
        reportFilterStart.setVariable(START_TIME_FIELD, EXPECTED_START_DATE_TIME);

        final Context reportFilterEnd = new Context();
        reportFilterEnd.setVariable(END_TIME_FIELD, EXPECTED_END_DATE_TIME);

        final Context reportFilterDays = new Context();
        reportFilterDays.setVariable(FILTER_FIELD_NAME, UsageReportFilter.builder().days(DAYS).build());

        final Context reportFilterParEnv = new Context();
        reportFilterParEnv.setVariable(FILTER_FIELD_NAME,
                UsageReportFilter.builder().parallelEnv(PARALLEL_ENV).build());

        return Stream.of(
                Arguments.of(reportFilterOwner, new String[]{QACCT_COMMAND, OWNER_KEY, OWNER}),
                Arguments.of(reportFilterQueue, new String[]{QACCT_COMMAND, QUEUE_KEY, QUEUE}),
                Arguments.of(reportFilterStart,
                        new String[]{QACCT_COMMAND, START_DATE_TIME_KEY, EXPECTED_START_DATE_TIME}),
                Arguments.of(reportFilterEnd, new String[]{QACCT_COMMAND, END_DATE_TIME_KEY, EXPECTED_END_DATE_TIME}),
                Arguments.of(reportFilterDays, new String[]{QACCT_COMMAND, DAYS_KEY, String.valueOf(DAYS)}),
                Arguments.of(reportFilterParEnv, new String[]{QACCT_COMMAND, PARALLEL_ENV_KEY, PARALLEL_ENV})
        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForQpingCommandCreating")
    public void testQpingTemplateResolving(final String qmasterHost, final String qmasterPort) {
        final Context context = new Context();
        context.setVariable(QMASTER_HOST_STRING, qmasterHost);
        context.setVariable(QMASTER_PORT_STRING, qmasterPort);

        final String[] result = commandCompiler.compileCommand(CommandType.SGE, QPING_COMMAND, context);
        assertArrayEquals(new String[]{QPING_COMMAND, INFO_KEY, qmasterHost, qmasterPort, QMASTER_STRING, ONE}, result);
    }

    static Stream<Arguments> provideParametersForQpingCommandCreating() {
        return IntStream.range(1, 4).mapToObj(Integer::toString).map(index -> Arguments.of(SOME_HOST + index, index));
    }

    @ParameterizedTest
    @MethodSource("provideParametersForQmasterDefiningCommandCreating")
    public void testQmasterDefiningTemplateResolving(final String qmasterHostPath) {
        final Context context = new Context();
        context.setVariable(QMASTER_HOST_PATH_STRING, qmasterHostPath);

        final String[] result = commandCompiler.compileCommand(CommandType.SGE, DEFINE_QMASTER_COMMAND, context);
        assertArrayEquals(new String[]{CAT_COMMAND, qmasterHostPath}, result);
    }

    static Stream<Arguments> provideParametersForQmasterDefiningCommandCreating() {
        return Stream.of(
                        "/home",
                        "/opt/sge",
                        "/opt/sge/default",
                        "/opt/sge/default/common")
                .map(Arguments::of);
    }

    @Test
    public void shouldReturnPeDeletionCommandWithPeName() {
        final Context parallelEnvName = new Context();
        parallelEnvName.setVariable(PE_NAME_CONTEXT_VAR, EXPECTED_PE_NAME);
        final String[] expected = new String[]{QCONF_COMMAND, DELETE_PE_KEY, EXPECTED_PE_NAME};
        final String[] actual = commandCompiler.compileCommand(CommandType.SGE, QCONF_PE_DELETION, parallelEnvName);
        assertArrayEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideParametersForQueueListingCommandCreation")
    public void shouldReturnQueueListingCommand(final List<String> queues, final String[] command) {
        final Context context = new Context();
        context.setVariable(FILTER_FIELD_NAME,
                QueueFilter.builder().queues(queues).build());
        assertArrayEquals(command, commandCompiler.compileCommand(CommandType.SGE, QCONF_SQL, context));
    }

    static Stream<Arguments> provideParametersForQueueListingCommandCreation() {
        final List<String> listOfQueues = List.of(QUEUE, QUEUE2);
        final List<String> emptyListOfQueues = Collections.emptyList();

        return Stream.of(
                Arguments.of(listOfQueues, new String[]{QCONF_COMMAND, SQ, QUEUE, QUEUE2}),
                Arguments.of(emptyListOfQueues, new String[]{QCONF_COMMAND, SQL}),
                Arguments.of(null, new String[]{QCONF_COMMAND, SQL})
        );
    }

    @Test
    public void shouldReturnQueueDeletionCommand() {
        final Context context = new Context();
        context.setVariable(QUEUE_FIELD_NAME, QUEUE);
        final String[] command = new String[]{QCONF_COMMAND, DQ, QUEUE};
        assertArrayEquals(command, commandCompiler.compileCommand(CommandType.SGE, QCONF_DQ, context));
    }

    @Test
    public void shouldReturnQueueCreationCommand() {
        final Context context = new Context();
        context.setVariable(TEMP_FILE_FIELD_NAME, TEMP_FILE_PATH);
        final String[] command = new String[]{QCONF_COMMAND, AQ, TEMP_FILE_PATH};
        assertArrayEquals(command, commandCompiler.compileCommand(CommandType.SGE, QCONF_AQ, context));
    }

    @Test
    public void shouldReturnQueueUpdateCommand() {
        final Context context = new Context();
        context.setVariable(TEMP_FILE_FIELD_NAME, TEMP_FILE_PATH);
        final String[] command = new String[]{QCONF_COMMAND, MQ, TEMP_FILE_PATH};
        assertArrayEquals(command, commandCompiler.compileCommand(CommandType.SGE, QCONF_MQ, context));
    }

    @ParameterizedTest
    @MethodSource("provideParametersForCompilingEntityConfigFile")
    public void shouldCompileEntityConfigFile(final CommandType commandType, final String entity,
                                              final Context context, final String expected) {
        final Path pathToTempFile = commandCompiler.compileEntityConfigFile(commandType, entity, context);

        Assertions.assertNotEquals(null, pathToTempFile);
        Assertions.assertTrue(pathToTempFile.isAbsolute());

        String actual;
        try {
            actual = Files.readString(pathToTempFile);
            Files.deleteIfExists(pathToTempFile);
        } catch (final IOException e) {
            actual = null;
        }
        Assertions.assertEquals(expected, actual);
    }

    static Stream<Arguments> provideParametersForCompilingEntityConfigFile() {
        final Context contextForCreateQueue = new Context();
        contextForCreateQueue.setVariable(QNAME_FILED, QUEUE);
        contextForCreateQueue.setVariable(HOST_LIST_FIELD, SOME_HOSTLIST);
        contextForCreateQueue.setVariable(PE_LIST_FIELD, PE_LIST_DEFAULT);
        contextForCreateQueue.setVariable(OWNER_LIST_FIELD, OWNER_LIST_DEFAULT);
        contextForCreateQueue.setVariable(USER_LIST_FIELD, USER_LIST_DEFAULT);

        return Stream.of(
                Arguments.of(CommandType.SGE, QUEUE_ENTITY, contextForCreateQueue, CREATED_QUEUE)
        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForGetLogLinesCommandCreating")
    public void testGetLogLinesTemplateResolving(final String path, final String lines,
                                                 final boolean fromHead, final String linuxCommand) {
        final Context context = new Context();
        context.setVariable(PATH, path);
        context.setVariable("path", path);
        context.setVariable("lines", lines);
        context.setVariable("fromHead", fromHead);
        final String[] result = commandCompiler.compileCommand(CommandType.COMMON, "get_log_lines", context);

        assertArrayEquals(new String[]{linuxCommand, N_KEY, lines, path}, result);
    }

    static Stream<Arguments> provideParametersForGetLogLinesCommandCreating() {
        return Stream.of(
                Arguments.of(String.format(SOME_PATH_TEMPLATE, 1), "10", false, "tail"),
                Arguments.of(String.format(SOME_PATH_TEMPLATE, 2), "20", true, "head"));
    }

    @ParameterizedTest
    @MethodSource("provideParametersForGetLogFileInfoCommandCreating")
    public void testGetLogFileInfoTemplateResolving(final String path) {
        final Context context = new Context();
        context.setVariable(PATH, path);
        final String[] result = commandCompiler.compileCommand(CommandType.COMMON, "get_logfile_info", context);

        assertArrayEquals(new String[]{"wc", "-l", "-c", path}, result);
    }

    static Stream<Arguments> provideParametersForGetLogFileInfoCommandCreating() {
        return IntStream.range(1, 4)
                .mapToObj(index -> Arguments.of(String.format(SOME_PATH_TEMPLATE, index)));
    }

    @Test
    public void shouldReturnPeListingCommandSpl() {
        final String[] actual = commandCompiler.compileCommand(CommandType.SGE, QCONF_SPL, new Context());
        assertArrayEquals(new String[]{QCONF_COMMAND, SPL}, actual);
    }

    @Test
    public void shouldReturnPeListingCommandSp() {
        final ParallelEnvFilter peFilter = ParallelEnvFilter.builder().parallelEnvs(List.of(PE_MAKE, PE_MSI)).build();
        final Context context = new Context();
        context.setVariable(FILTER_FIELD_NAME, peFilter);
        final String[] actual = commandCompiler.compileCommand(CommandType.SGE, QCONF_SP, context);
        assertArrayEquals(new String[]{QCONF_COMMAND, SP, PE_MAKE, SP, PE_MSI}, actual);
    }

    @ParameterizedTest
    @MethodSource("provideValidParameters")
    public void shouldMakeValidQsubCommand(final JobOptions.JobOptionsBuilder jobOptionsBuilder,
                                           final String[] expectedCommand) {
        final JobOptions jobOptions = jobOptionsBuilder.build();
        final Context context = new Context();
        context.setVariable(OPTIONS, jobOptions);
        context.setVariable(ARGUMENTS, CommandArgUtils.toEscapeQuotes(jobOptions.getArguments()));
        assertArrayEquals(expectedCommand, commandCompiler.compileCommand(CommandType.SGE, QSUB, context));
    }

    static Stream<Arguments> provideValidParameters() {
        return Stream.of(
                Arguments.of(getSimpleJobCommand().canBeBinary(true),
                        new String[]{QSUB, BINARY_OPTION, IS_BINARY_OPTION, JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand().useAllEnvVars(true),
                        new String[]{QSUB, USE_ALL_VARS_OPTION, JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand().name(JOB_NAME),
                        new String[]{QSUB, NAME_OPTION, JOB_NAME, JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand().workingDir(WORK_DIR_NAME),
                        new String[]{QSUB, WORK_DIR_OPTION, WORK_DIR_NAME, JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand().priority(Long.parseLong(PRIORITY)),
                        new String[]{QSUB, PRIORITY_OPTION, PRIORITY, JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand().queues(List.of(QUEUE, QUEUE2)),
                        new String[]{QSUB, QUEUES_OPTION, QUEUE, QUEUE2, JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand().parallelEnvOptions(new ParallelEnvOptions(PE_NAME, 1, 100)),
                        new String[]{QSUB, PARALLEL_ENV_OPTION, PE_NAME,
                                PE_MIN_PARAM.concat(PE_PARAM_SPLITTER).concat(PE_MAX_PARAM), JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand().parallelEnvOptions(new ParallelEnvOptions(PE_NAME, 1, 0)),
                        new String[]{QSUB, PARALLEL_ENV_OPTION, PE_NAME, PE_MIN_PARAM, JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand().arguments(List.of(ARG1, ARG2)),
                        new String[]{QSUB, JOB_COMMAND, ARG1, ARG2})
        );
    }

    @Test
    public void shouldMakeValidQsubCommandWithLogDirParam() {
        final Context context = new Context();
        context.setVariable(OPTIONS, getSimpleJobCommand().build());
        context.setVariable(LOG_DIR, SOME_LOG_DIR);
        final String[] expectedCommand = new String[]{QSUB, ERR_PATH_OPTION, ERR_LOG_PATH,
                                                      OUT_PATH_OPTION, OUT_LOG_PATH, JOB_COMMAND};

        assertArrayEquals(expectedCommand, commandCompiler.compileCommand(CommandType.SGE, QSUB, context));
    }

    @ParameterizedTest
    @MethodSource("provideValidParametersWithEnvVariables")
    public void shouldMakeValidQsubCommandWithEnvVariables(final JobOptions.JobOptionsBuilder jobOptionsBuilder,
                                                           final String envVariables,
                                                           final String[] expectedCommand) {
        final JobOptions jobOptions = jobOptionsBuilder.build();
        final Context context = new Context();
        context.setVariable(OPTIONS, jobOptions);
        context.setVariable(ENV_VARIABLES, CommandArgUtils.toEscapeQuotes(envVariables));
        assertArrayEquals(expectedCommand, commandCompiler.compileCommand(CommandType.SGE, QSUB, context));
    }

    static Stream<Arguments> provideValidParametersWithEnvVariables() {
        return Stream.of(
                Arguments.of(getSimpleJobCommand(), ENV_VAR_MAP_ENTRY,
                        new String[]{QSUB, ENV_VAR_OPTION, ENV_VAR_MAP_ENTRY, JOB_COMMAND}),
                Arguments.of(getSimpleJobCommand(), ENV_VAR_MAP_ONLY_KEY,
                        new String[]{QSUB, ENV_VAR_OPTION, ENV_VAR_MAP_ONLY_KEY, JOB_COMMAND})
        );
    }

    @Test
    public void shouldReturnPeRegistrationCommand() {
        final Context context = new Context();
        context.setVariable(TEMP_FILE_FIELD_NAME, TEMP_FILE_PATH);
        final String[] command = new String[]{QCONF_COMMAND, CREATE_PE_KEY, TEMP_FILE_PATH};
        assertArrayEquals(command, commandCompiler.compileCommand(CommandType.SGE, PE_CREATION_COMMAND, context));
    }

    private static String getPropertyValue(final String propertyName) {
        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(PROPERTIES_PATH));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(propertyName);
    }

    private static AbstractConfigurableTemplateResolver getResolverByCommandPath(final String commandPath) {
        final File file = new File(commandPath);
        final AbstractConfigurableTemplateResolver resolver = file.exists() && file.isDirectory()
                ? new FileTemplateResolver()
                : new ClassLoaderTemplateResolver();
        resolver.setPrefix(commandPath);
        resolver.setTemplateMode(TemplateMode.TEXT);
        return resolver;
    }

    private static JobOptions.JobOptionsBuilder getSimpleJobCommand() {
        return JobOptions.builder().command(JOB_COMMAND);
    }

    @ParameterizedTest
    @MethodSource("provideGoodParameters")
    public void shouldMakeRightQstatCommand(final String state, final List<String> owners,
                                            final String[] expectedCommand) {
        final JobFilter jobFilter = JobFilter.builder()
                .state(state)
                .owners(owners)
                .build();
        final Context context = new Context();
        context.setVariable(JOB_FILTER, jobFilter);
        if (jobFilter != null) {
            final String jobState = Optional.ofNullable(jobFilter.getState())
                    .filter(StringUtils::hasText)
                    .map(QstatCommandParser::getStateFromStateMap)
                    .orElse(null);
            context.setVariable(JOB_STATE, jobState);
        }
        assertArrayEquals(expectedCommand,
                commandCompiler.compileCommand(CommandType.SGE, QSTAT_COMMAND, context));
    }

    static Stream<Arguments> provideGoodParameters() {
        return Stream.of(
                Arguments.of(null, Collections.singletonList(SGEUSER),
                        new String[]{QSTAT_COMMAND, USER, SGEUSER, TYPE_XML}),
                Arguments.of(null, List.of(SGEUSER, SOMEUSER, ANOTHERUSER),
                        new String[]{QSTAT_COMMAND, USER, SGEUSER, SOMEUSER, ANOTHERUSER, TYPE_XML}),
                Arguments.of(PENDING_STRING, null, new String[]{QSTAT_COMMAND, STATE, PENDING, TYPE_XML}),
                Arguments.of(RUNNING_STRING, null, new String[]{QSTAT_COMMAND, STATE, RUNNING, TYPE_XML}),
                Arguments.of(SUSPENDED_STRING, null, new String[]{QSTAT_COMMAND, STATE, SUSPENDED, TYPE_XML}),
                Arguments.of(ZOMBIE_STRING, null, new String[]{QSTAT_COMMAND, STATE, ZOMBIE, TYPE_XML}),
                Arguments.of(USER_HOLD, null, new String[]{QSTAT_COMMAND, STATE, USER_HOLD, TYPE_XML}),
                Arguments.of(OPERATOR_HOLD, null, new String[]{QSTAT_COMMAND, STATE, OPERATOR_HOLD, TYPE_XML}),
                Arguments.of(SYSTEM_HOLD, null, new String[]{QSTAT_COMMAND, STATE, SYSTEM_HOLD, TYPE_XML}),
                Arguments.of(DEPENDENCY_HOLD, null, new String[]{QSTAT_COMMAND, STATE, DEPENDENCY_HOLD, TYPE_XML}),
                Arguments.of(ARRAY_HOLD, null, new String[]{QSTAT_COMMAND, STATE, ARRAY_HOLD, TYPE_XML}),
                Arguments.of(ALL_HOLDS, null, new String[]{QSTAT_COMMAND, STATE, ALL_HOLDS, TYPE_XML}),
                Arguments.of(ALL_STATES, null, new String[]{QSTAT_COMMAND, STATE, ALL_STATES, TYPE_XML}),
                Arguments.of(EMPTY_STRING, null, new String[]{QSTAT_COMMAND, TYPE_XML}),
                Arguments.of(SPACE, null, new String[]{QSTAT_COMMAND, TYPE_XML}),
                Arguments.of(PENDING_STRING, List.of(SGEUSER, SOMEUSER, ANOTHERUSER),
                        new String[]{QSTAT_COMMAND, USER, SGEUSER, SOMEUSER, ANOTHERUSER, STATE, PENDING, TYPE_XML})
        );
    }

    @Test
    public void shouldMakeRightQstatCommandWithNullFilter() {
        final Context context = new Context();
        context.setVariable(JOB_FILTER, null);
        assertArrayEquals(new String[]{QSTAT_COMMAND, TYPE_XML},
                commandCompiler.compileCommand(CommandType.SGE, QSTAT_COMMAND, context));
    }

    @ParameterizedTest
    @MethodSource("provideDeleteJobFilterAndExpectedCommand")
    public void shouldMakeRightQdelCommand(final DeleteJobFilter filter,
                                           final String[] expectedCommand) {
        final Context context = new Context();
        context.setVariable(JOB_FILTER, filter);
        assertArrayEquals(expectedCommand,
                commandCompiler.compileCommand(CommandType.SGE, QDEL_COMMAND, context));
    }

    static Stream<Arguments> provideDeleteJobFilterAndExpectedCommand() {
        return Stream.of(
                Arguments.of(
                        DeleteJobFilter.builder().force(true).id(1L).user(SGEUSER).build(),
                        new String[]{QDEL_COMMAND, FORCED_QDEL, USER_QDEL, SGEUSER, ONE}),
                Arguments.of(
                        DeleteJobFilter.builder().force(false).id(1L).user(SGEUSER).build(),
                        new String[]{QDEL_COMMAND, USER_QDEL, SGEUSER, ONE}),
                Arguments.of(
                        DeleteJobFilter.builder().force(false).user(SGEUSER).build(),
                        new String[]{QDEL_COMMAND, USER_QDEL, SGEUSER}),
                Arguments.of(
                        DeleteJobFilter.builder().force(false).id(1L).build(),
                        new String[]{QDEL_COMMAND, ONE})
        );
    }
}
