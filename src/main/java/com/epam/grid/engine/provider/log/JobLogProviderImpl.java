package com.epam.grid.engine.provider.log;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.job.JobLogInfo;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.utils.DirectoryPathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.epam.grid.engine.utils.TextConstants.DOT;
import static com.epam.grid.engine.utils.TextConstants.SPACE;

@Slf4j
@Service
public class JobLogProviderImpl implements JobLogProvider {

    private static final String GET_LOG_LINES_COMMAND = "get_log_lines";
    private static final String GET_LOGFILE_INFO_COMMAND = "get_logfile_info";
    private static final String CANT_FIND_LOG_FILE = "Can't find the job with id = %d or the job log file.";
    private static final String CANT_PARSE_WC_CMD_RESPONSE = "Can't parse wc-command response on the server.";
    private static final String WC_COMMAND_REGEX_PATTERN = "^\\d+ \\d+ .+";

    /**
     * The command execution mechanism used.
     */
    private final SimpleCmdExecutor simpleCmdExecutor;

    /**
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

    /**
     * The path to the directory where all log files are stored
     * occurred when processing the job.
     */
    private final String logDir;

    public JobLogProviderImpl(final SimpleCmdExecutor simpleCmdExecutor,
                              final GridEngineCommandCompiler commandCompiler,
                              @Value("${job.log.dir}") final String logDir,
                              @Value("${grid.engine.shared.folder}") final String gridSharedFolder) {
        this.simpleCmdExecutor = simpleCmdExecutor;
        this.commandCompiler = commandCompiler;
        this.logDir = DirectoryPathUtils.resolvePathToAbsolute(gridSharedFolder, logDir);
    }

    /**
     * This method provides information about the log file and obtains the specified number of lines from it.
     *
     * @param jobId    The job identifier.
     * @param logType  The log file type to obtain information from.
     * @param lines    The number of lines.
     * @param fromHead if it's true, lines are taken from the head of the log file, otherwise from the tail.
     * @return The object of {@link JobLogInfo}
     */
    @Override
    public JobLogInfo getJobLogInfo(final long jobId, final JobLogInfo.Type logType,
                                    final int lines, final boolean fromHead) {
        if (lines < 0) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST,
                    String.format("The 'lines' parameter can't be < 0, received value = %d", lines));
        }
        final Context context = new Context();
        context.setVariable("path", getLogFilePath(jobId, logType));
        context.setVariable("lines", lines);
        context.setVariable("fromHead", fromHead);

        final CommandResult resultLogFileInfoCommand = simpleCmdExecutor.execute(
                commandCompiler.compileCommand(getProviderType(), GET_LOGFILE_INFO_COMMAND, context));
        final CommandResult resultLogLinesCommand = simpleCmdExecutor.execute(
                commandCompiler.compileCommand(getProviderType(), GET_LOG_LINES_COMMAND, context));

        if (resultLogFileInfoCommand.getExitCode() != 0 || resultLogLinesCommand.getExitCode() != 0) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, String.format(CANT_FIND_LOG_FILE, jobId));
        }
        final String wcCommandResult = resultLogFileInfoCommand.getStdOut().stream()
                .findFirst()
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        CANT_PARSE_WC_CMD_RESPONSE))
                .trim();
        if (!wcCommandResult.matches(WC_COMMAND_REGEX_PATTERN)) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, CANT_PARSE_WC_CMD_RESPONSE);
        }
        final String[] splitWcCommandResult = wcCommandResult.split(SPACE);

        return JobLogInfo.builder()
                .jobId(jobId)
                .type(logType)
                .lines(resultLogLinesCommand.getStdOut())
                .totalCount(Integer.parseInt(splitWcCommandResult[0]))
                .bytes(Integer.parseInt(splitWcCommandResult[1]))
                .build();
    }

    /**
     * Gets a job log file.
     *
     * @param jobId   The job identifier.
     * @param logType The type of required log file.
     * @return The job log file like a byte array.
     */
    @Override
    public InputStream getJobLogFile(final long jobId, final JobLogInfo.Type logType) {
        try {
            return new BufferedInputStream(new FileInputStream(getLogFilePath(jobId, logType)));
        } catch (final IOException e) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, String.format(CANT_FIND_LOG_FILE, jobId), e);
        }
    }

    /**
     * Gets a path to job log file.
     * Note: the names of the log files are defined by the job submission command templates for each grid-engine.
     *
     * @param jobId   The job identifier.
     * @param logType The type of required log file.
     * @return The job path to job log file.
     */
    private String getLogFilePath(final long jobId, final JobLogInfo.Type logType) {
        return Paths.get(logDir, jobId + DOT + logType.getSuffix()).toString();
    }

    @Override
    public String getJobLogDir() {
        return logDir;
    }

    @Override
    public CommandType getProviderType() {
        return CommandType.COMMON;
    }

    /**
     * This method checks for job log directory existence and write accessibility,
     * in case of failure, the application hasn't to start.
     */
    @PostConstruct
    public void checkLogDirAvailability() {
        final Path logPath = Path.of(logDir);
        if (!Files.isDirectory(logPath) || !Files.isWritable(logPath)) {
            final String message = "The directory to log files was not found or write permissions are missing: "
                    + logDir;
            log.error(message);
            throw new IllegalStateException(message);
        }
    }
}
