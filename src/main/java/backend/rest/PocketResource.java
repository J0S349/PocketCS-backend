package backend.rest;

import backend.*;
import com.amazonaws.services.dynamodbv2.document.Item;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Peeps on 11/23/16.
 */

@Path("/")
public class PocketResource {

    private DBConnector dbConnector;
    private final String ALGORITHMS_TABLE = "Algorithms";
    private final String DATA_STRUCTURES_TABLE = "DataStructures";
    private final String SOFTWARE_DESIGN_TABLE = "softwareDesign";
    private final String USER_TABLE = "user";

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
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        //System.out.println("Valid name entered");
        if (tableName.equals(ALGORITHMS_TABLE)) {
            algorithmsTable = AlgorithmsTable.openTable(ALGORITHMS_TABLE, dbConnector);
            return Response.ok(URLEncoder.encode(algorithmsTable.toJSON(), "UTF-8")).build();

        } else if (tableName.equals(DATA_STRUCTURES_TABLE)) {
            dataStructuresTable = DataStructuresTable.openTable(DATA_STRUCTURES_TABLE, dbConnector);
            return Response.ok(URLEncoder.encode(dataStructuresTable.toJSON(), "UTF-8")).build();

        } else if (tableName.equals(SOFTWARE_DESIGN_TABLE)) {
            softwareDesignTable = SoftwareDesignTable.openTable(SOFTWARE_DESIGN_TABLE, dbConnector);
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
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        start();

        String decodedKey = "";
        if(!tables.contains(tableName)){
            String msg = "table not found";
            System.out.println(msg);
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
                if(status)
                    return Response.ok("Success").build();
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

                if(status)
                    return Response.ok("Success").build();
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

                if(status)
                    return Response.ok("Success").build();
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
            return Response.ok("Success").build();
        }
        else //if the user does not exist in the DB, then add
        {
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
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        start();

        if(!tables.contains(tableName)){
            String msg = "table not found";
            System.out.println(msg);
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
                if(status)
                    return Response.ok("Success").build();
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
                if(status)
                    return Response.ok("Success").build();
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

                if(status)
                    return Response.ok("Success").build();
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
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("wrong")
                .build();
    }




}
