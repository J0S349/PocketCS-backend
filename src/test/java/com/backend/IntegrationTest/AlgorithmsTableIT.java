package com.backend.IntegrationTest;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.backend.AlgorithmsTable;
import com.backend.DBConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Peeps on 10/25/16.
 */
public class AlgorithmsTableIT {

    private static final String TABLE_NAME = "AlgorithmsTableTest";
    private static Item sessionRow;


    private DBConnector connector;
    private AlgorithmsTable table;

    @Before
    public void connect()
    {
        connector = new DBConnector();
        AlgorithmsTable.openTable(TABLE_NAME, connector).deleteTable();
        table = AlgorithmsTable.createTable(TABLE_NAME, connector);

        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
        sessionRow = new Item()
                .withPrimaryKey( AlgorithmsTable.getKeyColumn(), UUID.randomUUID().toString())
                .withLong(AlgorithmsTable.getUserIdColumn(), 0)
                .withString(AlgorithmsTable.getNameColumn(), "Binary Search")
                .withInt(AlgorithmsTable.getCategoryIdColumn(), 1)
                .withString(AlgorithmsTable.getDescriptionColumn(), "It is used to find the specific index of a given value within a sorted list of values.")
                .withString(AlgorithmsTable.getRuntimeColumn(), "Best Case: log(n)?Worst Case: O(n)")
                .withString(AlgorithmsTable.getImageIdColumn(), "null")
                .withString(AlgorithmsTable.getDateCreatedColumn(), timeStamp)
                .withString(AlgorithmsTable.getDateUpdatedColumn(), timeStamp)
                .withString(AlgorithmsTable.getHelpfulLinkColumn(), "null");

        table.put(sessionRow);
    }

    @After
    public void disconnect() {
        table.deleteTable(); //error on line: 33
        connector.close();
    }

    @Test
    public void createAndVerifyTable(){
        String keyColumn = sessionRow.getString(AlgorithmsTable.getKeyColumn());
        Item result = table.getItemWithAttribute(AlgorithmsTable.getKeyColumn(), keyColumn);

        String algoName = result.getString(AlgorithmsTable.getNameColumn());

        assertThat(algoName, equalTo("Binary Search"));
    }

    @Test
    public void updateItemOnTable(){

        // update the algorithm name with something else
        sessionRow.with(AlgorithmsTable.getNameColumn(), "Binary Search Algorithm");

        boolean status = table.update(sessionRow);

        assertThat(status, equalTo(true));
    }

    @Test
    public void updateItemNotInTable(){

        //change the Primary key id to a new one
        sessionRow.withPrimaryKey(AlgorithmsTable.getKeyColumn(), UUID.randomUUID().toString());

        boolean status = table.update(sessionRow);

        assertThat(status, equalTo(false));
    }

    @Test
    public void deleteItemWithinTable(){
        String keyColumn = sessionRow.getString(AlgorithmsTable.getKeyColumn());

        boolean status = table.deleteItemWithPrimaryKey(keyColumn);

        assertThat(status, equalTo(true));
    }

    @Test
    public void deleteItemNotWithinTable(){
        String keyColumn = "";

        boolean status = table.deleteItemWithPrimaryKey(keyColumn);

        assertThat(status, equalTo(false));
    }
}
