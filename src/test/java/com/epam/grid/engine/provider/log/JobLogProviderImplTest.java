package com.epam.grid.engine.provider.log;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.job.JobLogInfo;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.utils.DirectoryPathUtils;
import com.epam.grid.engine.provider.utils.sge.TestSgeConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class JobLogProviderImplTest {

    private static final String GET_LOG_LINES_COMMAND = "get_log_lines";
    private static final String GET_LOGFILE_INFO_COMMAND = "get_logfile_info";

    private static final long SOME_JOB_ID = 10;
    private static final int SOME_LINES = 10;
    private static final int SOME_BYTES = 150;
    private static final JobLogInfo.Type SOME_LOG_TYPE = JobLogInfo.Type.ERR;
    private static final String SOME_LOG_DIR = "/logs";
    private static final String SOME_SHARED_FOLDER = "/mnt/shared_folder/";
    private static final String LOG_FILE_NAME = String.format("%d.%s", SOME_JOB_ID, SOME_LOG_TYPE.getSuffix());
    private static final List<String> INFO_COMMAND_RESULT_STDOUT = Collections.singletonList(
            String.format("%d %d %s", SOME_LINES, SOME_BYTES, LOG_FILE_NAME));

    private static final MockedStatic<DirectoryPathUtils> pathUtilsStaticMock
            = Mockito.mockStatic(DirectoryPathUtils.class);
    private final SimpleCmdExecutor mockCmdExecutor = Mockito.mock(SimpleCmdExecutor.class);
    private final GridEngineCommandCompiler commandCompiler = Mockito.mock(GridEngineCommandCompiler.class);
    private final JobLogProvider jobLogProvider = new JobLogProviderImpl(mockCmdExecutor, commandCompiler,
            SOME_LOG_DIR, SOME_SHARED_FOLDER);

    @BeforeAll
    static void configurePathUtilsStaticMock() {
        pathUtilsStaticMock.when(() -> DirectoryPathUtils.resolvePathToAbsolute(Mockito.any(), Mockito.any()))
                .thenReturn(Path.of(SOME_SHARED_FOLDER, SOME_LOG_DIR).toString());
    }

    @AfterAll
    static void clearPathUtilsStaticMock() {
        pathUtilsStaticMock.close();
    }

    @Test
    void shouldReturnCorrectObjectWhenGettingJobLogInfo() {
        final List<String> testStdOut = Collections.singletonList("Test line for StdOut.");
        final JobLogInfo expectedJobLogInfo = new JobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE,
                testStdOut, SOME_LINES, SOME_BYTES);

        final CommandResult infoCommandResult = new CommandResult();
        infoCommandResult.setStdOut(INFO_COMMAND_RESULT_STDOUT);
        infoCommandResult.setStdErr(TestSgeConstants.EMPTY_LIST);

        final CommandResult linesCommandResult = new CommandResult();
        linesCommandResult.setStdOut(testStdOut);
        linesCommandResult.setStdErr(TestSgeConstants.EMPTY_LIST);

        mockCommandCompilation(GET_LOGFILE_INFO_COMMAND, infoCommandResult, "wc", "-l", "-c", LOG_FILE_NAME);
        mockCommandCompilation(GET_LOG_LINES_COMMAND, linesCommandResult, "tail", "-n", "1", LOG_FILE_NAME);

        final JobLogInfo result = jobLogProvider.getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, 1, false);
        Assertions.assertEquals(expectedJobLogInfo, result);
    }

    @Test
    void shouldThrowExceptionWhenGettingJobLogInfoWithBadRequest() {
        final int someBadCountLines = -5;
        final GridEngineException thrown = Assertions.assertThrows(GridEngineException.class,
                () -> jobLogProvider.getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, someBadCountLines, false));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, thrown.getHttpStatus());
    }

    @ParameterizedTest
    @MethodSource("provideCommandResultsForGettingJobLogInfoTesting")
    void shouldThrowExceptionWhenGettingJobLogInfoWhenReceivedBadCommandResult(
            final int infoCommandResultStatus,
            final int linesCommandResultStatus,
            final List<String> infoCommandResultStdOut,
            final HttpStatus expectedHttpStatus) {
        final CommandResult infoCommandResult = new CommandResult();
        infoCommandResult.setStdOut(infoCommandResultStdOut);
        infoCommandResult.setExitCode(infoCommandResultStatus);

        final CommandResult linesCommandResult = new CommandResult();
        linesCommandResult.setExitCode(linesCommandResultStatus);

        mockCommandCompilation(GET_LOGFILE_INFO_COMMAND, infoCommandResult, "wc", "-l", "-c", LOG_FILE_NAME);
        mockCommandCompilation(GET_LOG_LINES_COMMAND, linesCommandResult, "tail", "-n", "1", LOG_FILE_NAME);

        final GridEngineException result = Assertions.assertThrows(GridEngineException.class,
                () -> jobLogProvider.getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, 1, false));
        Assertions.assertEquals(expectedHttpStatus, result.getHttpStatus());
    }

    static Stream<Arguments> provideCommandResultsForGettingJobLogInfoTesting() {
        return Stream.of(
                Arguments.of(0, 1, INFO_COMMAND_RESULT_STDOUT, HttpStatus.NOT_FOUND),
                Arguments.of(1, 0, INFO_COMMAND_RESULT_STDOUT, HttpStatus.NOT_FOUND),
                Arguments.of(1, 1, INFO_COMMAND_RESULT_STDOUT, HttpStatus.NOT_FOUND),
                Arguments.of(0, 0, Collections.singletonList(LOG_FILE_NAME), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private void mockCommandCompilation(final String command, final CommandResult commandResult,
                                        final String... compiledArray) {
        Mockito.doReturn(compiledArray).when(commandCompiler).compileCommand(Mockito.eq(CommandType.COMMON),
                Mockito.matches(command),
                Mockito.any());

        Mockito.doReturn(commandResult).when(mockCmdExecutor).execute(compiledArray);
    }
}
