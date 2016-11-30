package backend.IntegrationTest;

import com.amazonaws.services.dynamodbv2.document.Item;
import backend.*;
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

public class DataStructuresCategoryTableIT {
    private static final String TABLE_NAME = "DataStructuresCategoryTableTest";
    private static final String NAME_COLUMN = "name";


    private DBConnector connector;
    private DataStructuresCategoryTable table;

    @Before
    public void connect()
    {
        connector = new DBConnector();
        DataStructuresCategoryTable.openTable(TABLE_NAME, connector).deleteTable();
        table = DataStructuresCategoryTable.createTable(connector, TABLE_NAME);

        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        table.put(3, "hashmap","maintains key and value pairs and often denoted as HashMap<Key, Value> or HashMap<K, V>. ");

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

        assertThat(name, equalTo("hashmap"));
    }

    @Test
    public void updateItemOnTable(){
        String description = " Like HashMap, Hashtable stores " +
                "key/value pairs in a hash table. When using a Hashtable, " +
                "you specify an object that is used as a key, and the value " +
                "that you want linked to that key.";

        String CategoryName = "hash table";
        int id = 3;
        table.update(id, CategoryName, description);

        Item result = table.get(3);
        String name = (String) result.get(NAME_COLUMN);

        assertThat(name, equalTo(CategoryName));
    }

    @Test
    public void deleteItem(){
        table.delete(3);

        assertNull(table.get(3));
    }
}
