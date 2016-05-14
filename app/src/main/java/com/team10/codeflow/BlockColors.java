package com.team10.codeflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Created by Federico on 10/05/2016.
 */
public class BlockColors extends PreferenceActivity{



    public int getColorForType(Block.BlockType type, Context ct, SharedPreferences preferences){
        String prefKey = getPrefKeyFromBlockType(type);

        Log.d("PREFERENCES BS", "PREF KEY IS: " + prefKey);

        String colorString = preferences.getString(prefKey, "#FFFFFF");//return white if it's not in the DB

        int color = Color.parseColor(String.valueOf(colorString));

        if(colorString.equalsIgnoreCase("#FFFFFF")){
            color = Color.parseColor(String.valueOf(getDefaultColor(type)));
        }

        return color;
    }

    private String getDefaultColor(Block.BlockType type){
        switch(type){
            case COMMENT:
                return "#e6e6e6";
            case VARIABLE:
                return "#99ff99";
            case INPUT:
                return "#33ccff";
            case OUTPUT:
                return "#ff6666";
            case WHILE_LOOP:
            case WHILE_LOOP_END:
                return "#6699ff";
            case FOR_LOOP:
            case FOR_LOOP_END:
                return "#33cccc";
            case IF_STATEMENT:
            case IF_STATEMENT_END:
                return "#ff99cc";
            case ELSE_STATEMENT:
            case ELSE_STATEMENT_END:
                return "#ffccff";
            default:
                return "#e6e6e6";
        }
    }

    private Block.BlockType getBlockTypeFromPrefKey(String prefKey){
        switch(prefKey){
            case "pref_COMMENT_color":
                return Block.BlockType.COMMENT;
            case "pref_VARIABLE_color":
                return Block.BlockType.VARIABLE;
            case "pref_INPUT_color":
                return Block.BlockType.INPUT;
            case "pref_OUTPUT_color":
                return Block.BlockType.OUTPUT;
            case "pref_WHILELOOP_color":
                return Block.BlockType.WHILE_LOOP;
            case "pref_FORLOOP_color":
                return Block.BlockType.FOR_LOOP;
            case "pref_IFSTATEMENT_color":
                return Block.BlockType.IF_STATEMENT;
            case "pref_ELSESTATEMENT_color":
                return Block.BlockType.ELSE_STATEMENT;
            default:
                return Block.BlockType.COMMENT;
        }
    }

    private String getPrefKeyFromBlockType(Block.BlockType type){
        switch(type){
            case COMMENT:
                return "pref_COMMENT_color";
            case VARIABLE:
                return "pref_VARIABLE_color";
            case INPUT:
                return "pref_INPUT_color";
            case OUTPUT:
                return "pref_OUTPUT_color";
            case WHILE_LOOP:
            case WHILE_LOOP_END:
                return "pref_WHILELOOP_color";
            case FOR_LOOP:
            case FOR_LOOP_END:
                return "pref_FORLOOP_color";
            case IF_STATEMENT:
            case IF_STATEMENT_END:
                return "pref_IFSTATEMENT_color";
            case ELSE_STATEMENT:
            case ELSE_STATEMENT_END:
                return "pref_ELSESTATEMENT_color";
            default:
                return "pref_COMMENT_color";
        }
    }

}
