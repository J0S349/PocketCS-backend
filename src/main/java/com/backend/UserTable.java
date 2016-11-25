package com.backend;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by GabrielZapata on 11/22/16.
 */
public class UserTable {
    private static final String TABLE_NAME = "user";
    private static final String KEY_COLUMN = "facebookID";
    private static final String FIRST_NAME_COLUMN = "firstName";
    private static final String LAST_NAME_COLUMN = "lastName";
    private static final String ALGO_NUM_COLUMN = "numOfAlgo";
    private static final String DS_NUM_COLUMN = "numOfDS";
    private static final String SD_NUM_COLUMN = "numOfSD";


    private Table table;

    public static UserTable createTable(String tableName, DBConnector connector){

        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

        attributeDefinitions.add(new AttributeDefinition()
                .withAttributeName(KEY_COLUMN)
                .withAttributeType(ScalarAttributeType.S));

        // Create the KeySchema for knowing what is primary key(s) of the table
        KeySchemaElement keySchema = new KeySchemaElement()
                .withAttributeName(KEY_COLUMN)
                .withKeyType(KeyType.HASH); //Partition key

        KeySchemaElement sortSchema = new KeySchemaElement().clone()
                .withAttributeName(FIRST_NAME_COLUMN)
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
            return null;
        }

        try {
            table.waitForActive();

        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return new UserTable(table);

    }

    private UserTable(Table table){
        this.table = table;
    }

    public static UserTable openTable(String tableName, DBConnector connector){
        Table table = connector.getDynamoDB().getTable(tableName);
        return new UserTable(table);
    }

    public boolean put(Item item){

        //check if it contains the appropriate paramters
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
        return false;
    }

    public boolean update(Item item){

        // check if it is a valid item
        if(!validItem(item)){
            return false;
        }

        if(table.getItem(KEY_COLUMN, item.getString(KEY_COLUMN)) != null){
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
    public Item getItemWithAttribute(String columnName, String value){
        Item item = table.getItem(columnName, value);
        return item;
    }

    public Item getItemWithAttribute(String columnName, long value){
        Item item = table.getItem(columnName, value);
        return item;
    }

    public boolean deleteItemWithPrimaryKey(String id) {
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(KEY_COLUMN, id);
        try {
            table.deleteItem(deleteItemSpec);
            return true;
        }catch (Exception e){
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
    // '<->' for the delimiter to parse the string in the future.
    public String toJSON(){
        StringBuilder stringBuilder = new StringBuilder();

        // get all the items from the table
        ItemCollection<ScanOutcome> items = table.scan();

        // get an iterator for the items in the table
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()){
            stringBuilder.append(iterator.next().toJSON());
            if(iterator.hasNext())
                stringBuilder.append("<->");
        }
        return stringBuilder.toString();
    }

    public HashSet<String> getTableAttributes(){
        HashSet<String> hashSet = new HashSet<String>();

        hashSet.add(KEY_COLUMN);
        hashSet.add(FIRST_NAME_COLUMN);
        hashSet.add(LAST_NAME_COLUMN);
        hashSet.add(ALGO_NUM_COLUMN);
        hashSet.add(DS_NUM_COLUMN);
        hashSet.add(SD_NUM_COLUMN);

        return hashSet;
    }

    public boolean validItem(Item item){
        HashSet<String> hashSet = getTableAttributes();

        Map<String, Object> map = item.asMap();

        Set<String> keys = map.keySet();

        return containsValidNumberOfColumns(hashSet, map, keys);
    }

    private boolean containsValidNumberOfColumns(HashSet<String> hashSet, Map<String, Object> map, Set<String> keys) {
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
    public static String getTableName(){return TABLE_NAME;}
    public static String getKeyColumn(){return KEY_COLUMN;}
    public static String getFirstNameColumn(){return FIRST_NAME_COLUMN;}
    public static String getLastNameColumn(){return LAST_NAME_COLUMN;}
    public static String getAlgoNumColumn(){return ALGO_NUM_COLUMN;}
    public static String getDsNumColumn(){return DS_NUM_COLUMN;}
    public static String getSdNumColumn(){ return SD_NUM_COLUMN;}
}
