package com.team10.codeflow;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.Serializable;

/**
 * Block class - for each pseudocode 'block' element
 */
public class Block implements Serializable {

    //Main block attributes
    private int blockCol;           //colour of block (from list in colors.xml) - Assigned in the setType() method
    private String blockName;       //display name of the block eg. IF, FOR (rather than FOR_LOOP) - Assigned in setType()
    private BlockType blockType;    //type of the block (from the BlockType enum) - Assigned in setType()

    //private SharedPreferences preferences;

    //User block attributes (variable names, operator signs etc)
    private String userVal1;
    private String userOperatorVal;
    private String userVal2;
    public transient Context context;

    //BLOCK TYPES:
    public enum BlockType {
        COMMENT,
        VARIABLE,
        INPUT,
        OUTPUT,
        WHILE_LOOP,
        WHILE_LOOP_END,
        FOR_LOOP,
        FOR_LOOP_END,
        IF_STATEMENT,
        IF_STATEMENT_END,
        ELSE_STATEMENT,
        ELSE_STATEMENT_END
    }

    /**
     * Block CustomView Constructors
     */
    public Block(Context context) {                     //default initialization block when no attributes are passed
        Log.d("SETTINGS:", "Block Created - No Param");
        this.context = context;
        setType(BlockType.COMMENT);
    }

    public Block(BlockType blockType, Context context) {     //initializing a block with a given BlockType
        Log.d("SETTINGS:", "Block Created");
        this.context = context;
        setType(blockType);
    }

    /**
     * Get a Block's BlockType
     * @return BlockType of the block
     */
    public BlockType getType() {
        return blockType;
    }

    /**
     * Set a Block's BlockType
     * This gives the block the specific colour and name, based on the BlockType
     * @param type Is the enum type eg. BlockType.VARIABLE
     */
    public void setType(BlockType type) {
        this.blockType = type;

        //Assign block colours (Note: These will need to reference to the user's settings colour choice)
        //Assign block display names
        BlockColors bc = new BlockColors();
        blockCol = bc.getColorForType(blockType, context, PreferenceManager.getDefaultSharedPreferences(context));
        Log.d("Colors assigned", "" + blockCol);
        switch (blockType) {
            case COMMENT:
                blockName = " //";
                userVal1 = "My Comment";
                break;
            case VARIABLE:
                blockName = "VAR";
                userVal1 = "myVar";
                userOperatorVal = "=";
                userVal2 = "";
                break;
            case INPUT:
                blockName = "INPUT";
                userVal1 = "";
                break;
            case OUTPUT:
                blockName = "OUTPUT";
                userVal1 = "alert( myVar )";
                break;
            case WHILE_LOOP:
                blockName = "WHILE";
                userVal1 = "";
                userOperatorVal = "<";
                userVal2 = "";
                break;
            case WHILE_LOOP_END:
                blockName = "END WHILE";
                break;
            case FOR_LOOP:
                blockName = "FOR";
                userVal1 = "Item i";
                userVal2 = "myItemList";
                break;
            case FOR_LOOP_END:
                blockName = "END FOR";
                break;
            case IF_STATEMENT:
                blockName = "IF";
                userOperatorVal = "==";
                break;
            case IF_STATEMENT_END:
                blockName = "END IF";
                break;
            case ELSE_STATEMENT:
                blockName = "ELSE";
                break;
            case ELSE_STATEMENT_END:
                blockName = "END ELSE";
                break;
            default:
                blockCol = R.color.lightgrey;
                break;
        }
        Log.d("Colors assigned", "" + blockCol);
        Log.d("SETTINGS:", "setType Completed");
    }

    /**
     * Get a Block's blockName
     * @return The String name of a Block
     */
    public String getName() {
        return blockName;
    }

    /**
     * Get a Block's blockCol
     * @return The int reference of a Block's background colour
     */
    public int getColor(){
        return blockCol;
    }

    public String getUserVal1() {
        return userVal1;
    }

    public void setUserVal1(String newVal) {
        this.userVal1 = newVal;
    }

    public String getUserVal2() {
        return userVal2;
    }

    public void setUserVal2(String newVal) {
        this.userVal2 = newVal;
    }

    public String getUserOperatorVal() {
        return userOperatorVal;
    }

    public void setUserOperatorVal(String newVal) {
        this.userOperatorVal = newVal;
    }

    public String stringify() {
        if (userVal1 != null && userVal1 != "") {
            if (userVal2 != null && userVal2 != "") {
                return blockName + " " + userVal1 + " " + userOperatorVal + " " + userVal2;
            } else {
                return blockName + " " + userVal1;
            }
        } else {
            return blockName;
        }
    }
}