
<a name="paths"></a>
## Resources

<a name="health-check-controller_resource"></a>
### Health-check-controller
Health Check Controller


<a name="checkhealthusingget"></a>
#### Check status
```
GET /check
```


##### Description
Tries to get status of grid engine


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Status successfully received|[HealthCheckInfo](definitions.md#healthcheckinfo)|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Grid engine is not reachable|No Content|
|**500**|Internal error|No Content|


##### Produces

* `\*/*`


##### Example HTTP request

###### Request path
```
/check
```


##### Example HTTP response

###### Response 200
```json
{
  "checkTime" : "string",
  "startTime" : "string",
  "statusInfo" : {
    "code" : 0,
    "info" : "string",
    "status" : "string"
  }
}
```


<a name="host-controller_resource"></a>
### Host-controller
Host Controller


<a name="listhostsusingpost"></a>
#### List host nodes
```
POST /hosts
```


##### Description
Returns list that contains information about specific hosts regarding to filter


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**hostFilter**  <br>*optional*|hostFilter|[HostFilter](definitions.md#hostfilter)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Hosts received successfully|[Listing«Host»](definitions.md#225c01981c26ed06907326b31323db01)|
|**201**|Created|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Requested hosts not found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/hosts
```


###### Request body
```json
{
  "hosts" : [ "string" ]
}
```


##### Example HTTP response

###### Response 200
```json
{
  "elements" : [ {
    "hostname" : "string",
    "load" : 0.0,
    "memTotal" : 0,
    "memUsed" : 0,
    "numOfCore" : 0,
    "numOfProcessors" : 0,
    "numOfSocket" : 0,
    "numOfThread" : 0,
    "totalSwapSpace" : 0.0,
    "typeOfArchitect" : "string",
    "usedSwapSpace" : 0.0
  } ]
}
```


<a name="host-group-operation-controller_resource"></a>
### Host-group-operation-controller
Host Group Operation Controller


<a name="listhostgroupsusingpost"></a>
#### Filters host groups
```
POST /clusters/filter
```


##### Description
Returns the list which contains information about host groups with respect to provided filters


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**hostGroupFilter**  <br>*optional*|hostGroupFilter|[HostGroupFilter](definitions.md#hostgroupfilter)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Host groups list successfully received|< [HostGroup](definitions.md#hostgroup) > array|
|**201**|Created|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Required host group doesn't exist|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/clusters/filter
```


###### Request body
```json
{
  "hostGroupNames" : [ "string" ]
}
```


##### Example HTTP response

###### Response 200
```json
[ {
  "hostGroupEntry" : [ "string" ],
  "hostGroupName" : "string"
} ]
```


<a name="gethostgroupusingget"></a>
#### Info about selected host group
```
GET /clusters/{groupname}
```


##### Description
Returns information about the selected host group


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**groupname**  <br>*required*|groupname|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Host group successfully received|[HostGroup](definitions.md#hostgroup)|
|**400**|Wrong host group name|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Required host group doesn't exist|No Content|
|**500**|Internal error|No Content|


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/clusters/string
```


##### Example HTTP response

###### Response 200
```json
{
  "hostGroupEntry" : [ "string" ],
  "hostGroupName" : "string"
}
```


<a name="job-operation-controller_resource"></a>
### Job-operation-controller
Job Operation Controller


<a name="filterjobsusingpost"></a>
#### Filter jobs
```
POST /jobs
```


##### Description
Returns list that contains information about specific jobs regarding to filter


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**jobFilter**  <br>*optional*|jobFilter|[JobFilter](definitions.md#jobfilter)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Job received successfully|[Listing«Job»](definitions.md#ea037821c9f1ca25d9963816611322a9)|
|**201**|Created|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Specified job(-s) not found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/jobs
```


###### Request body
```json
{
  "ids" : [ 0 ],
  "names" : [ "string" ],
  "owners" : [ "string" ],
  "state" : "string"
}
```


##### Example HTTP response

###### Response 200
```json
{
  "elements" : [ {
    "id" : 0,
    "name" : "string",
    "owner" : "string",
    "priority" : 0.0,
    "queueName" : "string",
    "slots" : 0,
    "state" : {
      "category" : "string",
      "state" : "string",
      "stateCode" : "string"
    },
    "submissionTime" : "string"
  } ]
}
```


<a name="deletejobusingdelete"></a>
#### Delete job
```
DELETE /jobs
```


##### Description
Tries to delete one or more jobs by username, id or job name. If successful, returns the message and information about deleted job


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**deleteJobFilter**  <br>*required*|deleteJobFilter|[DeleteJobFilter](definitions.md#deletejobfilter)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Job deleted successfully|[DeletedJobInfo](definitions.md#deletedjobinfo)|
|**204**|No Content|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Specified job(-s) not found|No Content|
|**500**|Internal error|No Content|


##### Produces

* `\*/*`


##### Example HTTP request

###### Request path
```
/jobs
```


###### Request body
```json
{
  "force" : true,
  "id" : 0,
  "user" : "string"
}
```


##### Example HTTP response

###### Response 200
```json
{
  "id" : [ 0 ],
  "user" : "string"
}
```


<a name="runjobusingpost"></a>
#### Submits a job into a cluster
```
POST /jobs/submit
```


##### Description
Tries to add a job to the queue, if successful, returns the index of the job.


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**options**  <br>*required*|options|[JobOptions](definitions.md#joboptions)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Job submitted successfully|[Job](definitions.md#job)|
|**201**|Created|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Not Found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/jobs/submit
```


###### Request body
```json
{
  "arguments" : [ "string" ],
  "canBeBinary" : true,
  "command" : "string",
  "envVariables" : {
    "string" : "string"
  },
  "name" : "string",
  "parallelEnvOptions" : {
    "max" : 0,
    "min" : 0,
    "name" : "string"
  },
  "priority" : 0,
  "queues" : [ "string" ],
  "useAllEnvVars" : true,
  "workingDir" : "string"
}
```


##### Example HTTP response

###### Response 200
```json
{
  "id" : 0,
  "name" : "string",
  "owner" : "string",
  "priority" : 0.0,
  "queueName" : "string",
  "slots" : 0,
  "state" : {
    "category" : "string",
    "state" : "string",
    "stateCode" : "string"
  },
  "submissionTime" : "string"
}
```


<a name="getjobloginfousingget"></a>
#### Obtain a list of job log lines and information about log file
```
GET /jobs/{jobId}/logs
```


##### Description
Tries to get job log information


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**jobId**  <br>*required*|jobId|integer (int32)||
|**Query**|**fromHead**  <br>*optional*|fromHead|boolean||
|**Query**|**lines**  <br>*optional*|lines|integer (int32)|`0`|
|**Query**|**type**  <br>*optional*|type|enum (ERR, OUT)|`"ERR"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Log received successfully|[JobLogInfo](definitions.md#jobloginfo)|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Specified job(-s) not found|No Content|
|**500**|Internal error|No Content|


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/jobs/0/logs
```


##### Example HTTP response

###### Response 200
```json
{
  "bytes" : 0,
  "jobId" : 0,
  "lines" : [ "string" ],
  "totalCount" : 0,
  "type" : "string"
}
```


<a name="getjoblogfileusingget"></a>
#### Get the job log file
```
GET /jobs/{jobId}/logs/file
```


##### Description
Tries to get of the job log file


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**jobId**  <br>*required*|jobId|integer (int32)|
|**Query**|**type**  <br>*required*|type|enum (ERR, OUT)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Log received successfully|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Specified job(-s) not found|No Content|


##### Produces

* `\*/*`


##### Example HTTP request

###### Request path
```
/jobs/0/logs/file?type=string
```


<a name="parallel-env-controller_resource"></a>
### Parallel-env-controller
Parallel Env Controller


<a name="registerparallelenvusingpost"></a>
#### Registers parallel environment
```
POST /parallelenv
```


##### Description
Registers the parallel environment with specified properties


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**registrationRequest**  <br>*required*|registrationRequest|[PeRegistrationVO](definitions.md#peregistrationvo)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Parallel environment registered successfully|[ParallelEnv](definitions.md#parallelenv)|
|**201**|Created|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Registration denied|No Content|
|**404**|PE registration failed due to registrationparameter were not found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/parallelenv
```


###### Request body
```json
{
  "allocationRule" : "string",
  "name" : "string",
  "slots" : 0
}
```


##### Example HTTP response

###### Response 200
```json
{
  "accountingSummary" : true,
  "allocationRule" : {
    "allocationRule" : "string",
    "originalState" : "string",
    "stateNumber" : 0
  },
  "allowedUserGroups" : [ "string" ],
  "controlSlaves" : true,
  "forbiddenUserGroups" : [ "string" ],
  "jobIsFirstTask" : true,
  "name" : "string",
  "slots" : 0,
  "startProcArgs" : [ "string" ],
  "stopProcArgs" : [ "string" ],
  "urgencySlots" : {
    "state" : 0,
    "urgencyStateType" : "string"
  }
}
```


<a name="listparallelenvusingpost"></a>
#### List parallel environments
```
POST /parallelenv/filter
```


##### Description
Returns list that contains all PE or information about specific PE regarding to filter


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**parallelEnvFilter**  <br>*optional*|parallelEnvFilter|[ParallelEnvFilter](definitions.md#parallelenvfilter)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Parallel environments received successfully|< [ParallelEnv](definitions.md#parallelenv) > array|
|**201**|Created|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Requested parallel environments not found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/parallelenv/filter
```


###### Request body
```json
{
  "parallelEnvs" : [ "string" ]
}
```


##### Example HTTP response

###### Response 200
```json
[ {
  "accountingSummary" : true,
  "allocationRule" : {
    "allocationRule" : "string",
    "originalState" : "string",
    "stateNumber" : 0
  },
  "allowedUserGroups" : [ "string" ],
  "controlSlaves" : true,
  "forbiddenUserGroups" : [ "string" ],
  "jobIsFirstTask" : true,
  "name" : "string",
  "slots" : 0,
  "startProcArgs" : [ "string" ],
  "stopProcArgs" : [ "string" ],
  "urgencySlots" : {
    "state" : 0,
    "urgencyStateType" : "string"
  }
} ]
```


<a name="getparallelenvusingget"></a>
#### Provides specific parallel environment
```
GET /parallelenv/{name}
```


##### Description
Returns object that contains information about specific PE


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**name**  <br>*required*|name|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|PE successfully retrieved|[ParallelEnv](definitions.md#parallelenv)|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Requested parallel environments not found|No Content|
|**500**|Internal error|No Content|


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/parallelenv/string
```


##### Example HTTP response

###### Response 200
```json
{
  "accountingSummary" : true,
  "allocationRule" : {
    "allocationRule" : "string",
    "originalState" : "string",
    "stateNumber" : 0
  },
  "allowedUserGroups" : [ "string" ],
  "controlSlaves" : true,
  "forbiddenUserGroups" : [ "string" ],
  "jobIsFirstTask" : true,
  "name" : "string",
  "slots" : 0,
  "startProcArgs" : [ "string" ],
  "stopProcArgs" : [ "string" ],
  "urgencySlots" : {
    "state" : 0,
    "urgencyStateType" : "string"
  }
}
```


<a name="deleteparallelenvusingdelete"></a>
#### Delete parallel environment
```
DELETE /parallelenv/{name}
```


##### Description
Deletes parallel environment


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**name**  <br>*required*|name|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|PE was successfully deleted|[ParallelEnv](definitions.md#parallelenv)|
|**204**|No Content|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Deletion denied|No Content|
|**404**|Requested parallel environments not found|No Content|
|**500**|Internal error|No Content|


##### Produces

* `\*/*`


##### Example HTTP request

###### Request path
```
/parallelenv/string
```


##### Example HTTP response

###### Response 200
```json
{
  "accountingSummary" : true,
  "allocationRule" : {
    "allocationRule" : "string",
    "originalState" : "string",
    "stateNumber" : 0
  },
  "allowedUserGroups" : [ "string" ],
  "controlSlaves" : true,
  "forbiddenUserGroups" : [ "string" ],
  "jobIsFirstTask" : true,
  "name" : "string",
  "slots" : 0,
  "startProcArgs" : [ "string" ],
  "stopProcArgs" : [ "string" ],
  "urgencySlots" : {
    "state" : 0,
    "urgencyStateType" : "string"
  }
}
```


<a name="queue-operation-controller_resource"></a>
### Queue-operation-controller
Queue Operation Controller


<a name="registerqueueusingpost"></a>
#### Registers queue
```
POST /queues
```


##### Description
Registers the queue with specified properties


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**registrationRequest**  <br>*required*|registrationRequest|[QueueVO](definitions.md#queuevo)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Queue registered successfully|[Queue](definitions.md#queue)|
|**201**|Created|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Registration denied|No Content|
|**404**|Queue registration failed due to some reason: associated users, hosts or parallel env not found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/queues
```


###### Request body
```json
{
  "allowedUserGroups" : [ "string" ],
  "hostList" : [ "string" ],
  "name" : "string",
  "ownerList" : [ "string" ],
  "parallelEnvironmentNames" : [ "string" ]
}
```


##### Example HTTP response

###### Response 200
```json
{
  "allowedUserGroups" : [ "string" ],
  "hostList" : [ "string" ],
  "interval" : "string",
  "jobPriority" : 0,
  "loadThresholds" : {
    "string" : 0.0
  },
  "name" : "string",
  "numOfSuspendedJobs" : 0,
  "numberInSchedulingOrder" : 0,
  "ownerList" : [ "string" ],
  "parallelEnvironmentNames" : [ "string" ],
  "qtype" : "string",
  "slots" : {
    "slots" : 0,
    "slotsDetails" : {
      "string" : 0
    }
  },
  "suspendThresholds" : {
    "string" : 0.0
  },
  "tmpDir" : "string"
}
```


<a name="listqueuenamesusingget"></a>
#### Lists queues names
```
GET /queues
```


##### Description
Returns the list of names of all currently defined cluster queues


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Queues received successfully|< [Queue](definitions.md#queue) > array|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Requested queue(-s) not found|No Content|
|**500**|Internal error|No Content|


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/queues
```


##### Example HTTP response

###### Response 200
```json
[ {
  "allowedUserGroups" : [ "string" ],
  "hostList" : [ "string" ],
  "interval" : "string",
  "jobPriority" : 0,
  "loadThresholds" : {
    "string" : 0.0
  },
  "name" : "string",
  "numOfSuspendedJobs" : 0,
  "numberInSchedulingOrder" : 0,
  "ownerList" : [ "string" ],
  "parallelEnvironmentNames" : [ "string" ],
  "qtype" : "string",
  "slots" : {
    "slots" : 0,
    "slotsDetails" : {
      "string" : 0
    }
  },
  "suspendThresholds" : {
    "string" : 0.0
  },
  "tmpDir" : "string"
} ]
```


<a name="updatequeueusingput"></a>
#### Updates queue
```
PUT /queues
```


##### Description
Updates the queue with specified properties


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**updateRequest**  <br>*required*|updateRequest|[QueueVO](definitions.md#queuevo)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Queue was successfully updated|[Queue](definitions.md#queue)|
|**201**|Created|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Update denied|No Content|
|**404**|Queue update failed due to some reason: queue name, associated users, hosts or parallel env not found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/queues
```


###### Request body
```json
{
  "allowedUserGroups" : [ "string" ],
  "hostList" : [ "string" ],
  "name" : "string",
  "ownerList" : [ "string" ],
  "parallelEnvironmentNames" : [ "string" ]
}
```


##### Example HTTP response

###### Response 200
```json
{
  "allowedUserGroups" : [ "string" ],
  "hostList" : [ "string" ],
  "interval" : "string",
  "jobPriority" : 0,
  "loadThresholds" : {
    "string" : 0.0
  },
  "name" : "string",
  "numOfSuspendedJobs" : 0,
  "numberInSchedulingOrder" : 0,
  "ownerList" : [ "string" ],
  "parallelEnvironmentNames" : [ "string" ],
  "qtype" : "string",
  "slots" : {
    "slots" : 0,
    "slotsDetails" : {
      "string" : 0
    }
  },
  "suspendThresholds" : {
    "string" : 0.0
  },
  "tmpDir" : "string"
}
```


<a name="listqueuesusingpost"></a>
#### Filters queues
```
POST /queues/filter
```


##### Description
Returns the list which contains information about queues with respect to provided filters


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**queueFilter**  <br>*optional*|queueFilter|[QueueFilter](definitions.md#queuefilter)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Queues received successfully|< [Queue](definitions.md#queue) > array|
|**201**|Created|No Content|
|**400**|Missing or invalid request body: 'name' should be specified!|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Requested queue(-s) not found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/queues/filter
```


###### Request body
```json
{
  "queues" : [ "string" ]
}
```


##### Example HTTP response

###### Response 200
```json
[ {
  "allowedUserGroups" : [ "string" ],
  "hostList" : [ "string" ],
  "interval" : "string",
  "jobPriority" : 0,
  "loadThresholds" : {
    "string" : 0.0
  },
  "name" : "string",
  "numOfSuspendedJobs" : 0,
  "numberInSchedulingOrder" : 0,
  "ownerList" : [ "string" ],
  "parallelEnvironmentNames" : [ "string" ],
  "qtype" : "string",
  "slots" : {
    "slots" : 0,
    "slotsDetails" : {
      "string" : 0
    }
  },
  "suspendThresholds" : {
    "string" : 0.0
  },
  "tmpDir" : "string"
} ]
```


<a name="deletequeueusingdelete"></a>
#### Delete queue
```
DELETE /queues/{queue_name}
```


##### Description
Deletes one or more queues


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**queue_name**  <br>*required*|queue_name|string|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Queue was successfully deleted|[Queue](definitions.md#queue)|
|**204**|No Content|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Deletion denied|No Content|
|**404**|Requested queue(-s) not found|No Content|
|**500**|Internal error|No Content|


##### Produces

* `\*/*`


##### Example HTTP request

###### Request path
```
/queues/string
```


##### Example HTTP response

###### Response 200
```json
{
  "allowedUserGroups" : [ "string" ],
  "hostList" : [ "string" ],
  "interval" : "string",
  "jobPriority" : 0,
  "loadThresholds" : {
    "string" : 0.0
  },
  "name" : "string",
  "numOfSuspendedJobs" : 0,
  "numberInSchedulingOrder" : 0,
  "ownerList" : [ "string" ],
  "parallelEnvironmentNames" : [ "string" ],
  "qtype" : "string",
  "slots" : {
    "slots" : 0,
    "slotsDetails" : {
      "string" : 0
    }
  },
  "suspendThresholds" : {
    "string" : 0.0
  },
  "tmpDir" : "string"
}
```


<a name="usage-operation-controller_resource"></a>
### Usage-operation-controller
Usage Operation Controller


<a name="getusagereportusingpost"></a>
#### Provides usage report
```
POST /usage
```


##### Description
Returns a report containing usage summary information


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Body**|**filter**  <br>*required*|filter|[UsageReportFilter](definitions.md#usagereportfilter)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Usage report received successfully|[UsageReport](definitions.md#usagereport)|
|**201**|Created|No Content|
|**400**|Missing or invalid request body|No Content|
|**401**|Unauthorized|No Content|
|**403**|Forbidden|No Content|
|**404**|Requested usage report not found|No Content|
|**500**|Internal error|No Content|


##### Consumes

* `application/json`


##### Produces

* `\*/*`
* `application/json`


##### Example HTTP request

###### Request path
```
/usage
```


###### Request body
```json
{
  "days" : 0,
  "endTime" : "string",
  "jobIdOrName" : "string",
  "owner" : "string",
  "parallelEnv" : "string",
  "queue" : "string",
  "startTime" : "string"
}
```


##### Example HTTP response

###### Response 200
```json
{
  "cpuTime" : 0.0,
  "ioData" : 0.0,
  "ioWaiting" : 0.0,
  "memory" : 0.0,
  "systemTime" : 0.0,
  "userTime" : 0.0,
  "wallClock" : 0
}
```



