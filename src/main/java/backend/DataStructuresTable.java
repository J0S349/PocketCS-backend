package backend;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Peeps on 10/26/16.
 */

public class DataStructuresTable {
    private static final String TABLE_NAME = "DataStructures";
    private static final String KEY_COLUMN = "DSID";
    private static final String USER_ID_COLUMN = "userID";
    private static final String NAME_COLUMN = "DSName";
    private static final String CATEGORY_ID_COLUMN = "categoryID";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String RUNTIME_COLUMN = "runtime";
    private static final String IMAGE_ID_COLUMN = "imageID";
    private static final String DATE_CREATED_COLUMN = "dateCreated";
    private static final String DATE_UPDATED_COLUMN = "dateUpdated";
    private static final String HELPFUL_LINK_COLUMN = "helpfulLink";

    private Table table;

    public static DataStructuresTable createTable(DBConnector connector) {
        return createTableHelper(connector, TABLE_NAME);
    }

    public static DataStructuresTable createTable(DBConnector connector, String tableName){
        return createTableHelper(connector, tableName);
    }

    private static DataStructuresTable createTableHelper(DBConnector connector, String tableName){
        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

        attributeDefinitions.add(new AttributeDefinition()
                .withAttributeName(KEY_COLUMN)
                .withAttributeType(ScalarAttributeType.S));

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


        return new DataStructuresTable(table);
    }

    private DataStructuresTable(Table table){
        this.table = table;
    }

    public static DataStructuresTable openTable(String tableName, DBConnector connector){
        Table table = connector.getDynamoDB().getTable(tableName);
        return new DataStructuresTable(table);
    }

    public boolean put(Item item){
        if(!validItem(item))
            return false;

        if(table.getItem(KEY_COLUMN, item.getString(KEY_COLUMN)) == null)
        {
            try{
                table.putItem(item);
                return true;
            } catch (Exception e){
                return false;
            }

        }
        else{
            return false;
        }
    }

    public boolean update(Item item){

        // contains all the appropriate attributes
        if(!validItem(item))
            return false;
        // check that the item is within the table already
        if(table.getItem(KEY_COLUMN, item.getString(KEY_COLUMN)) != null){

            // try adding it to the table
            try {
                // take advantage of table's ability to add / update values
                // with putItem function
                table.putItem(item);
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        else{
            return false;
        }
    }

    public Item getItemWithAttribute(String columnName, String value){
        Item item = table.getItem(columnName, value);
        return item;
    }
    public Item getItemWithAttribute(String columnName, long value){
        Item item = table.getItem(columnName, value);
        return item;
    }

    public boolean deleteItemWithPrimaryKey(String DSID) {
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(KEY_COLUMN, DSID);
        try {
            table.deleteItem(deleteItemSpec);
            return true;
        } catch (Exception e){
            return false;
        }
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

    // Returns a JSON string of all the Items within the table. Uses a
    // '<->' for the delimeter to parse the string in the future.
    public String toJSON(){
        StringBuilder stringBuilder = new StringBuilder();

        // get all the items from the table
        ItemCollection<ScanOutcome> items = table.scan();

        // get an iterator for the items in the table
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()){
            stringBuilder.append(iterator.next().toJSON());
            if(iterator.hasNext())
                stringBuilder.append("∑"); //using Sum notation character as delimeter
        }

        return stringBuilder.toString();
    }
    public HashSet<String> getTableAttributes(){
        HashSet<String> hashSet = new HashSet<String>();

        hashSet.add(KEY_COLUMN);
        hashSet.add(USER_ID_COLUMN);
        hashSet.add(NAME_COLUMN);
        hashSet.add(CATEGORY_ID_COLUMN);
        hashSet.add(DESCRIPTION_COLUMN);
        hashSet.add(RUNTIME_COLUMN);
        hashSet.add(IMAGE_ID_COLUMN);
        hashSet.add(DATE_CREATED_COLUMN);
        hashSet.add(DATE_UPDATED_COLUMN);
        hashSet.add(HELPFUL_LINK_COLUMN);

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
    // Getters for accessing column names
    public static String getKeyColumn(){return KEY_COLUMN;}
    public static String getNameColumn(){return NAME_COLUMN; }
    public static String getDescriptionColumn(){return DESCRIPTION_COLUMN; }
    public static String getUserIdColumn(){return USER_ID_COLUMN;}
    public static String getCategoryIdColumn(){return CATEGORY_ID_COLUMN; }
    public static String getRuntimeColumn(){return RUNTIME_COLUMN; }
    public static String getImageIdColumn(){return IMAGE_ID_COLUMN; }
    public static String getDateCreatedColumn(){return DATE_CREATED_COLUMN; }
    public static String getDateUpdatedColumn(){return DATE_UPDATED_COLUMN; }
    public static String getHelpfulLinkColumn(){return HELPFUL_LINK_COLUMN; }
    public static String getTableName(){return TABLE_NAME; }
}
