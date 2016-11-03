package com.backend.rest;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.backend.AlgorithmsTable;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.*;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
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

    @Before
    public void setup(){
        WebTarget webTarget = target("init");
        System.out.println("Before");
        webTarget.request();
    }

//    @After
//    public void teardown(){
//        WebTarget webTarget = target("destroy");
//        webTarget.request();
//    }

    @Test
    public void TestingWithNoParameters(){
        WebTarget webTarget = target("getTable");
        Response response = webTarget.request().get();
        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void invalidTableNameEntered(){
        WebTarget webTarget = target("getTable")
                .queryParam("tableName", "testing");

        Response response = webTarget.request().get();
        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void validTableNameEntered(){
        WebTarget webTarget = target("getTable")
                .queryParam("tableName", ALGORITHMS_TABLE);

        String result = webTarget.request().get(String.class);
        System.out.println("Results: " + result);
    }
}
