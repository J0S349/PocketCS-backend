package backend.IntegrationTest;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.xspec.NULL;
import backend.DBConnector;
import backend.SoftwareDesignTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by GabrielZapata on 10/30/16.
 */

public class SoftwareDesignTableIT {
    private static final String TABLE_NAME = "SoftwareDesignTableTest";
    private static final String NAME_COLUMN = "SDName";


    private DBConnector connector;
    private SoftwareDesignTable table;
    private static Item sessionRow;
    private String timeStamp;


    @Before
    public void connect()
    {
        connector = new DBConnector();
        SoftwareDesignTable.openTable(TABLE_NAME, connector).deleteTable();
        table = SoftwareDesignTable.createTable(TABLE_NAME, connector);

        assert(table != null);

        // Adding the same values every time
        timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        sessionRow = new Item()
                .withPrimaryKey(SoftwareDesignTable.getKeyColumn(), UUID.randomUUID().toString())
                .withLong(SoftwareDesignTable.getUserIdColumn(), 0)
                .withString(SoftwareDesignTable.getNameColumn(), "Singleton")
                .withLong(SoftwareDesignTable.getCategoryIdColumn(), 3)
                .withString(SoftwareDesignTable.getDescriptionColumn(), "It prevents you from creating new instances across different" +
                            " screens which is helpful when working with database instances")
                .withString(SoftwareDesignTable.getBenefitColumn(), "The same instance can be sharec across different instances")
                .withString(SoftwareDesignTable.getDownsideColumn(), "The object's data can be manipulated by other programs")
                .withString(SoftwareDesignTable.getImageIdColumn(), "null")
                .withString(SoftwareDesignTable.getDateCreatedColumn(), timeStamp)
                .withString(SoftwareDesignTable.getDateUpdatedColumn(), timeStamp)
                .withString(SoftwareDesignTable.getHelpfulLinkColumn(), "null");

        table.put(sessionRow);
    }

    @After
    public void disconnect() {
        table.deleteTable();
        connector.close();
    }

    @Test
    public void createAndVerifyTable(){
        String keyColumn = SoftwareDesignTable.getKeyColumn();
        Item row = table.getItemWithAttribute(SoftwareDesignTable.getKeyColumn(), sessionRow.getString(keyColumn));
        String name = row.getString(SoftwareDesignTable.getNameColumn());

        assertThat(name, equalTo("Singleton"));//<---
    }


    @Test
    public void updateItemOnTable(){

        sessionRow.withString(SoftwareDesignTable.getNameColumn(), "Singleton Pattern");
        boolean result = table.update(sessionRow);

        assertThat(result, equalTo(true));
    }

    @Test
    public void deleteItemWithinTable(){
        boolean result = table.deleteItemWithPrimaryKey(sessionRow.getString(SoftwareDesignTable.getKeyColumn()));
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void deleteItemNotWithinTable(){
        boolean result = table.deleteItemWithPrimaryKey("123");

        assertThat(result, equalTo(false));
    }

    @Test
    public void deleteItemWithAnEmptyPrimaryKe(){
        boolean result = table.deleteItemWithPrimaryKey("");

        assertThat(result, equalTo(false));
    }

    @Test
    public void insertItemWithInvalidNumberOfParameters(){
        Item items = new Item()
                .withPrimaryKey(SoftwareDesignTable.getKeyColumn(), UUID.randomUUID().toString())
                .withLong(SoftwareDesignTable.getUserIdColumn(), 0)
                .withString(SoftwareDesignTable.getNameColumn(), "Singleton")
                .withLong(SoftwareDesignTable.getCategoryIdColumn(), 3)
                .withString(SoftwareDesignTable.getDescriptionColumn(), "It prevents you from creating new instances across different" +
                        " screens which is helpful when working with database instances")
                .withString(SoftwareDesignTable.getBenefitColumn(), "The same instance can be sharec across different instances")
                .withString(SoftwareDesignTable.getDownsideColumn(), "The object's data can be manipulated by other programs")
                .withString(SoftwareDesignTable.getImageIdColumn(), "null")
                .withString(SoftwareDesignTable.getDateCreatedColumn(), "today");

        boolean result = table.put(items);

        assertThat(result, equalTo(false));
    }

    @Test
    public void getItemWithinTable(){
        String itemKey = sessionRow.getString(SoftwareDesignTable.getKeyColumn());
        Item object = table.getItemWithAttribute(SoftwareDesignTable.getKeyColumn(), itemKey);

        assertNotNull(object);
    }

}