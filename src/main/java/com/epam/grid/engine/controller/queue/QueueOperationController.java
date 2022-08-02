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

package com.epam.grid.engine.controller.queue;

import com.epam.grid.engine.entity.QueueFilter;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.QueueVO;
import com.epam.grid.engine.service.QueueOperationProviderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The class which receives web requests from users and transmits it
 * for processing to the {@link QueueOperationProviderService}.
 */
@RestController
@RequestMapping("/queues")
@RequiredArgsConstructor
public class QueueOperationController {

    private static final String QUEUE_NOT_FOUND = "Requested queue(-s) not found";
    private static final String QUEUE_REGISTRATION_PARAMETERS_NOT_FOUND = "Queue registration"
            + " failed due to some reason: associated users, hosts or parallel env not found";
    private static final String QUEUE_UPDATE_PARAMETERS_NOT_FOUND = "Queue update"
            + " failed due to some reason: queue name, associated users, hosts or parallel env not found";
    private static final String INTERNAL_ERROR = "Internal error";
    private static final String MISSING_OR_INVALID_REQUEST_BODY = "Missing or invalid request body";
    private static final String DELETION_DENIED = "Deletion denied";
    private static final String UPDATE_DENIED = "Update denied";
    private static final String REGISTRATION_DENIED = "Registration denied";
    private static final String SUCCESSFULLY_RECEIVED = "Queues received successfully";
    private static final String SUCCESSFULLY_REGISTERED = "Queue registered successfully";
    private static final String SUCCESSFULLY_DELETED = "Queue was successfully deleted";
    private static final String SUCCESSFULLY_UPDATED = "Queue was successfully updated";

    private final QueueOperationProviderService queueOperationProviderService;

    /**
     * Returns a list containing the existing Queues. Each {@link Queue} entity of
     * this List has only one attribute representing Queue's name.
     *
     * @return a List containing the existing Queues with their names
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Lists queues names",
            notes = "Returns the list of names of all currently defined cluster queues",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_RECEIVED),
            @ApiResponse(code = 404, message = QUEUE_NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public List<Queue> listQueueNames() {
        return queueOperationProviderService.listQueues();
    }

    /**
     * Returns a List containing specified {@link Queue}s with respect to provided {@link QueueFilter}.
     *
     * @param queueFilter a provided filter
     * @return a List containing specified Queues with respect to provided filter
     */
    @PostMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Filters queues",
            notes = "Returns the list which contains information about queues with respect to provided filters",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_RECEIVED),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY + ": 'name' should be specified!"),
            @ApiResponse(code = 404, message = QUEUE_NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public List<Queue> listQueues(@RequestBody(required = false) final QueueFilter queueFilter) {
        return queueOperationProviderService.listQueues(queueFilter);
    }

    /**
     * Delete specified queue.
     *
     * @param queueName An object with the task deletion parameters.
     * @return Which queues were deleted.
     */
    @DeleteMapping("/{queue_name}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete queue",
            notes = "Deletes one or more queues"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_DELETED),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY),
            @ApiResponse(code = 403, message = DELETION_DENIED),
            @ApiResponse(code = 404, message = QUEUE_NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public Queue deleteQueue(@PathVariable("queue_name") final String queueName) {
        return queueOperationProviderService.deleteQueue(queueName);
    }

    /**
     * Registers a queue with specified in the {@link QueueVO} properties.
     *
     * @param registrationRequest the properties of the queue to be registered
     * @return the registered {@link Queue}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Registers queue",
            notes = "Registers the queue with specified properties",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_REGISTERED),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY),
            @ApiResponse(code = 403, message = REGISTRATION_DENIED),
            @ApiResponse(code = 404, message = QUEUE_REGISTRATION_PARAMETERS_NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public Queue registerQueue(@RequestBody final QueueVO registrationRequest) {
        return queueOperationProviderService.registerQueue(registrationRequest);
    }

    /**
     * Updates a queue with specified in the {@link QueueVO} properties.
     *
     * @param updateRequest the properties of the queue to be updated
     * @return the updated {@link Queue}
     */
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Updates queue",
            notes = "Updates the queue with specified properties",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_UPDATED),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY),
            @ApiResponse(code = 403, message = UPDATE_DENIED),
            @ApiResponse(code = 404, message = QUEUE_UPDATE_PARAMETERS_NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public Queue updateQueue(@RequestBody final QueueVO updateRequest) {
        return queueOperationProviderService.updateQueue(updateRequest);
    }
}
