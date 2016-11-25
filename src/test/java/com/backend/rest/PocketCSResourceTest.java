package com.backend.rest;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.backend.AlgorithmsTable;

//import com.sun.deploy.net.URLEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.*;

import javax.print.DocFlavor;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * Created by Peeps on 11/2/16.
 */
public class PocketCSResourceTest extends JerseyTest{

    private static final String ALGORITHMS_TABLE = "Algorithms";
    private static final String DATA_STRUCTURES_TABLE = "dataStructures";
    private static final String SOFTWARE_DESIGN_TABLE = "softwareDesign";

    @Override
    protected Application configure() {
        return new ResourceConfig(PocketCSResource.class);
    }

//    @Test
//    public void TestingWithNoParameters(){
//        WebTarget webTarget = target("getTable");
//        Response response = webTarget.request().get();
//        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
//    }
//
//    @Test
//    public void invalidTableNameEntered(){
//        WebTarget webTarget = target("getTable")
//                .queryParam("tableName", "testing");
//
//        Response response = webTarget.request().get();
//        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
//    }
//
//    @Test
//    public void validTableNameEntered(){
//        WebTarget webTarget = target("getTable")
//                .queryParam("tableName", ALGORITHMS_TABLE);
//
//        String result = webTarget.request().get(String.class);
//
//        assertNotNull(result);
//    }
//
//    // Testing addItem End point
//    @Test
//    public void callAddItemsEndPointWithNoParameters(){
//        WebTarget webTarget = target("addItem");
//
//        Response response = webTarget.request().get();
//
//        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
//    }
//
//    @Test
//    public void callAddItemEndPointWithWrongTableName(){
//        WebTarget webTarget = target("addItem")
//                .queryParam("tableName", ALGORITHMS_TABLE);
//
//        Response response = webTarget.request().get();
//
//        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
//    }
//
//    @Test
//    public void insertItemWithValidTableNameAndInvalidItem() throws UnsupportedEncodingException {
//
//        Item sessionRow = new Item()
//                .withPrimaryKey("algoID", 1)
//                .withLong("userID", 0)
//                .withString("algoName", "Binary Search")
//                .withInt("categoryID", 0)
//                .withString("description", "Does some searching")
//                .withString("runtime", "bla")
//                .withString("imageID", "rea")
//                .withString("dateCreated", "fds");
//
//        WebTarget webTarget;
//        webTarget = target("addItem")
//                .queryParam("tableName", ALGORITHMS_TABLE)
//                .queryParam("item", URLEncoder.encode(sessionRow.toJSON(), "UTF-8")); // encode JSON string
//
//        Response response = webTarget.request().get();
//        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
//    }
//
//    @Test
//    public void insertItemWithValidTableNameAndValidItem() throws UnsupportedEncodingException {
//        Item sessionRow = new Item()
//                .withPrimaryKey("algoID", UUID.randomUUID().toString())
//                .withLong("userID", 0)
//                .withString("algoName", "Binary Search")
//                .withInt("categoryID", 0)
//                .withString("description", "Does some searching")
//                .withString("runtime", "bla")
//                .withString("imageID", "rea")
//                .withString("dateCreated", "fds")
//                .withString("dateUpdated", "10/12/3134")
//                .withString("helpfulLink", "12/34/43");
//
//        WebTarget webTarget;
//        webTarget = target("addItem")
//                .queryParam("tableName", ALGORITHMS_TABLE)
//                .queryParam("item", URLEncoder.encode(sessionRow.toJSON(), "UTF-8")); // encode JSON string
//
//        String response = webTarget.request().get(String.class);
//        assertEquals(response.intern(), "Success");
//    }

}
