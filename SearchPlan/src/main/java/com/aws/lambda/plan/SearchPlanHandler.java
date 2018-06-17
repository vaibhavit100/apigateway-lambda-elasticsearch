package com.aws.lambda.plan;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class SearchPlanHandler implements RequestStreamHandler {

    private static final String SERVER_URI="https://search-basic-search-3xgpao4dpiofloqfz3a5vcasvm.us-west-1.es.amazonaws.com";
    private static final String DEFAULT_INDEX = "plan-details";
    private static final String REQUEST_QUERY_PARAMETERS = "queryParams";

    private JSONParser parser = new JSONParser();


    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream,
                              Context context) throws IOException {

        LambdaLogger logger = context.getLogger();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        BoolQueryBuilder bQueryBuilder = new BoolQueryBuilder();
        JSONObject response = new JSONObject();

        try {
            JSONObject event = (JSONObject)parser.parse(bufferedReader);

            if (event.get(REQUEST_QUERY_PARAMETERS) != null) {
                JSONObject queryParameters = (JSONObject)event.get(REQUEST_QUERY_PARAMETERS);
                logger.log(String.format("Request params are :  %s", queryParameters.toJSONString()));
                queryParameters.forEach((key, value)-> bQueryBuilder.must(QueryBuilders.matchQuery((String)key, value)));
            } else {
                logger.log("Request Query Params are empty");
            }

            Search search = new Search.Builder(String.format("{\"query\":%s}", bQueryBuilder.toString()))
                    .addIndex(this.DEFAULT_INDEX)
                    .build();


            JestClientFactory jestClientFactory = new JestClientFactory();
            HttpClientConfig clientConfig = new HttpClientConfig
                    .Builder(SERVER_URI)
                    .multiThreaded(true)
                    .defaultMaxTotalConnectionPerRoute(10)
                    .maxTotalConnection(10)
                    .build();

            jestClientFactory.setHttpClientConfig(clientConfig);
            JestClient client = jestClientFactory.getObject();

            JestResult result = client.execute(search);
            JSONObject header = new JSONObject();
            response.put("isBase64Encoded", true);
            response.put("statusCode", 200);
            header.put("Content-Type", "application/json");
            response.put("headers",header);
            response.put("body", result.getJsonString());

        }catch(Exception ex){
            logger.log("Exception while processing the request");
            response.put("statusCode", "400");
            response.put("exception", ex);
            logger.log(ex.getMessage());
        }

        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream, "UTF-8");
        outputWriter.write(response.toJSONString());
        outputWriter.close();
    }

}