
Implementation Steps:


Load Data to AWS Elastic Search via logstash:

1) Create Elastic Search micro Cluster on aws

2) Install logstash locally (6.4 version)

3) Install Amazon_ES logstash plugin for logstash
https://github.com/awslabs/logstash-output-amazon_es/blob/master/README.md

4) Add logstash config file logstash_resource.config which takes input from csv file and send output to aws elastic search with the help of Amazon_ES logstash plugin

a) There were some errors like "'No handler for type [string] declared on field". This is due to String is now deprecated
in elastic search and need to use "text" or "keyword" instead. But it looks Amazon_ES logstash plugin still using "string",
so to resolve this, need to override plugin template by downloading template from https://raw.githubusercontent.com/logstash-plugins/logstash-output-elasticsearch/master/lib/logstash/outputs/elasticsearch/elasticsearch-template-es6x.json
and add it in logstash config file

5) Start logstash and verify through AWS kibana all the data is populating or not


AWS Lambda Function:
1) Implemented class SearchPlanHandler.java for Lambda function
2) Initially tried to create project jar using maven, but consistently facing the error "java.lang.NoClassDefFoundError: org/json/JSONObject" after running test in aws lambda then decided to use gradle to package the project
3) Install gradle and add file demo.gradle to package project then run: gradle demo 
4) Upload project zip file to create AWS Lambda function

Add following JSON to test lambda function:
{
  "queryParams": {
    "PLAN_NAME": "KAISER",
    "SPONSOR_DFE_NAME": "SUNRISE BANK"
  }
}



Create API Gateway:
1) Created API with resource plandetails and method GET
2) Add QueryString while creating method: PLAN_NAME, SPONSOR_DFE_NAME, SPONS_DFE_LOC_US_STATE
3) In IntegrationRequest add template to send user input to lambda function
{
		"queryParams": {
		set($queryMap = $input.params().querystring)# foreach($key in $queryMap.keySet())
		"$key": "$queryMap.get($key)"#
		if ($foreach.hasNext),
	}
}

4) Test and then deploy

Final URL:

a) Search With PLAN_NAME
https://ihj78gmlma.execute-api.us-west-1.amazonaws.com/demo/plandetails?PLAN_NAME=KAISER

b) Search With SPONSOR_DFE_NAME

https://ihj78gmlma.execute-api.us-west-1.amazonaws.com/demo/plandetails?SPONSOR_DFE_NAME=SUNRISE%20BANK

c) Search With SPONS_DFE_MAIL_US_STATE

https://ihj78gmlma.execute-api.us-west-1.amazonaws.com/demo/plandetails?SPONS_DFE_LOC_US_STATE=CA

a) Search With PLAN_NAME AND SPONS_DFE_MAIL_US_STATE
https://ihj78gmlma.execute-api.us-west-1.amazonaws.com/demo/plandetails?PLAN_NAME=KAISER&SPONS_DFE_MAIL_US_STATE=CA
