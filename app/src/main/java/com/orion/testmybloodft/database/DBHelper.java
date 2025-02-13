package com.orion.testmybloodft.database;

/**
 * Created by Arun on 02/02/17.
 */

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orion.testmybloodft.config.App;
import com.orion.testmybloodft.models.AddonProductMod;

/**
 * Defining the Database Handler
 */

public class DBHelper  extends SQLiteOpenHelper {
    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION =1;
    // Database Name
    private static final String DATABASE_NAME = "TestMyBloodFT.db";
    private static final String TAG = DBHelper.class.getSimpleName().toString();

    private static final String TABLE_TODAY_SCHEDULE = "today";

    /*Common column names*/
    private static final String Key_Row_Id = "_id";
    private static final String Key_Session_Mail_Id = "sessionMailId";

    /*column names table user cards*/
    private static final String Key_Card_Default = "defaultCard";
    private static final String Key_Card_Id = "cardId";
    private static final String Key_Card_Brand = "brand";
    private static final String Key_Card_Expiry_Month = "expiryMonth";
    private static final String Key_Card_Expiry_Year = "expiryYear";
    private static final String Key_Card_Last4 = "last4";

    /*column names table addon*/
    private static final String Key_Addon_Id = "id";
    private static final String Key_Addon_Name = "name";
    private static final String Key_Addon_Quantity = "quantity";
    private static final String Key_Addon_Image = "image";
    private static final String Key_Addon_Total_Price = "totalPrice";
    private static final String Key_Addon_Price = "price";

    /*column names table property*/
    private static final String Key_Property_Id = "property_id";
    private static final String Key_Property_Basic = "dryCost";
    private static final String Key_Property_FullBoard = "fullBoardCost";
    private static final String Key_Property_Checkin = "checkIn";
    private static final String Key_Property_Checkout = "checkOut";
    private static final String Key_Property_Basic_Qnty = "basicQnty";
    private static final String Key_Property_FullBoard_Qnty = "fullBoardQnty";
    private static final String TOKEN = "token";

    public DBHelper( ) {
        super(App.getInstance(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*User credit cards table create statement*/
    private static final String CREATE_TABLE_USER_CARDS = "CREATE TABLE "
            + TABLE_TODAY_SCHEDULE + "(" + Key_Row_Id
            + " INTEGER PRIMARY KEY," + Key_Card_Default
            + " TEXT," + Key_Card_Id
            + " TEXT," + Key_Card_Brand
            + " TEXT," + Key_Card_Expiry_Month
            + " TEXT," + Key_Card_Expiry_Year
            + " TEXT," + Key_Card_Last4
            + " TEXT," + Key_Session_Mail_Id
            + " TEXT" + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here
       // db.execSQL(SportsRepo.createTable());
        db.execSQL(CREATE_TABLE_USER_CARDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop table if existed, all data will be gone!!!
        //db.execSQL("DROP TABLE IF EXISTS " + Sports.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODAY_SCHEDULE);
        onCreate(db);
    }


    /*
   * Create a Addon */
    public long createAddon(AddonProductMod addon) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Key_Addon_Id, addon.getId());
        values.put(Key_Addon_Name, addon.getName());
        values.put(Key_Addon_Quantity, addon.getQuantity());
        values.put(Key_Addon_Image, addon.getImage());
        values.put(Key_Addon_Price, addon.getPrice());
        values.put(Key_Addon_Total_Price, addon.getTotalPrice());
        values.put(Key_Property_Id, addon.getPropertyId());
        // insert row

        return db.insert(TABLE_TODAY_SCHEDULE, null, values);
    }

    /*
    * Deleting a Today table
    */
    public void deleteTodayTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODAY_SCHEDULE, null,null);
    }


}