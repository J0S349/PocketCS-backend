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

public class SoftwareDesignCategoryTableIT {
    private static final String TABLE_NAME = "SoftwareDesignCategoryTableTest";
    private static final String NAME_COLUMN = "name";


    private DBConnector connector;
    private SoftwareDesignCategoryTable table;

    @Before
    public void connect()
    {
        connector = new DBConnector();
        DataStructuresCategoryTable.openTable(TABLE_NAME, connector).deleteTable();
        table = SoftwareDesignCategoryTable.createTable(TABLE_NAME, connector);
        
        assert(table != null);
        
        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        table.put(3, "Algorithm_strategy_pattern", "Addressing concerns related to " +
                "high-level strategies describing how to exploit application " +
                "characteristics on a computing platform.");

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

        assertThat(name, equalTo("Algorithm_strategy_pattern"));
    }

    @Test
    public void updateItemOnTable(){
        String description = "Addressing concerns related to " +
                "high-level strategies describing how to exploit application " +
                "characteristics on a computing platform.";
		String patternName = "Algorithm strategy pattern";
		int iD = 3;
		table.update(iD, patternName, description);

        Item result = table.get(iD);
        String name = (String) result.get(NAME_COLUMN);

        assertThat(name, equalTo(patternName));
    }

    @Test
    public void deleteItem(){
        table.delete(3);

        assertNull(table.get(3));
    }
}
