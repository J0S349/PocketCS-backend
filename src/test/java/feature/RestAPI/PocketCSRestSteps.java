package feature.RestAPI;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import backend.rest.PocketResource;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

//import org.apache.xpath.operations.String;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;


/**
 * Created by Peeps on 12/5/16.
 */
public class PocketCSRestSteps extends JerseyTest {
    Response response;


    @Override
    protected Application configure() {
        return new ResourceConfig(PocketResource.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @When("^I call the \'(.*)\' endpoint with no input")
    public void callGetTableWithNoInput(String endpoint){
        WebTarget webTarget = target(endpoint);

        response = webTarget.request().get();
    }

    @When("^I call the \'(.*)\' endpoint on the table \'(.*)\'$")
    public void callGetTableEndpoint(String endpoint, String tableName){
        WebTarget webTarget = target(endpoint)
                .queryParam("tableName", tableName);
        response = webTarget.request().get();
    }


    @Then("^the result of \'.*\' endpoint should be \'(.*)\'$")
    public void testingExpectedOutput(String output)
    {
        assertThat(response.readEntity(String.class), equalTo(output));
    }

    @Then("the result of 'getTable' endpoint should not be empty")
    public void testingExpectedOutputNotEmpty(){

        assertThat(response.readEntity(String.class), not(""));
    }







}
