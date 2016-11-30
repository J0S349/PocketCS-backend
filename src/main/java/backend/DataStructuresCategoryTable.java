package backend;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.ArrayList;

/**
 * Created by GabrielZapata on 11/2/16.
 */

public class DataStructuresCategoryTable {

    private static final String TABLE_NAME = "DataStructuresCategory";
    private static final String KEY_COLUMN = "DCID";
    private static final String NAME_COLUMN = "name";
    private static final String DESCRIPTION_COLUMN = "description";

    private Table table;

    public static DataStructuresCategoryTable createTable(DBConnector connector){
        return createTableHelper(connector, TABLE_NAME);
    }

    // This is mainly due to how we want to be able to test against a table within DynamoDB
    public static DataStructuresCategoryTable createTable(DBConnector connector, String tableName){
        return createTableHelper(connector, tableName);
    }

    private static DataStructuresCategoryTable createTableHelper(DBConnector connector, String tableName){
        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

        attributeDefinitions.add(new AttributeDefinition()
                .withAttributeName(KEY_COLUMN)
                .withAttributeType(ScalarAttributeType.N));

        // Create the KeySchema for knowing what is primary key(s) of the table
        KeySchemaElement keySchema = new KeySchemaElement()
                .withAttributeName(KEY_COLUMN)
                .withKeyType(KeyType.HASH); //Partition key

        // Now we create a table request so that DynamoDB know that we want to create a table
        CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(keySchema)
                //.withKeySchema(sortSchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L));

        // Now we create the request to create the table.
        Table table;
        try {
            table = connector.getDynamoDB().createTable(request);
        } catch (Exception e){

            e.printStackTrace();
            return null;
        }

        try {
            table.waitForActive();

        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }


        return new DataStructuresCategoryTable(table);
    }



    private DataStructuresCategoryTable(Table table){
        this.table = table;
    }

    public static DataStructuresCategoryTable openTable(String tableName, DBConnector connector){
        Table table = connector.getDynamoDB().getTable(tableName);
        return new DataStructuresCategoryTable(table);
    }

    public void put(long ID, String name, String description)
    {

        Item sessionRow = new Item()
                .withPrimaryKey(KEY_COLUMN, ID)
                .withString(NAME_COLUMN, name)
                .withString(DESCRIPTION_COLUMN, description);
        try{
            table.putItem(sessionRow);

        } catch (Exception e){
            System.out.println("Error adding row to table");
            e.printStackTrace();
        }
    }

    public void update(
            long ID, String name, String description
    )
    {
        // Taking advantage of tables ability to add / update an item that is entered into the table
        put(ID, name, description);

    }

    public Item get(long ID){

        Item item = table.getItem(KEY_COLUMN, ID);
        return item;
    }
    public void delete(long ID) {
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(KEY_COLUMN, ID);
        table.deleteItem(deleteItemSpec);
    }
    public boolean deleteTable() {
        try {
            table.delete();
        } catch (ResourceNotFoundException ex) {
            return false; // didn't exist
        }
        try {
            table.waitForDelete();
        } catch (InterruptedException ex) {
            // See http://www.yegor256.com/2015/10/20/interrupted-exception.html
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
        return true; // Success table deletion
    }

    public static String getTableName(){return TABLE_NAME; }
    public static String getKeyColumn(){return KEY_COLUMN; }
    public static String getNameColumn(){return NAME_COLUMN; }
    public static String getDescriptionColumn(){return DESCRIPTION_COLUMN; }
}
