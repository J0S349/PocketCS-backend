package backend;

import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.s3.model.Region;


import java.io.File;

/**
 * Created by Peeps on 10/8/16.
 *  This is the class that will allow us to comunicate between the database and our application
 *
 */
public class DBConnector {

    private static DynamoDB dynamoDB;
    private static AmazonDynamoDB client;

    private static final String CREDENTIAL_FILE_PATH = "./access.ini";
    // Make sure that you install awscli by running this command "sudo pip install awscli"

    private static AWSCredentialsProvider getCredentialsProvider() {
        if (new File(CREDENTIAL_FILE_PATH).exists()) {
            // reads properties accessKey, secretKey
            return new PropertiesFileCredentialsProvider(CREDENTIAL_FILE_PATH);
        } else {
            // first looks at env vars AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
            // then sys props aws.accessKeyId, aws.secretKey
            // then file ~/.aws/credentials
            // see http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
            return new DefaultAWSCredentialsProviderChain();
        }
    }

    public DBConnector() {
        client = new AmazonDynamoDBClient(getCredentialsProvider());
        client.setRegion(com.amazonaws.regions.Region.getRegion(Regions.US_EAST_1));
        //client.setRegion(Region.US_West.toAWSRegion()); // US West
        dynamoDB = new DynamoDB(client);
    }

    public void close(){
        dynamoDB.shutdown();
        client.shutdown();
    }

    // returns the dynamo DB instance. Singletons software design pattern
    public static DynamoDB getDynamoDB(){return dynamoDB; } //get dynamoDB instance

}
