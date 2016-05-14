/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.team10.codeflow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *  This class is used for Sandbox screen's Drag & Drop to reorder list
 *  It is a Custom ListView Adapter to allow each list element to be a custom element
 *  In our case - each element is a Block, with a specific layout depending on it's type
 */
public class StableArrayAdapter extends ArrayAdapter<Block> {

    public static HashMap<Block, Integer> mIdMap = new HashMap<Block, Integer>();
    final int INVALID_ID = -1;;

    /**
     * View Holder caches the layout items that repeat
     * ie. Background, Highlight, Block name, Corners, Variable EditTexts
     */
    static class ViewHolderItem {
        //Layout views for ALL layouts
        ImageView bgCol;
        ImageView corner1;
        ImageView corner2;
        ImageView corner3;
        ImageView corner4;
        TextView blockName;
        //Common layout views
        EditText etVarName;
        EditText etVarAssign;
        View operator_tooltip;
        EditText etWhileName;
        EditText etWhileAssign;
        View operator_tooltip_while;
        EditText etForName;
        EditText etForAssign;
        EditText etIfName;
        EditText etIfAssign;
        View operator_tooltip_if;
        //Views with specific click handlers
        TextView equals;
        TextView plusEquals;
        TextView minusEquals;
        TextView greaterThan;
        TextView lessThan;
        TextView greaterThanEqual;
        TextView lessThanEqual;
        TextView isEqual;
        TextView notEqual;
        CustomBlockOperator operatorView;
        CustomBlockOperator operatorView_while;
        CustomBlockOperator operatorView_if;
        EditText editText_comment;
        EditText editText_io;
    }

    public StableArrayAdapter(Context context, int textViewResourceId, List<Block> objects) {
        super(context, 0, objects);
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @Override
    public int getViewTypeCount() {
        //SET THE NUMBER OF LAYOUTS USED FOR BLOCKS
        return 7;
    }

    @Override
    public int getItemViewType(int position) {
        //SET THE TYPE OF LAYOUT DEPENDING ON THE BLOCK TYPE
        int layoutType = 0;
        switch (getItem(position).getType()) {
            case COMMENT: layoutType = 0; break;
            case VARIABLE: layoutType = 1; break;
            case INPUT: layoutType = 2; break;
            case OUTPUT: layoutType = 2; break;
            case WHILE_LOOP: layoutType = 3; break;
            case WHILE_LOOP_END: layoutType = 4; break;
            case FOR_LOOP: layoutType = 5; break;
            case FOR_LOOP_END: layoutType = 4; break;
            case IF_STATEMENT: layoutType = 6; break;
            case IF_STATEMENT_END: layoutType = 4; break;
            case ELSE_STATEMENT: layoutType = 4; break;
            case ELSE_STATEMENT_END: layoutType = 4; break;
            default: layoutType = 0; break;
        }
        return layoutType;
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        Block item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // Get the Block item for this position
        final Block block = getItem(position);
        final Block.BlockType blockType = block.getType();

        final ViewHolderItem viewHolder;

        int type = getItemViewType(position);

        //If the view doesn't exist yet (can't be reused, so create a new one)
        if (convertView == null) {

            //CHOOSE THE LAYOUT FOR THE ROW
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (type) {
                case 0: //IF LAYOUT TYPE 0 (DEFAULT / COMMENT)
                    convertView = inflater.inflate(R.layout.block_row_default, parent, false);
                    break;
                case 1: //IF LAYOUT TYPE 1 (VARIABLE)
                    convertView = inflater.inflate(R.layout.block_row_variable, parent, false);
                    break;
                case 2: //IF LAYOUT TYPE 2 (INPUT / OUTPUT)
                    convertView = inflater.inflate(R.layout.block_row_io, parent, false);
                    break;
                case 3: //IF LAYOUT TYPE 3 (WHILE_LOOP)
                    convertView = inflater.inflate(R.layout.block_row_while, parent, false);
                    break;
                case 4: //IF LAYOUT TYPE 4 (WHILE_LOOP_END, FOR_LOOP_END, IF_STATEMENT_END, ELSE_STATEMENT, ELSE_STATEMENT_END)
                    convertView = inflater.inflate(R.layout.block_row_default, parent, false);
                    break;
                case 5: //IF LAYOUT TYPE 5 (FOR_LOOP)
                    convertView = inflater.inflate(R.layout.block_row_for, parent, false);
                    break;
                case 6: //IF LAYOUT TYPE 6 (IF_STATEMENT)
                    convertView = inflater.inflate(R.layout.block_row_if, parent, false);
                    break;
                default:
                    convertView = inflater.inflate(R.layout.block_row_default, parent, false);
                    break;
            }

            //Set up the ViewHolder to improve performance
            viewHolder = new ViewHolderItem();
            viewHolder.bgCol = (ImageView) convertView.findViewById(R.id.bgCol);
            viewHolder.corner1 = (ImageView) convertView.findViewById(R.id.corner1);
            viewHolder.corner2 = (ImageView) convertView.findViewById(R.id.corner2);
            viewHolder.corner3 = (ImageView) convertView.findViewById(R.id.corner3);
            viewHolder.corner4 = (ImageView) convertView.findViewById(R.id.corner4);
            viewHolder.blockName = (TextView) convertView.findViewById(R.id.blockName);

            viewHolder.etVarName = (EditText) convertView.findViewById(R.id.editText_varName);
            viewHolder.etVarAssign = (EditText) convertView.findViewById(R.id.editText_varAssign);
            viewHolder.etWhileName = (EditText) convertView.findViewById(R.id.editText_whileName);
            viewHolder.etWhileAssign = (EditText) convertView.findViewById(R.id.editText_whileAssign);
            viewHolder.etForName = (EditText) convertView.findViewById(R.id.editText_forName);
            viewHolder.etForAssign = (EditText) convertView.findViewById(R.id.editText_forAssign);
            viewHolder.etIfName = (EditText) convertView.findViewById(R.id.editText_ifName);
            viewHolder.etIfAssign = (EditText) convertView.findViewById(R.id.editText_ifAssign);
            viewHolder.operator_tooltip = convertView.findViewById(R.id.operator_tooltip);
            viewHolder.operator_tooltip_while = convertView.findViewById(R.id.operator_tooltip_while);
            viewHolder.operator_tooltip_if = convertView.findViewById(R.id.operator_tooltip_if);
            viewHolder.operatorView = (CustomBlockOperator) convertView.findViewById(R.id.operator_var);
            viewHolder.operatorView_while = (CustomBlockOperator) convertView.findViewById(R.id.operator_while);
            viewHolder.operatorView_if = (CustomBlockOperator) convertView.findViewById(R.id.operator_if);
            viewHolder.equals = (TextView) convertView.findViewById(R.id.equals);
            viewHolder.plusEquals = (TextView) convertView.findViewById(R.id.plusEquals);
            viewHolder.minusEquals = (TextView) convertView.findViewById(R.id.minusEquals);
            viewHolder.greaterThan = (TextView) convertView.findViewById(R.id.greaterThan);
            viewHolder.lessThan = (TextView) convertView.findViewById(R.id.lessThan);
            viewHolder.greaterThanEqual = (TextView) convertView.findViewById(R.id.greaterThanEqual);
            viewHolder.lessThanEqual = (TextView) convertView.findViewById(R.id.lessThanEqual);
            viewHolder.isEqual = (TextView) convertView.findViewById(R.id.isEqual);
            viewHolder.notEqual = (TextView) convertView.findViewById(R.id.notEqual);

            viewHolder.editText_comment = (EditText) convertView.findViewById(R.id.editText_comment);
            viewHolder.editText_io = (EditText) convertView.findViewById(R.id.editText_io);

            //Store the holder with the view.
            convertView.setTag(viewHolder);

        }
        else {
            //If reusing existing view - use the viewHolder to save resources
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        //Create the click listeners for the layout
        createClickListener(viewHolder, block, type);

        //Set rounded corners on each block
        viewHolder.corner1.setRotation(180);
        viewHolder.corner2.setRotation(270);
        viewHolder.corner4.setRotation(90);

        //Get block's colour from type
        viewHolder.bgCol.setBackgroundColor(block.getColor());//ContextCompat.getColor(getContext(), block.getColor())
        viewHolder.bgCol.setAlpha((float) 1);

        //Create Block's Name
        viewHolder.blockName.setText(block.getName());

        //Activate the correct block layout based on it's type
        setBlockLayout(block, type, viewHolder);

        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * Activate the correct block layout depending on the Block's BlockType
     * This method will assign the values for layout elements (views) depending on block type and the current Block object
     * @param block       Current Block element
     * @param type        Type of block element
     */
    private void setBlockLayout(final Block block, int type, final ViewHolderItem viewHolder) {

        //If block is of type COMMENT or DEFAULT
        if (type == 0) {
            viewHolder.bgCol.setAlpha((float) 0.7);
            viewHolder.editText_comment.setText(block.getUserVal1());
        }
        //If block is of type VARIABLE
        else if (type == 1) {
            viewHolder.operator_tooltip.setVisibility(View.GONE);

            viewHolder.etVarName.setText(block.getUserVal1());
            viewHolder.etVarAssign.setText(block.getUserVal2());

            //Get operator
            viewHolder.operatorView.newOperator(block.getUserOperatorVal());
        }
        //If block is of type INPUT or OUTPUT
        else if (type == 2) {
            viewHolder.editText_io.setText(block.getUserVal1());
        }
        //If block is of type WHILE
        else if (type == 3) {
            viewHolder.operator_tooltip_while.setVisibility(View.GONE);

            viewHolder.etWhileName.setText(block.getUserVal1());
            viewHolder.etWhileAssign.setText(block.getUserVal2());

            //Get operator
            viewHolder.operatorView_while.newOperator(block.getUserOperatorVal());
        }
        //If block is of type END or ELSE
        if (type == 4) {
            viewHolder.editText_comment.setVisibility(View.GONE);
        }
        //If block is of type FOR
        if (type == 5) {
            viewHolder.etForName.setText(block.getUserVal1());
            viewHolder.etForAssign.setText(block.getUserVal2());
        }
        //If block is of type IF
        if (type == 6) {
            viewHolder.operator_tooltip_if.setVisibility(View.GONE);

            viewHolder.etIfName.setText(block.getUserVal1());
            viewHolder.etIfAssign.setText(block.getUserVal2());

            //Get operator
            viewHolder.operatorView_if.newOperator(block.getUserOperatorVal());
        }
    }

    /**
     * Create click listeners for views on each layout
     * @param viewHolder
     * @param block
     * @param type integer for type of layout
     */
    private void createClickListener(final ViewHolderItem viewHolder, final Block block, int type) {
        //IF DEFAULT or COMMENT LAYOUT (TYPE 0)
        if (type == 0) {
            //Create click handler for comment edit text
            viewHolder.editText_comment.setOnDragListener(dropListener);
            viewHolder.editText_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter a comment:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.editText_comment.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal1(enterText.getText().toString());
                            viewHolder.editText_comment.setText(enterText.getText());
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });
        }
        //IF VARIABLE LAYOUT (TYPE 1)
        else if (type == 1) {
            //Create click handler for var name Edit Text
            viewHolder.etVarName.setOnDragListener(dropListener);
            viewHolder.etVarName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter new variable name:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.etVarName.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal1(enterText.getText().toString());
                            viewHolder.etVarName.setText(enterText.getText());
                            Toast.makeText(getContext(), "Saved this to block: "+getPosition(block), Toast.LENGTH_SHORT).show();
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });

            //Create click handler for variableAssignment Edit Text
            viewHolder.etVarAssign.setOnDragListener(dropListener);
            viewHolder.etVarAssign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter the assignment:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.etVarAssign.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal2(enterText.getText().toString());
                            viewHolder.etVarAssign.setText(enterText.getText());
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });

            //Create click handler for equals
            viewHolder.equals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("=");
                    viewHolder.operatorView.newOperator("=");
                    viewHolder.operator_tooltip.setVisibility(View.GONE);
                }
            });

            //Create click handler for plusEquals
            viewHolder.plusEquals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("+=");
                    viewHolder.operatorView.newOperator("+=");
                    viewHolder.operator_tooltip.setVisibility(View.GONE);
                }
            });

            //Create click handler for minusEquals
            viewHolder.minusEquals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("-=");
                    viewHolder.operatorView.newOperator("-=");
                    viewHolder.operator_tooltip.setVisibility(View.GONE);
                }
            });

            //Create click handler for the operator custom view
            viewHolder.operatorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Show operator tooltip selections
                    viewHolder.operator_tooltip.setVisibility(View.VISIBLE);
                }
            });
        }
        //IF INPUT / OUTPUT LAYOUT (TYPE 2)
        else if (type == 2) {
            //Create Click handlers for IO edit text
            viewHolder.editText_io.setOnDragListener(dropListener);
            viewHolder.editText_io.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter an input/output:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.editText_io.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal1(enterText.getText().toString());
                            viewHolder.editText_io.setText(enterText.getText());
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });
        }
        //IF WHILE LAYOUT (TYPE 3)
        else if (type == 3) {
            //Create click handler for var name Edit Text
            viewHolder.etWhileName.setOnDragListener(dropListener);
            viewHolder.etWhileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter new variable name:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.etWhileName.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal1(enterText.getText().toString());
                            viewHolder.etWhileName.setText(enterText.getText());
                            Toast.makeText(getContext(), "Saved this to block: "+getPosition(block), Toast.LENGTH_SHORT).show();
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });

            //Create click handler for variableAssignment Edit Text
            viewHolder.etWhileAssign.setOnDragListener(dropListener);
            viewHolder.etWhileAssign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter the assignment:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.etWhileAssign.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal2(enterText.getText().toString());
                            viewHolder.etWhileAssign.setText(enterText.getText());
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });

            //Create click handler for greaterThan
            viewHolder.greaterThan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal(">");
                    viewHolder.operatorView_while.newOperator(">");
                    viewHolder.operator_tooltip_while.setVisibility(View.GONE);
                }
            });

            //Create click handler for lessThan
            viewHolder.lessThan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("<");
                    viewHolder.operatorView_while.newOperator("<");
                    viewHolder.operator_tooltip_while.setVisibility(View.GONE);
                }
            });

            //Create click handler for greaterThanEqual
            viewHolder.greaterThanEqual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal(">=");
                    viewHolder.operatorView_while.newOperator(">=");
                    viewHolder.operator_tooltip_while.setVisibility(View.GONE);
                }
            });

            //Create click handler for lessThanEqual
            viewHolder.lessThanEqual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("<=");
                    viewHolder.operatorView_while.newOperator("<=");
                    viewHolder.operator_tooltip_while.setVisibility(View.GONE);
                }
            });

            //Create click handler for notEqual
            viewHolder.notEqual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("!=");
                    viewHolder.operatorView_while.newOperator("!=");
                    viewHolder.operator_tooltip_while.setVisibility(View.GONE);
                }
            });

            //Create click handler for isEqual
            viewHolder.isEqual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("==");
                    viewHolder.operatorView_while.newOperator("==");
                    viewHolder.operator_tooltip_while.setVisibility(View.GONE);
                }
            });


            //Create click handler for the operator custom view
            viewHolder.operatorView_while.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Show operator tooltip selections
                    viewHolder.operator_tooltip_while.setVisibility(View.VISIBLE);
                }
            });
        }
        //IF FOR LAYOUT (TYPE 5)
        else if (type == 5) {
            //Create click handler for var name Edit Text
            viewHolder.etForName.setOnDragListener(dropListener);
            viewHolder.etForName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter new variable name:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.etForName.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal1(enterText.getText().toString());
                            viewHolder.etForName.setText(enterText.getText());
                            Toast.makeText(getContext(), "Saved this to block: " + getPosition(block), Toast.LENGTH_SHORT).show();
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });

            //Create click handler for variable Assignment Edit Text
            viewHolder.etForAssign.setOnDragListener(dropListener);
            viewHolder.etForAssign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter the assignment:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.etForAssign.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal2(enterText.getText().toString());
                            viewHolder.etForAssign.setText(enterText.getText());
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });
        }
        //IF IF_STATEMENT LAYOUT (TYPE 6)
        else if (type == 6) {
            //Create click handler for var name Edit Text
            viewHolder.etIfName.setOnDragListener(dropListener);
            viewHolder.etIfName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter new variable name:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.etIfName.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal1(enterText.getText().toString());
                            viewHolder.etIfName.setText(enterText.getText());
                            Toast.makeText(getContext(), "Saved this to block: "+getPosition(block), Toast.LENGTH_SHORT).show();
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });

            //Create click handler for variableAssignment Edit Text
            viewHolder.etIfAssign.setOnDragListener(dropListener);
            viewHolder.etIfAssign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_rename = new AlertDialog.Builder(getContext());
                    alert_rename.setMessage("Enter the assignment:");

                    //Create the RENAME dialog's Edit Text view
                    final EditText enterText = new EditText(getContext());
                    enterText.setPadding(50, 0, 50, 30);
                    enterText.setText(viewHolder.etIfAssign.getText());
                    alert_rename.setView(enterText);
                    enterText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);    //Remove auto-suggestions

                    //Make the Edit Text keyboard auto-appear
                    enterText.post(new Runnable() {
                        public void run() {
                            enterText.requestFocusFromTouch();                      //Request Edit Text focus
                            enterText.setSelection(enterText.getText().length());   //Set cursor to end of current text
                            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(enterText, 0);                   //Show soft input keyboard
                        }
                    });

                    alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //OK Button was clicked
                            block.setUserVal2(enterText.getText().toString());
                            viewHolder.etIfAssign.setText(enterText.getText());
                        }
                    });
                    alert_rename.setNegativeButton("Cancel", null);
                    alert_rename.show();
                }
            });

            //Create click handler for greaterThan
            viewHolder.greaterThan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal(">");
                    viewHolder.operatorView_if.newOperator(">");
                    viewHolder.operator_tooltip_if.setVisibility(View.GONE);
                }
            });

            //Create click handler for lessThan
            viewHolder.lessThan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("<");
                    viewHolder.operatorView_if.newOperator("<");
                    viewHolder.operator_tooltip_if.setVisibility(View.GONE);
                }
            });

            //Create click handler for greaterThanEqual
            viewHolder.greaterThanEqual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal(">=");
                    viewHolder.operatorView_if.newOperator(">=");
                    viewHolder.operator_tooltip_if.setVisibility(View.GONE);
                }
            });

            //Create click handler for lessThanEqual
            viewHolder.lessThanEqual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("<=");
                    viewHolder.operatorView_if.newOperator("<=");
                    viewHolder.operator_tooltip_if.setVisibility(View.GONE);
                }
            });

            //Create click handler for notEqual
            viewHolder.notEqual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("!=");
                    viewHolder.operatorView_if.newOperator("!=");
                    viewHolder.operator_tooltip_if.setVisibility(View.GONE);
                }
            });

            //Create click handler for isEqual
            viewHolder.isEqual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    block.setUserOperatorVal("==");
                    viewHolder.operatorView_if.newOperator("==");
                    viewHolder.operator_tooltip_if.setVisibility(View.GONE);
                }
            });


            //Create click handler for the operator custom view
            viewHolder.operatorView_if.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Show operator tooltip selections
                    viewHolder.operator_tooltip_if.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    /**
     * Create the dropListener (OnDragListener) for dragging blocks from header bar
     */
    public View.OnDragListener dropListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int dragEvent = event.getAction();

            switch (dragEvent) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    //RelativeLayout target = (RelativeLayout) v;
                    Button dragged = (Button) event.getLocalState();

                    //Do stuff when block dropped in main blockview
                    String btext = dragged.getText().toString();

                    Toast.makeText(getContext(), "Added new " + btext + " block.", Toast.LENGTH_SHORT).show();

                    Block.BlockType newBlockType = Block.BlockType.COMMENT;

                    switch (btext) {
                        case "FOR":
                            newBlockType = Block.BlockType.FOR_LOOP;
                            break;
                        case "WHILE":
                            newBlockType = Block.BlockType.WHILE_LOOP;
                            break;
                        case "COMMENT":
                            newBlockType = Block.BlockType.COMMENT;
                            break;
                        case "IF":
                            newBlockType = Block.BlockType.IF_STATEMENT;
                            break;
                        case "ELSE":
                            newBlockType = Block.BlockType.ELSE_STATEMENT;
                            break;
                        case "VAR":
                            newBlockType = Block.BlockType.VARIABLE;
                            break;
                        case "INPUT":
                            newBlockType = Block.BlockType.INPUT;
                            break;
                        case "OUTPUT":
                            newBlockType = Block.BlockType.OUTPUT;
                            break;
                        default:
                            newBlockType = Block.BlockType.COMMENT;
                            break;
                    }

                    //Get current blocklist
                    ArrayList<Block> newBlockList = DynamicListView.mBlockList;

                    if (newBlockType == Block.BlockType.WHILE_LOOP) {
                        newBlockList.add(new Block(Block.BlockType.WHILE_LOOP, getContext()));
                        newBlockList.add(new Block(Block.BlockType.WHILE_LOOP_END, getContext()));
                    }
                    else if (newBlockType == Block.BlockType.FOR_LOOP) {
                        newBlockList.add(new Block(Block.BlockType.FOR_LOOP, getContext()));
                        newBlockList.add(new Block(Block.BlockType.FOR_LOOP_END, getContext()));
                    }
                    else if (newBlockType == Block.BlockType.IF_STATEMENT) {
                        newBlockList.add(new Block(Block.BlockType.IF_STATEMENT, getContext()));
                        newBlockList.add(new Block(Block.BlockType.IF_STATEMENT_END, getContext()));
                    }
                    else if (newBlockType == Block.BlockType.ELSE_STATEMENT) {
                        newBlockList.add(new Block(Block.BlockType.ELSE_STATEMENT, getContext()));
                        newBlockList.add(new Block(Block.BlockType.ELSE_STATEMENT_END, getContext()));
                    }
                    else {
                        newBlockList.add(new Block(newBlockType, getContext()));
                    }

                    Sandbox.listView.setAdapter(null);
                    Sandbox.adapter = new StableArrayAdapter(getContext(), R.layout.block_customview_layout, newBlockList);

                    Sandbox.listView.setBlockList(newBlockList);
                    Sandbox.listView.setAdapter(Sandbox.adapter);

                    //Reset opacity of selected block to 1
                    dragged.setAlpha((float) 1);
                    break;
            }

            return true;
        }
    };
}