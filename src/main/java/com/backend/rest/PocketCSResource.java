package com.backend.rest;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.backend.*;

import javax.swing.*;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * Created by Peeps on 10/26/16.
 */

@Path("")
public class PocketCSResource {
    private static DBConnector dbConnector = new DBConnector();
    private static final String ALGORITHMS_TABLE = "Algorithms";
    private static final String DATA_STRUCTURES_TABLE = "dataStructures";
    private static final String SOFTWARE_DESIGN_TABLE = "softwareDesign";

    // will be used to store the names of the tables for quick access
    private static HashSet<String> tables = new HashSet<String>();


    private static AlgorithmsTable algorithmsTable;
    private static DataStructuresTable dataStructuresTable;
    private static SoftwareDesignTable softwareDesignTable;

    // adds table names to HashSet
    public void start(){
        tables.add(ALGORITHMS_TABLE);
        tables.add(DATA_STRUCTURES_TABLE);
        tables.add(SOFTWARE_DESIGN_TABLE);
    }

    // Returns the contents from a specified table as JSON string
    @GET
    @Path("/getTable")
    public Response getTableInfo(
            @QueryParam("tableName") String tableName
    ) throws UnsupportedEncodingException {
        // check whether no parameters were passed
        if (tableName == null) {
            String msg = "missing parameter tableName";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }
        start();

        if (!tables.contains(tableName)) {
            String msg = "table not found";
            System.out.println(msg);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

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
                //boolean status = false;
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
                //boolean status = false;
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
