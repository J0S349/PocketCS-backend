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
 * Created by Peeps on 10/31/16.
 */

public class SoftwareDesignTable {
    private static final String TABLE_NAME = "SoftwareDesign";
    private static final String KEY_COLUMN = "SDID";
    private static final String USER_ID_COLUMN = "userID";
    private static final String NAME_COLUMN = "SDName";
    private static final String CATEGORY_ID_COLUMN = "categoryID";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String BENEFIT_COLUMN = "benefit/analogy";
    private static final String DOWNSIDE_COLUMN = "downside/analogy";
    private static final String IMAGE_ID_COLUMN = "imageID";
    private static final String DATE_CREATED_COLUMN = "dateCreated";
    private static final String DATE_UPDATED_COLUMN = "dateUpdated";
    private static final String HELPFUL_LINK_COLUMN = "helpfulLink";

    private Table table;

    public static SoftwareDesignTable createTable(String tableName, DBConnector connector) {

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
        } catch (Exception e) {

            return null;
        }

        try {
            table.waitForActive();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return new SoftwareDesignTable(table);

    }

    private SoftwareDesignTable(Table table) {
        this.table = table;
    }

    public static SoftwareDesignTable openTable(String tableName, DBConnector connector) {
        Table table = connector.getDynamoDB().getTable(tableName);
        return new SoftwareDesignTable(table);
    }

    public boolean put(Item item){
        if(!validItem(item)){
            return false;
        }

        if(table.getItem(KEY_COLUMN, item.getString(KEY_COLUMN)) == null){
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

        if(table.getItem(KEY_COLUMN, item.getString(KEY_COLUMN)) != null) {
            // Taking advantage of tables ability to add / update an item that is entered into the table
            try{

                table.putItem(item);
                return true;

            } catch (Exception e){
                return false;
            }
        }
        return false;
    }

    public Item getItemWithAttribute(String attribute, String key) {
        return table.getItem(attribute, key);
    }

    public boolean deleteItemWithPrimaryKey(String ID) {
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(KEY_COLUMN,ID);
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

    public HashSet<String> getTableAttributes(){
        HashSet<String> hashSet = new HashSet<String>();

        hashSet.add(KEY_COLUMN);
        hashSet.add(USER_ID_COLUMN);
        hashSet.add(NAME_COLUMN);
        hashSet.add(CATEGORY_ID_COLUMN);
        hashSet.add(DESCRIPTION_COLUMN);
        hashSet.add(BENEFIT_COLUMN);
        hashSet.add(DOWNSIDE_COLUMN);
        hashSet.add(IMAGE_ID_COLUMN);
        hashSet.add(DATE_CREATED_COLUMN);
        hashSet.add(DATE_UPDATED_COLUMN);
        hashSet.add(HELPFUL_LINK_COLUMN);

        return hashSet;
    }

    public boolean validItem(Item item) {
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
            if (map.get(key) == null) {
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
                stringBuilder.append("<->");
        }

        return stringBuilder.toString();
    }

    public static String getTableName(){return TABLE_NAME;}
    public static String getKeyColumn(){return KEY_COLUMN; }
    public static String getUserIdColumn(){return USER_ID_COLUMN; }
    public static String getNameColumn(){return NAME_COLUMN; }
    public static String getCategoryIdColumn(){return CATEGORY_ID_COLUMN; }
    public static String getDescriptionColumn(){return DESCRIPTION_COLUMN;}
    public static String getBenefitColumn(){return BENEFIT_COLUMN;}
    public static String getDownsideColumn(){return DOWNSIDE_COLUMN; }
    public static String getImageIdColumn(){return IMAGE_ID_COLUMN; }
    public static String getDateCreatedColumn(){return DATE_CREATED_COLUMN; }
    public static String getDateUpdatedColumn(){return DATE_UPDATED_COLUMN; }
    public static String getHelpfulLinkColumn(){return HELPFUL_LINK_COLUMN; }

}
