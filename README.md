# SyniverseSDC-BatchAutomation
Java example for Batch Job Submission & File Creation using the Syniverse Developer Community (SDC)
https://developer.syniverse.com

## Prerequisites

You will first need to have an account (create at https://developer.syniverse.com ).

Secondly you will need to have subscribed to the Service Offerings for Developer Community Gateway, Media Service, Batch Automation and Event Manager. 

Do this by going to the Service Offerings tab (https://developer.syniverse.com/index.html#!/service-offerings) , clicking on each service in turn, click on Subscriptions, Click on Subscribe and then select an account from the drop down.

Thirdly you will need to create an application (or update an existing Application )

Do this by going to the Applications tab (https://developer.syniverse.com/index.html#!/applications), click on New application and follow the instructions. You also need to add (or update) the Account & APIs section to turn on the SDC Gateway Services, Media Services, Batch Automation and Event Manager. Lastly re-generate the Auth Keys, and then copy your Access token for use in the Code.

Fourth you will need to create a delivery configuration for the notifications (only required for running "monitor" type jobs)

Do this by going to the Event Manager tab and then to Delivery Configurations (https://developer.syniverse.com/index.html#!/ess/delivery-configurations), click on new Delivery Configuration, and then populate the Name and Address fields (the address is the URL where the notification should be sent to. The other properties can be left the same.
If you are just trying out the service you may want to try https://requestb.in/ to create an endpoint to receive the notifications.

Once you have created the delivery configuration, the generated "delivery configuration id" value is visible on the main delivery configurations page.

## Setup

To quickly run these samples out of the box, the following are required
- JDK 8
- Eclipse or other IDE 
- Maven 

Importing this project into Eclipse as "Existing Maven Projects" should generate needed project metadata and resolve dependencies (assuming automatic build preferences were not disabled in the workspace).

This sample bundles up all http requests required to run and then check the status of a BatchAutomation job
- Define a new MSS file
- Upload BatchAutomation input data to newly defined MSS file
- Create new BatchAutomation job with reference to newly created MSS file
- Get status (and other details) of newly created BatchAutomation job

Some configurations will need to be set to specify some user specific data, and define which sample job type is to be run. All of this is done in src/main/resources/application.properties

__user.sdc.bearer-token__ - This value is set as the Authentication header in the sample http requests. A valid SDC bearer token must be set here.\
__ess.delivery.configuration.id__ - If running a "monitor" job, a delivery configuration id must be set here. Details in the last section of the Prerequisites section above.\
__sample.job.type__ - Controls which type of sample job will be run. Options are equal to the names of the subfolders in the resources directory.... lookup, monitor and unsubscribe. The contents of the request that defines the batch job, and the input file content to be run with the job, are found in these subdirectories.


# Run It
com.syniverse.eis.aba.sample.schedule.CreateScheduleApp is the runnable class that kicks this off. 
Output info is written to System.out, and a successful run would look something like this:

MSS file creation response: {"file_id":"c5d82b8a-c0c1-44c1-84a0-74621ba838a2","file_name":"SyniverseSample","company-id":"829","file_tags":"","file_folder":"","app_name":"SyniverseSample","file_status":"CREATED","file_uri":"https://api.syniverse.com/mediastorage/v1/files/c5d82b8a-c0c1-44c1-84a0-74621ba838a2/content","file_compression_type":"","file_version":1,"file_size":0,"file_fullsize":209715200,"creation_time":"2018-01-12T14:15:53.416 +0000","modified_time":"2018-01-12T14:15:53.416 +0000","file_retention_time":1,"expire_time":"2018-01-13T14:15:53.416 +0000"}\
MSS file upload response status code: 201\
ABA schedule creation response: {"schedule":{"id":"83ebdd8b-528d-4b21-b8ba-71a16fda26ba","jobId":"NIS-Scrub-v2-fs1","name":"SampleNumberLookup","inputFileId":"c5d82b8a-c0c1-44c1-84a0-74621ba838a2","fileRetentionDays":1,"scheduleRetentionDays":1,"outputFileNamingExpression":"Sample","outputFileFolder":"","outputFileTag":null,"jobRuntimeContext":{}}}\
ABA execution current state: {"executions":[{"id":"f373facb-c8b9-4814-aab8-55eb7d9efc5b","scheduleDetail":{"id":"83ebdd8b-528d-4b21-b8ba-71a16fda26ba","jobId":"NIS-Scrub-v2-fs1","name":"SampleNumberLookup","inputFileId":"c5d82b8a-c0c1-44c1-84a0-74621ba838a2","fileRetentionDays":1,"scheduleRetentionDays":1,"outputFileNamingExpression":"Sample","outputFileFolder":"","outputFileTag":null,"jobRuntimeContext":{}},"status":"COMPLETE","statusReason":"Final Status","startTimestamp":1515766556239,"statusUpdateTimestamp":1515766561933,"outputFileId":"EMPTY_FILE","errorDetailFileId":"10028cc0-c6d8-42ae-b59f-d3e1183130ea","retryFileId":"65737c4e-af8a-4b5e-9944-41938228b34d","recordSuccessCount":0,"recordRetryCount":3,"recordErrorCount":0,"outputFileURI":"EMPTY_FILE","errorDetailFileURI":"https://api.syniverse.com/mediastorage/v1/files/10028cc0-c6d8-42ae-b59f-d3e1183130ea/content","retryFileURI":"https://api.syniverse.com/mediastorage/v1/files/65737c4e-af8a-4b5e-9944-41938228b34d/content"}]}\
Execution finished with status COMPLETE

	 
