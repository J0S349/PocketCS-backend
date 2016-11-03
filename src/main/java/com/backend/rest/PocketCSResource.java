package com.backend.rest;

import com.backend.*;

import javax.swing.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.lang.String;
import java.util.HashSet;


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
    ) {
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
            return Response.ok(algorithmsTable.toJSON()).build();

        } else if (tableName.equals(DATA_STRUCTURES_TABLE)) {
            dataStructuresTable = DataStructuresTable.openTable(DATA_STRUCTURES_TABLE, dbConnector);
            return Response.ok(dataStructuresTable.toJSON()).build();

        } else if (tableName.equals(SOFTWARE_DESIGN_TABLE)) {
            softwareDesignTable = SoftwareDesignTable.openTable(SOFTWARE_DESIGN_TABLE, dbConnector);
            return Response.ok(softwareDesignTable.toJSON()).build();

        } else {
            // if it doesn't return that, then return an error
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Something went wrong")
                    .build();
        }
    }

    //
    
}
