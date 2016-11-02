package com.backend.rest;

import com.backend.*;

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
    private static HashSet<String> tables = new HashSet<String>();


    private static AlgorithmsTable algorithmsTable;
    private static DataStructuresTable dataStructuresTable;
    private static SoftwareDesignTable softwareDesignTable;


    public void start(){
        tables.add(ALGORITHMS_TABLE);
        tables.add(DATA_STRUCTURES_TABLE);
        tables.add(SOFTWARE_DESIGN_TABLE);
    }

    public void tearDown()
    {
        if(!tables.isEmpty())
            tables.clear();
    }

    // Get the contents from a specific table
    @GET
    @Path("/table")
    public Response getTableInfo(
            @QueryParam("tableName") String tableName
    )
    {
        if (tableName == null){
            String msg = "missing parameter tableName";
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

        if(tableName.equals(ALGORITHMS_TABLE)){

            algorithmsTable = AlgorithmsTable.openTable(ALGORITHMS_TABLE, dbConnector);
            // will need to replace this so it can return a json string of
            // all the items within the table.
            return Response.ok(algorithmsTable.get(1).toJSON()).build();
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Something went wrong")
                .build();
    }

    
}
