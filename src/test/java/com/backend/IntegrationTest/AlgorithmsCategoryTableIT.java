package com.backend.IntegrationTest;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.backend.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


/**
 * Created by GabrielZapata on 11/5/16.
 */
public class AlgorithmsCategoryTableIT {

    private static final String TABLE_NAME = "AlgorithmsCategoryTableTest";
    private static final String NAME_COLUMN = "name";


    private DBConnector connector;
    private AlgorithmsCategoryTable table;

    @Before
    public void connect()
    {
        connector = new DBConnector();
        AlgorithmsCategoryTable.openTable(TABLE_NAME, connector).deleteTable();
        table = AlgorithmsCategoryTable.createTable(TABLE_NAME, connector);

        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        table.put(3, "Divide-and-conquer","recursively breaking down a problem into" +
                " two or more sub-problems of the same or related type, until these " +
                "become simple enough to be solved directly.");

    }

    @After
    public void disconnect() {
        table.deleteTable(); //error on line: 33
        connector.close();
    }

    @Test
    public void createAndVerifyTable(){
        Item result = table.get(3);
        String  name = (String) result.get(NAME_COLUMN);

        assertThat(name, equalTo("Divide-and-conquer"));
    }

    @Test
    public void updateItemOnTable(){
        table.update(3, "Divide_and_conquer","recursively breaking down a problem into" +
                " two or more sub-problems of the same or related type, until these " +
                "become simple enough to be solved directly.");

        Item result = table.get(3);
        String name = (String) result.get(NAME_COLUMN);

        assertThat(name, equalTo("Divide_and_conquer"));
    }

    @Test
    public void deleteItem(){
        table.delete(3);

        assertNull(table.get(3));
    }

}
