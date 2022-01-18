
<a name="definitions"></a>
## Definitions

<a name="deletejobfilter"></a>
### DeleteJobFilter

|Name|Description|Schema|
|---|---|---|
|**force**  <br>*optional*|**Example** : `true`|boolean|
|**id**  <br>*optional*|**Example** : `0`|integer (int64)|
|**user**  <br>*optional*|**Example** : `"string"`|string|


<a name="deletedjobinfo"></a>
### DeletedJobInfo

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|**Example** : `[ 0 ]`|< integer (int64) > array|
|**user**  <br>*optional*|**Example** : `"string"`|string|


<a name="healthcheckinfo"></a>
### HealthCheckInfo

|Name|Description|Schema|
|---|---|---|
|**checkTime**  <br>*optional*|**Example** : `"string"`|string (date-time)|
|**startTime**  <br>*optional*|**Example** : `"string"`|string (date-time)|
|**statusInfo**  <br>*optional*|**Example** : `"[statusinfo](#statusinfo)"`|[StatusInfo](definitions.md#statusinfo)|


<a name="host"></a>
### Host

|Name|Description|Schema|
|---|---|---|
|**hostname**  <br>*optional*|**Example** : `"string"`|string|
|**load**  <br>*optional*|**Example** : `0.0`|number (double)|
|**memTotal**  <br>*optional*|**Example** : `0`|integer (int64)|
|**memUsed**  <br>*optional*|**Example** : `0`|integer (int64)|
|**numOfCore**  <br>*optional*|**Example** : `0`|integer (int32)|
|**numOfProcessors**  <br>*optional*|**Example** : `0`|integer (int32)|
|**numOfSocket**  <br>*optional*|**Example** : `0`|integer (int32)|
|**numOfThread**  <br>*optional*|**Example** : `0`|integer (int32)|
|**totalSwapSpace**  <br>*optional*|**Example** : `0.0`|number (double)|
|**typeOfArchitect**  <br>*optional*|**Example** : `"string"`|string|
|**usedSwapSpace**  <br>*optional*|**Example** : `0.0`|number (double)|


<a name="hostfilter"></a>
### HostFilter

|Name|Description|Schema|
|---|---|---|
|**hosts**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|


<a name="hostgroup"></a>
### HostGroup

|Name|Description|Schema|
|---|---|---|
|**hostGroupEntry**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**hostGroupName**  <br>*optional*|**Example** : `"string"`|string|


<a name="hostgroupfilter"></a>
### HostGroupFilter

|Name|Description|Schema|
|---|---|---|
|**hostGroupNames**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|


<a name="job"></a>
### Job

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|**Example** : `0`|integer (int32)|
|**name**  <br>*optional*|**Example** : `"string"`|string|
|**owner**  <br>*optional*|**Example** : `"string"`|string|
|**priority**  <br>*optional*|**Example** : `0.0`|number (double)|
|**queueName**  <br>*optional*|**Example** : `"string"`|string|
|**slots**  <br>*optional*|**Example** : `0`|integer (int32)|
|**state**  <br>*optional*|**Example** : `"[jobstate](#jobstate)"`|[JobState](definitions.md#jobstate)|
|**submissionTime**  <br>*optional*|**Example** : `"string"`|string (date-time)|


<a name="jobfilter"></a>
### JobFilter

|Name|Description|Schema|
|---|---|---|
|**ids**  <br>*optional*|**Example** : `[ 0 ]`|< integer (int32) > array|
|**names**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**owners**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**state**  <br>*optional*|**Example** : `"string"`|string|


<a name="jobloginfo"></a>
### JobLogInfo

|Name|Description|Schema|
|---|---|---|
|**bytes**  <br>*optional*|**Example** : `0`|integer (int64)|
|**jobId**  <br>*optional*|**Example** : `0`|integer (int32)|
|**lines**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**totalCount**  <br>*optional*|**Example** : `0`|integer (int32)|
|**type**  <br>*optional*|**Example** : `"string"`|enum (ERR, OUT)|


<a name="joboptions"></a>
### JobOptions

|Name|Description|Schema|
|---|---|---|
|**arguments**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**canBeBinary**  <br>*optional*|**Example** : `true`|boolean|
|**command**  <br>*optional*|**Example** : `"string"`|string|
|**envVariables**  <br>*optional*|**Example** : `{<br>  "string" : "string"<br>}`|< string, string > map|
|**name**  <br>*optional*|**Example** : `"string"`|string|
|**parallelEnvOptions**  <br>*optional*|**Example** : `"[parallelenvoptions](#parallelenvoptions)"`|[ParallelEnvOptions](definitions.md#parallelenvoptions)|
|**priority**  <br>*optional*|**Example** : `0`|integer (int32)|
|**queues**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**useAllEnvVars**  <br>*optional*|**Example** : `true`|boolean|
|**workingDir**  <br>*optional*|**Example** : `"string"`|string|


<a name="jobstate"></a>
### JobState

|Name|Description|Schema|
|---|---|---|
|**category**  <br>*optional*|**Example** : `"string"`|enum (DELETED, ERROR, FINISHED, PENDING, RUNNING, SUSPENDED, UNKNOWN)|
|**state**  <br>*optional*|**Example** : `"string"`|string|
|**stateCode**  <br>*optional*|**Example** : `"string"`|string|


<a name="225c01981c26ed06907326b31323db01"></a>
### Listing«Host»

|Name|Description|Schema|
|---|---|---|
|**elements**  <br>*optional*|**Example** : `[ "[host](#host)" ]`|< [Host](definitions.md#host) > array|


<a name="ea037821c9f1ca25d9963816611322a9"></a>
### Listing«Job»

|Name|Description|Schema|
|---|---|---|
|**elements**  <br>*optional*|**Example** : `[ "[job](#job)" ]`|< [Job](definitions.md#job) > array|


<a name="parallelenv"></a>
### ParallelEnv

|Name|Description|Schema|
|---|---|---|
|**accountingSummary**  <br>*optional*|**Example** : `true`|boolean|
|**allocationRule**  <br>*optional*|**Example** : `"[rulestate](#rulestate)"`|[RuleState](definitions.md#rulestate)|
|**allowedUserGroups**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**controlSlaves**  <br>*optional*|**Example** : `true`|boolean|
|**forbiddenUserGroups**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**jobIsFirstTask**  <br>*optional*|**Example** : `true`|boolean|
|**name**  <br>*optional*|**Example** : `"string"`|string|
|**slots**  <br>*optional*|**Example** : `0`|integer (int32)|
|**startProcArgs**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**stopProcArgs**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**urgencySlots**  <br>*optional*|**Example** : `"[urgencystate](#urgencystate)"`|[UrgencyState](definitions.md#urgencystate)|


<a name="parallelenvfilter"></a>
### ParallelEnvFilter

|Name|Description|Schema|
|---|---|---|
|**parallelEnvs**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|


<a name="parallelenvoptions"></a>
### ParallelEnvOptions

|Name|Description|Schema|
|---|---|---|
|**max**  <br>*optional*|**Example** : `0`|integer (int32)|
|**min**  <br>*optional*|**Example** : `0`|integer (int32)|
|**name**  <br>*optional*|**Example** : `"string"`|string|


<a name="peregistrationvo"></a>
### PeRegistrationVO

|Name|Description|Schema|
|---|---|---|
|**allocationRule**  <br>*optional*|**Example** : `"string"`|string|
|**name**  <br>*optional*|**Example** : `"string"`|string|
|**slots**  <br>*optional*|**Example** : `0`|integer (int32)|


<a name="queue"></a>
### Queue

|Name|Description|Schema|
|---|---|---|
|**allowedUserGroups**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**hostList**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**interval**  <br>*optional*|**Example** : `"string"`|string|
|**jobPriority**  <br>*optional*|**Example** : `0`|integer (int32)|
|**loadThresholds**  <br>*optional*|**Example** : `{<br>  "string" : 0.0<br>}`|< string, number (double) > map|
|**name**  <br>*optional*|**Example** : `"string"`|string|
|**numOfSuspendedJobs**  <br>*optional*|**Example** : `0`|integer (int32)|
|**numberInSchedulingOrder**  <br>*optional*|**Example** : `0`|integer (int32)|
|**ownerList**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**parallelEnvironmentNames**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**qtype**  <br>*optional*|**Example** : `"string"`|string|
|**slots**  <br>*optional*|**Example** : `"[slotsdescription](#slotsdescription)"`|[SlotsDescription](definitions.md#slotsdescription)|
|**suspendThresholds**  <br>*optional*|**Example** : `{<br>  "string" : 0.0<br>}`|< string, number (double) > map|
|**tmpDir**  <br>*optional*|**Example** : `"string"`|string|


<a name="queuefilter"></a>
### QueueFilter

|Name|Description|Schema|
|---|---|---|
|**queues**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|


<a name="queuevo"></a>
### QueueVO

|Name|Description|Schema|
|---|---|---|
|**allowedUserGroups**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**hostList**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**name**  <br>*optional*|**Example** : `"string"`|string|
|**ownerList**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|
|**parallelEnvironmentNames**  <br>*optional*|**Example** : `[ "string" ]`|< string > array|


<a name="rulestate"></a>
### RuleState

|Name|Description|Schema|
|---|---|---|
|**allocationRule**  <br>*optional*|**Example** : `"string"`|enum (FILL_UP, PE_SLOTS, ROUND_ROBIN, SLOTS_ON_ASSIGNED_HOST)|
|**originalState**  <br>*optional*|**Example** : `"string"`|string|
|**stateNumber**  <br>*optional*|**Example** : `0`|integer (int32)|


<a name="slotsdescription"></a>
### SlotsDescription

|Name|Description|Schema|
|---|---|---|
|**slots**  <br>*optional*|**Example** : `0`|integer (int32)|
|**slotsDetails**  <br>*optional*|**Example** : `{<br>  "string" : 0<br>}`|< string, integer (int32) > map|


<a name="statusinfo"></a>
### StatusInfo

|Name|Description|Schema|
|---|---|---|
|**code**  <br>*optional*|**Example** : `0`|integer (int64)|
|**info**  <br>*optional*|**Example** : `"string"`|string|
|**status**  <br>*optional*|**Example** : `"string"`|enum (ERROR, NOT_INITIALIZED, NOT_PROVIDED, OK, WARNING)|


<a name="urgencystate"></a>
### UrgencyState

|Name|Description|Schema|
|---|---|---|
|**state**  <br>*optional*|**Example** : `0`|integer (int32)|
|**urgencyStateType**  <br>*optional*|**Example** : `"string"`|enum (AVG, MAX, MIN, NUMBER)|


<a name="usagereport"></a>
### UsageReport

|Name|Description|Schema|
|---|---|---|
|**cpuTime**  <br>*optional*|**Example** : `0.0`|number (double)|
|**ioData**  <br>*optional*|**Example** : `0.0`|number (double)|
|**ioWaiting**  <br>*optional*|**Example** : `0.0`|number (double)|
|**memory**  <br>*optional*|**Example** : `0.0`|number (double)|
|**systemTime**  <br>*optional*|**Example** : `0.0`|number (double)|
|**userTime**  <br>*optional*|**Example** : `0.0`|number (double)|
|**wallClock**  <br>*optional*|**Example** : `0`|integer (int32)|


<a name="usagereportfilter"></a>
### UsageReportFilter

|Name|Description|Schema|
|---|---|---|
|**days**  <br>*optional*|**Example** : `0`|integer (int32)|
|**endTime**  <br>*optional*|**Example** : `"string"`|string (date-time)|
|**jobIdOrName**  <br>*optional*|**Example** : `"string"`|string|
|**owner**  <br>*optional*|**Example** : `"string"`|string|
|**parallelEnv**  <br>*optional*|**Example** : `"string"`|string|
|**queue**  <br>*optional*|**Example** : `"string"`|string|
|**startTime**  <br>*optional*|**Example** : `"string"`|string (date-time)|



