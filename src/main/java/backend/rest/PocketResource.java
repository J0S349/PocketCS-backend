package backend.rest;

import backend.*;
import com.amazonaws.SystemDefaultDnsResolver;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Peeps on 11/23/16.
 */

@Path("/")
public class PocketResource {

    private DBConnector dbConnector;
    private final String ALGORITHMS_TABLE = AlgorithmsTable.getTableName();
    private final String DATA_STRUCTURES_TABLE = DataStructuresTable.getTableName();
    private final String SOFTWARE_DESIGN_TABLE = SoftwareDesignTable.getTableName();
    private final String USER_TABLE = UserTable.getTableName();

    // will be used to store the names of the tables for quick access
    private static HashSet<String> tables;
    private static AlgorithmsTable algorithmsTable;
    private static DataStructuresTable dataStructuresTable;
    private static SoftwareDesignTable softwareDesignTable;
    private static UserTable userTable;

    // adds table names to HashSet
    public void start(){
        dbConnector = new DBConnector();
        tables = new HashSet<String>();
        tables.add(ALGORITHMS_TABLE);
        tables.add(DATA_STRUCTURES_TABLE);
        tables.add(SOFTWARE_DESIGN_TABLE);
        tables.add(USER_TABLE);
    }

    // Returns the contents from a specified table as JSON string
    @GET
    @Path("/getTable")
    public Response getTableInfo(
            @QueryParam("tableName") String tableName
    ) throws UnsupportedEncodingException {

        //System.out.println("Hit End Point");
        // check whether no parameters were passed
        if (tableName == null) {
            String msg = "missing parameter tableName";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }
        start();
        //System.out.print("Table name entered");
        if (!tables.contains(tableName)) {
            String msg = "table not found";
            System.out.println(msg);
            Logging.getLOG().debug("user requested to get table, table name not found. ");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        //System.out.println("Valid name entered");
        if (tableName.equals(ALGORITHMS_TABLE)) {
            algorithmsTable = AlgorithmsTable.openTable(ALGORITHMS_TABLE, dbConnector);
            Logging.getLOG().debug("user requested to get table, table: " + ALGORITHMS_TABLE);
            return Response.ok(URLEncoder.encode(algorithmsTable.toJSON(), "UTF-8")).build();

        } else if (tableName.equals(DATA_STRUCTURES_TABLE)) {
            dataStructuresTable = DataStructuresTable.openTable(DATA_STRUCTURES_TABLE, dbConnector);
            Logging.getLOG().debug("user requested to get table, table: " + DATA_STRUCTURES_TABLE);
            return Response.ok(URLEncoder.encode(dataStructuresTable.toJSON(), "UTF-8")).build();

        } else if (tableName.equals(SOFTWARE_DESIGN_TABLE)) {
            softwareDesignTable = SoftwareDesignTable.openTable(SOFTWARE_DESIGN_TABLE, dbConnector);
            Logging.getLOG().debug("user requested to get table, table: " + SOFTWARE_DESIGN_TABLE);
            return Response.ok(URLEncoder.encode(softwareDesignTable.toJSON(), "UTF-8")).build();

        } else {
            // if it doesn't return that, then return an error
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Something went wrong")
                    .build();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //Used to delete an Item in the table
    @GET
    @Path("/deleteItem")
    public Response deleteItemFromTable(
            @QueryParam("tableName") String tableName,
             @QueryParam("key") String key //key is primaryKey for the table (which is a uuid)

    )  {

        List<String> missing = new ArrayList<String>();
        if(tableName == null) missing.add("table name");
        if( key == null) missing.add("key uuid");

        if(!missing.isEmpty()){
            String msg = "Missing parameters: " + missing.toString();
            System.out.println(msg);
            Logging.getLOG().debug("user requested to delete an item, parameters missing: " + msg);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        start();

        String decodedKey = "";
        if(!tables.contains(tableName)){
            String msg = "table not found";
            System.out.println(msg);
            Logging.getLOG().debug("user requested to delete item, table not found. error: " + Response.Status.BAD_REQUEST);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        try {
            decodedKey = URLDecoder.decode(key, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }

        if(tableName.equals(ALGORITHMS_TABLE)){
           algorithmsTable = AlgorithmsTable.openTable(ALGORITHMS_TABLE, dbConnector);

            assert(algorithmsTable != null);

            if(algorithmsTable.getItemWithAttribute(AlgorithmsTable.getKeyColumn(),decodedKey) != null){

                boolean status = algorithmsTable.deleteItemWithPrimaryKey(decodedKey);
                if(status) {
                    Logging.getLOG().debug("user requested to delete item from algorithms, item: " + algorithmsTable.getItemWithAttribute(AlgorithmsTable.getKeyColumn(),decodedKey));
                    return Response.ok("Success").build();
                }
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unable to add to " + ALGORITHMS_TABLE + " table")
                        .build();
            }

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid Item entered, does not meet the requirements for inserting value to "
                            + ALGORITHMS_TABLE + " table")
                    .build();
        }
        else if(tableName.equals(DataStructuresTable.getTableName())){
            if(dataStructuresTable.getItemWithAttribute(DataStructuresTable.getKeyColumn(),decodedKey) != null){
                boolean status = dataStructuresTable.deleteItemWithPrimaryKey(decodedKey);

                if(status) {
                    Logging.getLOG().debug("user requested to delete item from dataStructures, item: " + dataStructuresTable.getItemWithAttribute(DataStructuresTable.getKeyColumn(),decodedKey));
                    return Response.ok("Success").build();
                }
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unable to delete from " + DataStructuresTable.getTableName() + " table")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid Item entered, does not meet the requirements for inserting value to "
                            + DataStructuresTable.getTableName() + " table")
                    .build();
        }
        else if(tableName.equals(SoftwareDesignTable.getTableName())){

            if(softwareDesignTable.getItemWithAttribute(SoftwareDesignTable.getKeyColumn(),decodedKey) != null){
                boolean status = softwareDesignTable.deleteItemWithPrimaryKey(decodedKey);

                if(status) {
                    Logging.getLOG().debug("user requested to delete item from softwareDesigns, item: " + softwareDesignTable.getItemWithAttribute(SoftwareDesignTable.getKeyColumn(),decodedKey));
                    return Response.ok("Success").build();
                }
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unable to delete from " + SoftwareDesignTable.getTableName() + " table")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid Item entered, does not meet the requirements for deleting item from "
                            + SoftwareDesignTable.getTableName() + " table")
                    .build();
        }

        System.out.println("Not a valid item");
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("wrong")
                .build();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Used to decrement user's table of number regarding the specified table they're deleting an item from.
    //@GET
    //@Path("/decrementUserTable")



    //////////////////////////////////////////////////////////////////////////////////////////////////
    @GET
    @Path("/addUsers")
    public Response addToUserTable(
            //@QueryParam("userTable") String userTable,
            @QueryParam("facebookID") String facebookID,
            @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName
    ) throws UnsupportedEncodingException {

        List<String> missing = new ArrayList<String>();

        //if (userTable == null) missing.add("user table");
        if (facebookID == null) missing.add("facebookID key ");
        if (firstName == null) missing.add("firstname ");
        if (lastName == null) missing.add("lastname ");

        if (!missing.isEmpty()) {
            String msg = "Missing parameters: " + missing.toString();
            System.out.println(msg);
            Logging.getLOG().debug("users first login to app, but error occurred, missing params: " + missing.toString());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        start();

        UserTable userTable = UserTable.openTable(USER_TABLE, dbConnector);

        String decodedFBID = "";
        try {
            decodedFBID = URLDecoder.decode(facebookID, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }

        //if the item exists in the userTable DB
        if(userTable.getItemWithAttribute("facebookID", facebookID) != null)
        {
            Logging.getLOG().debug("user has logged in before, FBID: " + facebookID + " firstName: " + firstName + "lastName: " + lastName);
            return Response.ok("Success").build();
        }
        else //if the user does not exist in the DB, then add
        {
            Logging.getLOG().debug("user hasn't logged in before, FBID: " + facebookID + " firstName: " + firstName + "lastName: " + lastName);
            Item newUser = new Item()
                    .withPrimaryKey(UserTable.getKeyColumn(), facebookID)
                    .withString(UserTable.getFirstNameColumn(), firstName)
                    .withString(UserTable.getLastNameColumn(), lastName)
                    .withLong(UserTable.getAlgoNumColumn(), 0)
                    .withLong(UserTable.getSdNumColumn(), 0)
                    .withLong(UserTable.getDsNumColumn(), 0);
            userTable.put(newUser);
            return Response.ok("Success").build();
        }


    }

        //////////////////////////////////////////////////////////////////////////////////////////////////
    // Used to update an Item in the table.
    @GET
    @Path("/addItem")
    public Response addItemToTable(
            @QueryParam("tableName") String tableName,
            @QueryParam("item") String item
    ) throws UnsupportedEncodingException {

        List<String> missing = new ArrayList<String>();
        if(tableName == null) missing.add("table name");
        if( item == null) missing.add("Item object");

        if(!missing.isEmpty()){
            String msg = "Missing parameters: " + missing.toString();
            System.out.println(msg);
            Logging.getLOG().debug("user trying to add item, Missing parameters: " + missing.toString());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        start();

        if(!tables.contains(tableName)){
            String msg = "table not found";
            System.out.println(msg);
            Logging.getLOG().debug("user trying to add item, doesn't contain table: " + tableName);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        String decodedJSON = URLDecoder.decode(item, "UTF-8");
        Item row = new Item().fromJSON(decodedJSON);

        if(tableName.equals(ALGORITHMS_TABLE)){
            AlgorithmsTable algorithmsTable = AlgorithmsTable.openTable(ALGORITHMS_TABLE, dbConnector);

            if(algorithmsTable.validItem(row)){
                boolean status = algorithmsTable.put(row);
                if(status) {
                    Logging.getLOG().debug("user trying to add item to algorithms, item: "  + item);
                    return Response.ok("Success").build();
                }
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unable to add to " + ALGORITHMS_TABLE + " table")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid Item entered, does not meet the requirements for inserting value to "
                            + ALGORITHMS_TABLE + " table")
                    .build();
        }
        else if(tableName.equals(DataStructuresTable.getTableName())){
            if(dataStructuresTable.validItem(row)){
                boolean status = dataStructuresTable.put(row);
                if(status) {
                    Logging.getLOG().debug("user trying to add item to data structures, item: "  + item);
                    return Response.ok("Success").build();
                }
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unable to add to " + DataStructuresTable.getTableName() + " table")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid Item entered, does not meet the requirements for inserting value to "
                            + DataStructuresTable.getTableName() + " table")
                    .build();
        }
        else if(tableName.equals(SoftwareDesignTable.getTableName())){
            if(softwareDesignTable.validItem(row)){ //still need to create this function in the table class
                boolean status = softwareDesignTable.put(row); //fix attributes (i believe that is the issue with this 'put' function)

                if(status) {
                    Logging.getLOG().debug("user trying to add item to software design, item: "  + item);
                    return Response.ok("Success").build();
                }
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unable to add to " + SoftwareDesignTable.getTableName() + " table")
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid Item entered, does not meet the requirements for inserting value to "
                            + SoftwareDesignTable.getTableName() + " table")
                    .build();
        }

        System.out.println("Not a valid item");
        Logging.getLOG().debug("user trying to add item, item not valid ");
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("wrong")
                .build();
    }

    // This endpoint is currently being used for an iOS application. Will remove it once we get graded on it
    // -- Thankx :)
    @GET
    @Path("/getRecipes")
    public Response getRecipes(
            @QueryParam("items") String ingredients
    ){

        try {
            String decodedRecipes = URLDecoder.decode(ingredients, "UTF-8");
        } catch (Exception e){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error")
                    .build();
        }

        String baseURL = "https://api.edamam.com/search";
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(baseURL)
                .queryParam("app_id", "d1fc9900")
                .queryParam("app_key", "a9307ac9d85fe8df2ad5d37597245915")
                .queryParam("q", ingredients);
        String response = target.request().get(String.class);

        JSONObject jsonObject = new JSONObject(response);

        String recipes = jsonObject.get("hits").toString();

        JSONArray array = new JSONArray(recipes);

        StringBuilder results = new StringBuilder();
        for(int i = 0; i < array.length(); i++){
            JSONObject object = (JSONObject) array.get(i);
            JSONObject recipeObject = (JSONObject) object.get("recipe");

            //System.out.println(recipeObject.keySet());
//            System.out.println("Label: " + recipeObject.get("label"));
//            System.out.println("Image: " + recipeObject.get("image"));
//            System.out.println("URL: " + recipeObject.get("url"));
//            System.out.println();

            JSONObject data = new JSONObject();
            data.put("label", recipeObject.get("label"));
            data.put("image", recipeObject.get("image"));
            data.put("url", recipeObject.get("url"));

            results.append(data.toString() + "∑");
        }

        System.out.println(results.toString());

        return Response.ok(results.toString()).build();

        // Parsing JSON response

//
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        try{
//            Map<String, Object> map = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
//            });
//
//            //StringTokenizer tokenizer = new StringTokenizer(map.get("hits").toString(), "recipe=");
//           // System.out.println(map.get("hits"));
//            //System.out.println(map.keySet());
//
//        }
//        catch (Exception e){
//            System.out.println("Error parsing string");
//        }
//        return null;
    }



}
