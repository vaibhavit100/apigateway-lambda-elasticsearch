
Implementation Steps:


Load Data to AWS Elastic Search via logstash:

1) Create Elastic Search micro Cluster on aws

2) Install logstash locally (6.4 version)

3) Install Amazon_ES logstash plugin for logstash
https://github.com/awslabs/logstash-output-amazon_es/blob/master/README.md

4) Add logstash config file "logstash_resource.config" which takes input from csv file("f_5500_2017_latest.csv") and send output to aws elastic search with the help of Amazon_ES logstash plugin

a) While running logstash, noticed  some errors like "No handler for type [string] declared on field". This is due to String is now deprecated in elastic search. The fix is to use "text" or "keyword" instead. Since Amazon_ES logstash plugin still using "string", so to resolve this, overrided plugin template by downloading template from https://raw.githubusercontent.com/logstash-plugins/logstash-output-elasticsearch/master/lib/logstash/outputs/elasticsearch/elasticsearch-template-es6x.json and added it in logstash output file.

5) Start logstash and verify all the date through AWS kibana.


AWS Lambda Function:
1) Create java maven project SearchPlan
2) Added AWS dependency in pom.xml

<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-events</artifactId>
    <version>1.3.0</version>
</dependency>
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-core</artifactId>
    <version>1.1.0</version>
</dependency>

3) Created the SearchPlanHandler.java class for Lambda function which is using steams for handling input and output and Jest client to interact with elastic search service
2) Next step is to package the project, earlier tried to package using maven, but consistently facing the error "java.lang.NoClassDefFoundError: org/json/JSONObject" after running test in aws lambda(need to investigate further), then decided to use gradle to package the project due to shortage of time.
3) Install gradle and add file demo.gradle to package project  and then run: gradle demo
4) Upload project zip file present in build/distributions to create AWS Lambda function

Add following JSON to test lambda function:
{
  "queryParams": {
    "PLAN_NAME": "KAISER",
    "SPONSOR_DFE_NAME": "SUNRISE BANK"
  }
}



Create API Gateway:
1) Created API with resource plandetails.
2) Created method GET. Added QueryString while creating method: PLAN_NAME, SPONSOR_DFE_NAME, SPONS_DFE_LOC_US_STATE, it basically indicates what parameters users need to pass to call this GET method.
3) Next step is to add template in IntegrationRequest to send user request parameters(PLAN_NAME, SPONSOR_DFE_NAME, SPONS_DFE_LOC_US_STATE) to lambda function.The following template is added:
{
		"queryParams": {
		set($queryMap = $input.params().querystring)# foreach($key in $queryMap.keySet())
		"$key": "$queryMap.get($key)"#
		if ($foreach.hasNext),
	}
}

Note: Above template only allow to pass non null request parameter value to lambda function

4) Test the api Gateway

5) Deploy the app and select the environment as demo

Final URL:

a) Search With PLAN_NAME
https://ihj78gmlma.execute-api.us-west-1.amazonaws.com/demo/plandetails?PLAN_NAME=KAISER

b) Search With SPONSOR_DFE_NAME

https://ihj78gmlma.execute-api.us-west-1.amazonaws.com/demo/plandetails?SPONSOR_DFE_NAME=SUNRISE%20BANK

c) Search With SPONS_DFE_MAIL_US_STATE

https://ihj78gmlma.execute-api.us-west-1.amazonaws.com/demo/plandetails?SPONS_DFE_LOC_US_STATE=CA

d) Search With PLAN_NAME AND SPONS_DFE_MAIL_US_STATE
https://ihj78gmlma.execute-api.us-west-1.amazonaws.com/demo/plandetails?PLAN_NAME=KAISER&SPONS_DFE_MAIL_US_STATE=CA
