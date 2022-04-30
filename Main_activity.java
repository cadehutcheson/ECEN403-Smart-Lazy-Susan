package com.example.lazysusan;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    //declare buttons
    Button btnCW;
    Button btnCCW;
    Button pos1;
    Button pos2;
    Button pos3;
    Button pos4;
    Button s1;
    Button s2;
    Button s3;
    Button s4;
    Button storeToList;
    Button loadFood;
    Button cmdBtn;
    Button placeBtn;
    Button deleteBtn;

    //declare edittext fields
    EditText addressInput;
    EditText foodText;

    //variable definitions

    //String for raspberry pi ip address
    public static String wifiModuleIP = "";
    //Array list for storing menu items when loaded from text file
    public static ArrayList<String> foodList = new ArrayList<String>();

    //String array for storing names of food items on susan
    public static String susan[] = {"1","2","3","4"};
    //int array for giving values for lazy susan positions
    public static int table[] = {1,2,-1,-2};
    //int for holding position of requested food on lazy susan
    public static int foodPos = 0;
    //int for holding position that food is being requested to
    public static int reqPos = 0;

    //int for port number for comm. between app and raspberry pi
    public static int wifiModulePort = 0;
    //string for message that will be sent from app to pi, initially "0"
    public static String CMD = "0";

    //file name of text file that will store menu list
    private static final String FILE_NAME = "foodList.txt";

    public static String item;

    //TextInputLayout menu;
    AutoCompleteTextView foodDropdown;
    ArrayAdapter<String> adapterFoodItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Buttons definitions

        //Button for lazy susan control - 1 position(90 degrees) Clockwise
        btnCW = (Button) findViewById(R.id.btn1);
        //Button for lazy susan control - 1 position(90 degrees) Counter-Clockwise
        btnCCW = (Button) findViewById(R.id.btn2);
        //Button for storing a user inputted food item(string) to the menu list text file
        storeToList = (Button) findViewById(R.id.storeFoodBtn);
        //button for deleting the menu list from text file
        deleteBtn = (Button) findViewById(R.id.deleteBtn);
        //buttons for selecting request positions 1-4
        pos1 = (Button) findViewById(R.id.pos1);
        pos2 = (Button) findViewById(R.id.pos2);
        pos3 = (Button) findViewById(R.id.pos3);
        pos4 = (Button) findViewById(R.id.pos4);
        //buttons for selecting lazy susan positions 1-4
        s1 = (Button) findViewById(R.id.s1);
        s2 = (Button) findViewById(R.id.s2);
        s3 = (Button) findViewById(R.id.s3);
        s4 = (Button) findViewById(R.id.s4);
        //button for loading menu list from text file to display on screen
        loadFood = (Button) findViewById(R.id.loadFoodBtn);
        //button for executing lazy susan operation
        cmdBtn = (Button) findViewById(R.id.cmdBtn);
        //button for place selected menu item onto a position on the lazy susan
        placeBtn = (Button) findViewById(R.id.placeBtn);

        //input text field for entering raspberry pi ip address that is controlling lazy susan
        addressInput = (EditText) findViewById(R.id.ipAddr);
        //input text field for entering menu items
        foodText = (EditText) findViewById(R.id.foodText);

        String items[] = {"food1", "food2", "food3", "food4", "food5","food6", "food7", "food8", "food9", "food10"};
        foodDropdown = findViewById(R.id.foodDropdown); //**************************************************************************************************





        //text views for displaying currently selected food on the lazy susan and currently selected request position
        TextView SelectedFood = (TextView) findViewById(R.id.SelectedFood);
        TextView SelectedPos = (TextView) findViewById(R.id.SelectedPos);
        //text view for displaying menu list once loaded from text file
        TextView menuList = (TextView) findViewById(R.id.menuList);

        //setting default text for text views
        SelectedFood.setText("Select Food");
        SelectedPos.setText("Select Position");
        //menuList.setText("Load menu to view");


        FileInputStream fis = null;
        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while((text = br.readLine()) != null){
                foodList.add(text);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String initial = "";
        for(int i = 0; i < foodList.size(); i++){
            initial = initial + foodList.get(i) + "\n";
        }
        menuList.setText(initial);

        int size_init =0;
        if(foodList.size() >= 4){size_init=4;}
        else{size_init = foodList.size();}
        for(int i = 0; i < size_init; i++){
            susan[i] = foodList.get(i);
        }

        for(int i = 0; i < foodList.size(); i++){
            items[i] = foodList.get(i);
        }
        adapterFoodItems = new ArrayAdapter<String>(this,R.layout.list_foods, items);

        foodDropdown.setAdapter(adapterFoodItems);

        s1.setText(susan[0]);
        s2.setText(susan[1]);
        s3.setText(susan[2]);
        s4.setText(susan[3]);
        //***********************************************************************************************************************************************************
        foodDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id ) {
                item = parent.getItemAtPosition(position).toString();
            }
        });

        //listen for CW to be pressed
        btnCW.setOnClickListener(new View.OnClickListener(){
            @Override
            //operation to be done - send message "I" to raspberry pi for CW rotation
            public void onClick(View v) {
                //call getIP_Port() function so app knows who to send message to
                getIp_Port();
                //"I" indicates to the raspberry pi to rotate 1 position CW
                CMD = "I";
                Socket_AsyncTask cmd_CW = new Socket_AsyncTask();
                cmd_CW.execute();
                //calling function so app can update lazy susan positions
                rotCW();
            }

        });
        //listen for CCW to be pressed
        btnCCW.setOnClickListener(new View.OnClickListener(){
            @Override
            //operation to be done - send message "D" to raspberry pi for CCW rotation
            public void onClick(View v) {
                //call getIP_Port() function so app knows who to send message to
                getIp_Port();
                //"D" indicates to the raspberry pi to rotate 1 position CCW
                CMD = "D";
                Socket_AsyncTask cmd_CCW = new Socket_AsyncTask();
                cmd_CCW.execute();
                //calling function so app can update lazy susan positions
                rotCCW();
            }

        });
        //listen for pos1 to be pressed
        pos1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                reqPos = 1;
                //set selected position
                SelectedPos.setText("Position 1");
            }

        });
        //listen for pos2 to be pressed
        pos2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                reqPos = 2;
                //set selected position
                SelectedPos.setText("Position 2");
            }

        });
        //listen for pos3 to be pressed
        pos3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                reqPos = -1;
                //set selected position
                SelectedPos.setText("Position 3");
            }

        });
        //listen for pos4 to be pressed
        pos4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               reqPos = -2;
               //set selected position
                SelectedPos.setText("Position 4");
            }

        });
        //listen for s1 to be pressed
        s1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                foodPos = 1;
                //set selected food
                SelectedFood.setText(susan[0]);
            }

        });
        //listen for s2 to be pressed
        s2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                foodPos = 2;
                //set selected food
                SelectedFood.setText(susan[1]);
            }

        });
        //listen for s3 to be pressed
        s3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                foodPos = -1;
                //set selected food
                SelectedFood.setText(susan[2]);
            }

        });
        //listen for s4 to be pressed
        s4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                foodPos = -2;
                //set selected food
                SelectedFood.setText(susan[3]);
            }

        });
        //listen for storeToList to be pressed
        storeToList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FileOutputStream fos = null;

                String foodInput = foodText.getText().toString();
                if(foodInput == "" || foodInput == " " || foodInput == null || foodInput == "\n"){foodInput = "blah";}

                foodList.add(foodInput);
                foodText.setText("");
                try {
                    fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                    for(int i = 0; i < foodList.size(); i ++){
                        String text = foodList.get(i) + "\n";
                        fos.write(text.getBytes());
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    if(fos!=null){
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

        });
        //listen for loadFood to be pressed
        loadFood.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FileInputStream fis = null;
                try {
                    fis = openFileInput(FILE_NAME);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text;

                    while((text = br.readLine()) != null){
                        sb.append(text).append("\n");
                    }
                    menuList.setText(sb.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    if(fis != null){
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

        });
        //listen for deleteBtn to be pressed
        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //erase text file
                FileOutputStream fos = null;

                try {
                    fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                    String nullStr = "";
                    fos.write(nullStr.getBytes());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    if(fos!=null){
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                foodList.clear();
                menuList.setText("");
            }


        });
        //listen for cmdBtn to be pressed
        cmdBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            //this function will first calculate the required direction and amount of rotation
            //When the correct command is calculated, it will assign the command string the correct value, then send it to the raspberry pi
            //for a 4-position table, only 3 operations are needed for any item to go to any positions- 90 degrees CW, 180 CW, and 90 CCW
            public void onClick(View v) {

                //the values of requested lazy susan position and requested table Position are added.
                //These values are {1,2,-1,-2} for positions {1,2,3,4} respectively
                //Sum is taken from these values so that a 180 rotation can easily be calculated
                //The sum1 value for a 180 rotation will always equal 0 for any values selected
                int sum1 = foodPos + reqPos;
                //if the requested food position and requested table position are the same, no movement is necessary - food is already there
                if(foodPos == reqPos){
                    return;
                }
                //sum zero means lazy susan needs to rotate 180 degrees
                else if(sum1 == 0){
                    //set command string to 180
                    CMD = "180";
                    //call CW rotation function twice for 2*90 degrees rotation
                    rotCW();
                    rotCW();
                    //update the current position of the food items
                    foodPos = reqPos;
                }
                /*the remaining conditional statements calculate which direction to rotate
                the lazy susan 1 position based on foodPos value and sum1 value */


                else if(foodPos ==1){
                    //if food is at position 1 and requested to table position 4, sum1 will be -1
                    if (sum1 == -1) {
                        CMD = "CounterCW";
                        rotCCW();
                    }
                    //if food is at position 1 and requested to table position 2, sum1 will be 3
                    else if(sum1 == 3){
                        CMD = "Clockwise";
                        rotCW();
                    }
                    foodPos = reqPos;
                }
                else if(foodPos == 2){
                    //if food is at position 2 and requested to table position 1, sum1 will be 3
                    if (sum1 == 3) {
                        CMD = "CounterCW";
                        rotCCW();
                    }
                    //if food is at position 2 and requested to table position 3, sum1 will be 1
                    else if(sum1 == 1){
                        CMD = "Clockwise";
                        rotCW();
                    }
                    foodPos = reqPos;
                }
                else if(foodPos == -1){
                    //if food is at position 3 and requested to table position 2, sum1 will be 1
                    if (sum1 == 1) {
                        CMD = "CounterCW";
                        rotCCW();
                    }
                    //if food is at position 3 and requested to table position 4, sum1 will be -3
                    else if(sum1 == -3){
                        CMD = "Clockwise";
                        rotCW();
                    }
                    foodPos = reqPos;
                }
                else if(foodPos == -2){
                    //if food is at position 4 and requested to table position 3, sum1 will be -3
                    if (sum1 == -3) {
                        CMD = "CounterCW";
                        rotCCW();
                    }
                    //if food is at position 4 and requested to table position 1, sum1 will be -1
                    else if(sum1 == -1){
                        CMD = "Clockwise";
                        rotCW();
                    }
                    foodPos = reqPos;
                }
                //get raspberry pi ip and inputted port number
                getIp_Port();
                //send correct cmd string to raspberry for executing correct lazy susan operation
                Socket_AsyncTask cmd_go_stepper = new Socket_AsyncTask();
                cmd_go_stepper.execute();
            }

        });
        //listen for placeBtn to be pressed
        placeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //int index = selected position
                //String food = selected menu item
                //susan[index] = food
                //set text
                if(foodPos == 1){
                    susan[0] = item;
                    s1.setText(susan[0]);
                }
                else if(foodPos == 2){
                    susan[1] = item;
                    s2.setText(susan[1]);
                }
                else if(foodPos == -1){//3
                    susan[2] = item;
                    s3.setText(susan[2]);
                }
                else if(foodPos == -2){//4
                    susan[3] = item;
                    s4.setText(susan[3]);
                }
            }

        });
    }
    //function for getting user inputted IP address and port number for raspberrypi/microcontroller communication
    public void getIp_Port(){
        //assign user inputted text to string
        String Ip_Port = addressInput.getText().toString();
        //verify ip address in terminal
        Log.d("Test1","IP String: "+ Ip_Port);
        //split string at ":" to separate ip address from port number, store each into temporary string array
        String temp[] = Ip_Port.split(":");
        wifiModuleIP = temp[0];
        //assgign port number to int value, convert from string to integer
        wifiModulePort = Integer.valueOf(temp[1]);
        //verify split operation/correct ip and port
        Log.d("Test1","IP:"+ wifiModuleIP);
        Log.d("Test1","PORT:"+ wifiModulePort);
    }

    //function to be called after rotating lazy susan 1 position Clockwise - shifts string names to update positions of foods on the app display
    public void rotCW(){
        //temp string for holding last array value
        String temp = susan[3];
        //for loop that shifts contents of array up 1 index
        for(int i = 3; i > 0; i--){
            susan[i] = susan[i-1];
        }
        //first item becomes last item - food at position 4 is now at position 1
        susan[0] = temp;

        //update texts to display correct food item positions on lazy susan
        s1.setText(susan[0]);
        s2.setText(susan[1]);
        s3.setText(susan[2]);
        s4.setText(susan[3]);
    }
    //function to be called after rotating lazy susan 1 position Counter-Clockwise
    public void rotCCW(){
        //temp string for holding first array value
        String temp = susan[0];
        //for loop that shifts contents of array down 1 index
        for(int i = 0; i < 3; i++){
            susan[i] = susan[i+1];
        }
        //last item becomes first item - food at position 1 is now at position 4
        susan[3] = temp;

        //update texts to display correct food item positions on lazy susan
        s1.setText(susan[0]);
        s2.setText(susan[1]);
        s3.setText(susan[2]);
        s4.setText(susan[3]);
    }

    //class for data sending operations, used when user presses CW, CCW, or cmdBtn buttons
    public static class Socket_AsyncTask extends AsyncTask<Void,Void,Void>{
        Socket socket;

        @Override
        protected Void doInBackground(Void... params){
            try{
                //get ip from user input
                InetAddress inetAddress = InetAddress.getByName(MainActivity.wifiModuleIP);
                //define socket
                socket = new java.net.Socket(inetAddress, MainActivity.wifiModulePort);
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.writeBytes(CMD);
                dataOut.close();
                socket.close();
            }
            catch(UnknownHostException e){e.printStackTrace();} catch(IOException e){e.printStackTrace();}
            return null;
        }

    }
}