package backend;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by GabrielZapata on 11/2/16.
 */

public class AlgorithmsCategoryTable {
    private static final String TABLE_NAME = "AlgorithmsCategory";
    private static final String KEY_COLUMN = "ACID";
    private static final String NAME_COLUMN = "categoryName";
    private static final String DESCRIPTION_COLUMN = "description";

    private Table table;

    public static AlgorithmsCategoryTable createTable(DBConnector connector){

        return createTableHelper(connector, TABLE_NAME);
    }

    // This is mainly due to how we want to be able to test against a table within DynamoDB
    public static AlgorithmsCategoryTable createTable(DBConnector connector, String tableName){
        return createTableHelper(connector, tableName);
    }

    private static AlgorithmsCategoryTable createTableHelper(DBConnector connector, String tableName){
        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

        attributeDefinitions.add(new AttributeDefinition()
                .withAttributeName(KEY_COLUMN)
                .withAttributeType(ScalarAttributeType.N));

        // Create the KeySchema for knowing what is primary key(s) of the table
        KeySchemaElement keySchema = new KeySchemaElement()
                .withAttributeName(KEY_COLUMN)
                .withKeyType(KeyType.HASH); //Partition key

        KeySchemaElement sortSchema = new KeySchemaElement().clone()
                .withAttributeName(NAME_COLUMN)
                .withKeyType(KeyType.RANGE);

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


        return new AlgorithmsCategoryTable(table);
    }

    private AlgorithmsCategoryTable(Table table){
        this.table = table;
    }

    public static AlgorithmsCategoryTable openTable(String tableName, DBConnector connector){
        Table table = connector.getDynamoDB().getTable(tableName);
        return new AlgorithmsCategoryTable(table);
    }

    public boolean put(Item item)
    {
        //check if it contains the appropriate parameters
        if(!validItem(item))
            return false;

        if(table.getItem(KEY_COLUMN, item.getInt(KEY_COLUMN)) == null)
        {
            try{
                table.putItem(item);
                return true;

            } catch (Exception e){
                return false;
            }
        }
        return false;
    }

    public boolean update(Item item)
    {
        // Taking advantage of tables ability to add / update an item that is entered into the table
        // check if it is a valid item
        if(!validItem(item)){
            return false;
        }

        if(table.getItem(KEY_COLUMN, item.getInt(KEY_COLUMN)) != null){
            try{

                table.putItem(item);
                return true;

            } catch (Exception e){
                return false;
            }
        }
        else{
            // there is no item to update
            return false;
        }

    }
    // will return the first item that it matches with
    public Item getItemWithAttribute(String columnName, int value){
        Item item = table.getItem(columnName, value);
        return item;
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

    public HashSet<String> getTableAttributes(){
        HashSet<String> hashSet = new HashSet<String>();

        hashSet.add(KEY_COLUMN);
        hashSet.add(NAME_COLUMN);
        hashSet.add(DESCRIPTION_COLUMN);


        return hashSet;
    }

    public boolean validItem(Item item){
        HashSet<String> hashSet = getTableAttributes();

        Map<String, Object> map = item.asMap();

        Set<String> keys = map.keySet();

        for (String key : keys) {
            if (!hashSet.contains(key)) {
                return false;
            }

            // since it is within the HashSet, then we can remove it
            hashSet.remove(key);

            // check if it has a value
            if(map.get(key) == null){
                return false;
            } else {
                Object object = map.get(key);

                // for 'int' values, in dynamoDB, they are store as BigDecimal
                if (object instanceof BigDecimal) {
                    BigDecimal bigDecimal = (BigDecimal) object;
                    if (bigDecimal.intValue() < 0) {
                        return false;
                    }
                } else if (object instanceof String) {
                    String string = (String) object;
                    if (Strings.isNullOrEmpty(string)) {
                        return false;
                    }
                }
            }
        }

        // check whether the hashSet contains values, if it doesn't, then it doesn't meet the requirements
        if(hashSet.isEmpty())
            return true;
        return false;
    }

    //getters for getting the data info from the columns
    public static String getKeyColumn(){return KEY_COLUMN;}
    public static String getNameColumn(){return NAME_COLUMN;}
    public static String getDescriptionColumn(){return DESCRIPTION_COLUMN;}
    public static String getTableName(){return TABLE_NAME;}

}
