package backend.IntegrationTest;

import com.amazonaws.services.dynamodbv2.document.Item;
import backend.UserTable;
import backend.DBConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by GabrielZapata on 11/25/16.
 */
public class UserTableIT {
    private static final String TABLE_NAME = "UserTableTest";
    private static Item sessionRow;


    private DBConnector connector;
    private UserTable table;


    @Before
    public void connect()
    {
        connector = new DBConnector();
        UserTable.openTable(TABLE_NAME, connector).deleteTable();
        table = UserTable.createTable(connector, TABLE_NAME);

        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
        sessionRow = new Item()

                .withPrimaryKey(UserTable.getKeyColumn(), UUID.randomUUID().toString())
                .withString(UserTable.getFirstNameColumn(),"bob")
                .withString(UserTable.getLastNameColumn(),"billy")
                .withLong(UserTable.getAlgoNumColumn(),2)
                .withLong(UserTable.getDsNumColumn(),2)
                .withLong(UserTable.getSdNumColumn(),2);

        table.put(sessionRow);
    }

    @After
    public void disconnect() {
        table.deleteTable(); //error on line: 33
        connector.close();
    }

    @Test
    public void createAndVerifyTable(){
        String keyColumn = sessionRow.getString(UserTable.getKeyColumn());
        Item result = table.getItemWithAttribute(UserTable.getKeyColumn(), keyColumn);

        String FBID = result.getString(UserTable.getFirstNameColumn());

        assertThat(FBID, equalTo("bob"));
    }

    @Test
    public void updateItemOnTable(){

        // update the algorithm name with something else
        sessionRow.with(UserTable.getFirstNameColumn(), "Robert");

        boolean status = table.update(sessionRow);

        assertThat(status, equalTo(true));
    }

    @Test
    public void updateItemNotInTable(){

        //change the Primary key id to a new one
        sessionRow.withPrimaryKey(UserTable.getKeyColumn(), UUID.randomUUID().toString());

        boolean status = table.update(sessionRow);

        assertThat(status, equalTo(false));
    }

    @Test
    public void deleteItemWithinTable(){
        String keyColumn = sessionRow.getString(UserTable.getKeyColumn());

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
