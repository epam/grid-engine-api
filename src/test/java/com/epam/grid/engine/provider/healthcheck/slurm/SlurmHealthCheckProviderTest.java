package com.epam.grid.engine.provider.healthcheck.slurm;

import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.healthcheck.GridEngineStatus;
import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.entity.healthcheck.StatusInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;
import static com.epam.grid.engine.utils.TextConstants.NEW_LINE_DELIMITER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(properties = {"grid.engine.type=SLURM"})
public class SlurmHealthCheckProviderTest {

    private static final String NOT_PROVIDED_OUT = "some key    = some values" + NEW_LINE_DELIMITER
            + "BOOT_TIME               = 2022-03-28T00:07:55";
    private static final String ERROR_OUT = "some key    = some values" + NEW_LINE_DELIMITER
            + "BOOT_TIME               = 2022-03-28T00:07:55" + NEW_LINE_DELIMITER
            + "Slurmctld(primary) at slurmctld is DOWN";
    private static final String OK_OUT = "some key    = some values" + NEW_LINE_DELIMITER
            + "BOOT_TIME               = 2022-03-28T00:07:55" + NEW_LINE_DELIMITER
            + "Slurmctld(primary) at slurmctld is UP";

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;
    @Autowired
    private SlurmHealthCheckProvider slurmHealthCheckProvider;

    @ParameterizedTest
    @MethodSource("provideCorrectStdOutAndExpectedHealthConfig")
    public void shouldReturnCorrectHealthConfig(
            final long code,
            final GridEngineStatus gridEngineStatus,
            final String info,
            final LocalDateTime startTime,
            final LocalDateTime checkTime,
            final List<String> stdOut
    ) {
        final HealthCheckInfo expectedHealthCheckInfo = HealthCheckInfo.builder()
                .statusInfo(StatusInfo.builder()
                        .code(code)
                        .status(gridEngineStatus)
                        .info(info)
                        .build())
                .startTime(startTime)
                .checkTime(checkTime)
                .build();

        final CommandResult showConfigCommandResult = new CommandResult();
        showConfigCommandResult.setStdOut(stdOut);
        showConfigCommandResult.setStdErr(EMPTY_LIST);

        doReturn(showConfigCommandResult).when(mockCmdExecutor).execute(Mockito.any());

        final HealthCheckInfo result = slurmHealthCheckProvider.checkHealth();
        assertEquals(expectedHealthCheckInfo, result);
    }

    static Stream<Arguments> provideCorrectStdOutAndExpectedHealthConfig() {
        return Stream.of(
                Arguments.of(0L, GridEngineStatus.OK, OK_OUT,
                        LocalDateTime.of(2022, 12, 31, 23, 59, 59),
                        LocalDateTime.of(2022, 03, 28, 00, 38, 26),
                        List.of("Configuration data as of 2022-03-28T00:38:26",
                                "BOOT_TIME               = 2022-12-31T23:59:59",
                                "status: OK", OK_OUT)),

                Arguments.of(99999L, GridEngineStatus.NOT_PROVIDED, NOT_PROVIDED_OUT,
                        LocalDateTime.of(1900, 1, 1, 12, 59, 59),
                        LocalDateTime.of(2221, 5, 5, 00, 00, 01),
                        List.of("Configuration data as of 2221-05-05T00:00:01",
                                "BOOT_TIME               = 1900-01-01T12:59:59",
                                "status: NOT_PROVIDED", NOT_PROVIDED_OUT)),

                Arguments.of(99999L, GridEngineStatus.NOT_PROVIDED, EMPTY_STRING,
                        LocalDateTime.of(2002, 8, 18, 19, 39, 39),
                        LocalDateTime.of(2001, 10, 20, 10, 20, 30),
                        List.of("Configuration data as of 2001-10-20T10:20:30",
                                "BOOT_TIME               = 2002-08-18T19:39:39",
                                "status: NOT_PROVIDED", EMPTY_STRING)),

                Arguments.of(2L, GridEngineStatus.ERROR, ERROR_OUT,
                        LocalDateTime.of(1999, 2, 28, 10, 30, 30),
                        LocalDateTime.of(2021, 12, 30, 20, 50, 50),
                        List.of("Configuration data as of 2021-12-30T20:50:50",
                                "BOOT_TIME               = 1999-02-28T10:30:30",
                                "status: ERROR", ERROR_OUT))
        );
    }
}
