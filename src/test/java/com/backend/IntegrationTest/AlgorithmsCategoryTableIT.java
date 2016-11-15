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
    //private static final String NAME_COLUMN = "name";
    private static Item sessionRow;

    private DBConnector connector;
    private AlgorithmsCategoryTable table;

    @Before
    public void connect()
    {
        connector = new DBConnector();
        AlgorithmsCategoryTable.openTable(TABLE_NAME, connector).deleteTable();
        table = AlgorithmsCategoryTable.createTable(TABLE_NAME, connector);

        sessionRow = new Item()
                    .withPrimaryKey(AlgorithmsCategoryTable.getKeyColumn(), 3)
                    .withString(AlgorithmsCategoryTable.getNameColumn(), "Divide-and-conquer")
                    .withString(AlgorithmsCategoryTable.getDescriptionColumn(), "recursively breaking down a problem into two or more " +
                            "sub-problems of the same or related type, until these " +
                            "become simple enough to be solved directly.");

        table.put(sessionRow);
    }

    @After
    public void disconnect() {
        table.deleteTable();
        connector.close();
    }

    @Test
    public void createAndVerifyTable(){
        int keyColumn = sessionRow.getInt(AlgorithmsCategoryTable.getKeyColumn());
        Item result = table.getItemWithAttribute(AlgorithmsCategoryTable.getKeyColumn(), keyColumn);
        String  name = result.getString(AlgorithmsCategoryTable.getNameColumn());

        assertThat(name, equalTo("Divide-and-conquer"));

    }

    @Test
    public void updateItemOnTable(){
        sessionRow.with(AlgorithmsCategoryTable.getNameColumn(), "Divide_and_conquer");
        boolean status = table.update(sessionRow); //simplified the past test to make
        assertThat(status, equalTo(true));         //more clear what im doing/testing
    }

    @Test
    public void deleteItem(){
        table.delete(3);

        assertNull(table.get(3));
    }

}
