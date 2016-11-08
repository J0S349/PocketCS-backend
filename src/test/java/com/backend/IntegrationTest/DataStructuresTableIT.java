package com.backend.IntegrationTest;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.backend.DBConnector;
import com.backend.DataStructuresTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by GabrielZapata on 10/29/16.
 */
public class DataStructuresTableIT {

    private static final String TABLE_NAME = "DataStructuresTableTest";
    private static Item sessionRow;


    private DBConnector connector;
    private DataStructuresTable table;

    @Before
    public void connect()
    {
        connector = new DBConnector();
        DataStructuresTable.openTable(TABLE_NAME, connector).deleteTable();
        table = DataStructuresTable.createTable(TABLE_NAME, connector);

        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
        sessionRow = new Item()
                .withPrimaryKey( DataStructuresTable.getKeyColumn(), UUID.randomUUID().toString())
                .withLong(DataStructuresTable.getUserIdColumn(), 0)
                .withString(DataStructuresTable.getNameColumn(), "HashMap")
                .withInt(DataStructuresTable.getCategoryIdColumn(), 2)
                .withString(DataStructuresTable.getDescriptionColumn(), "It is a Data Structure used for accessing, storing and" +
                        " retreiving information fast. Only downside is that it takes up a fair amount of storage.")
                .withString(DataStructuresTable.getRuntimeColumn(), "Insertion: O(1)?Search: O(1)?Delete: O(1)")
                .withString(DataStructuresTable.getImageIdColumn(), "null")
                .withString(DataStructuresTable.getDateCreatedColumn(), timeStamp)
                .withString(DataStructuresTable.getDateUpdatedColumn(), timeStamp)
                .withString(DataStructuresTable.getHelpfulLinkColumn(), "null");

        table.put(sessionRow);
    }

    @After
    public void disconnect() {
        table.deleteTable(); //error on line: 33
        connector.close();
    }

    @Test
    public void createAndVerifyTable(){

        String keyColumn = sessionRow.getString(DataStructuresTable.getKeyColumn());
        Item result = table.getItemWithAttribute(DataStructuresTable.getKeyColumn(), keyColumn);

        String DSName = result.getString(DataStructuresTable.getNameColumn());

        assertThat(DSName, equalTo("HashMap"));
    }

    @Test
    public void updateItemOnTable(){

        sessionRow.with(DataStructuresTable.getNameColumn(), "HashTable");
        boolean status = table.update(sessionRow);

        assertThat(status, equalTo(true));
    }

    @Test
    public void updateItemNotInTable(){
        sessionRow.with(DataStructuresTable.getKeyColumn(), UUID.randomUUID().toString());

        boolean status = table.update(sessionRow);

        assertThat(status, equalTo(false));
    }

    @Test
    public void deleteItemWithinTable(){
        String key = sessionRow.getString(DataStructuresTable.getKeyColumn());
        boolean status = table.deleteItemWithPrimaryKey(key);

        assertThat(status, equalTo(true));
    }

    @Test
    public void deleteItemNotWithinTable(){
        String key = "";
        boolean status = table.deleteItemWithPrimaryKey(key);

        assertThat(status, equalTo(false));
    }


}
