package com.backend;

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

/**
 * Created by Peeps on 10/25/16.
 */
public class AlgorithmsTableIT {

    private static final String TABLE_NAME = "AlgorithmsTableTest";
    private static final String NAME_COLUMN = "algoName";


    private DBConnector connector;
    private AlgorithmsTable table;

    @Before
    public void connect()
    {
        connector = new DBConnector();
        AlgorithmsTable.openTable(TABLE_NAME, connector).deleteTable();
        table = AlgorithmsTable.createTable(TABLE_NAME, connector);

        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        table.put(1,0,"Binary Search", 1 ,"It is used to find the specific index of a given value within a sorted list of values.",
                "Best Case: log(n)?Worst Case: O(n)", "null", timeStamp, timeStamp,"null");

    }

    @After
    public void disconnect() {
        table.deleteTable(); //error on line: 33
        connector.close();
    }

    @Test
    public void createAndVerifyTable(){
        Item result = table.get(1);
        String algoName = (String) result.get(NAME_COLUMN);

        assertThat(algoName, equalTo("Binary Search"));
    }

    @Test
    public void updateItemOnTable(){
        table.update(1,1,"Binary Search Algorithm", 1 ,"It is a searching algorithm", "Best Case: log(n) Worst Case: O(n)", "blah", "today", "never","blah");

        Item result = table.get(1);
        String algoName = (String) result.get(NAME_COLUMN);

        assertThat(algoName, equalTo("Binary Search Algorithm"));
    }

    @Test
    public void deleteItem(){
        table.delete(2);

        assertNull(table.get(2));
    }

    @Test
    public void ItemBeingUpdatedNotWithinTable(){
        boolean success = table.update(2,1,"Binary Search Algorithm", 1 ,"It is a searching algorithm", "Best Case: log(n) Worst Case: O(n)", "blah", "today", "never","blah");

        assertThat(success, equalTo(false));
    }

}
