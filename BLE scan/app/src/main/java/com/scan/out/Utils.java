package com.scan.out;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by brendaramires on 10/12/15.
 */
public class Utils {

    public static final String DEVICES_NAMES = "devicesNames.csv";
    public static final String DEVICES_MEASURES = "devicesMeasures.csv";


    public static void writeToFile(String newContent, String fileName, Context context) {

        try {
            PrintWriter writer = new PrintWriter(context.openFileOutput(fileName, context.MODE_APPEND));

            if (writer != null) {
                writer.println(newContent);
                writer.println("\r\n");
                writer.close();

//                //display file saved message
//                Toast.makeText(getBaseContext(), "File saved successfully!",
//                        Toast.LENGTH_SHORT).show();

            } else {
                System.out.println("Não abriu o arquivo");
            }

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public static String readFile(String fileName, Context context) {
        String text = "";

        try {

            InputStream inputStream = context.openFileInput(fileName);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                inputStream.close();
                text = stringBuilder.toString();

                System.out.println("content of + " + fileName + "\n" + text);

                return text;

            } else {
                System.out.println("NAO TEM ARQUIVO");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            Log.e("Main activity", "File not found: " + e.toString());
        } catch (IOException e) {
            System.out.println("Can not read file");
            Log.e("Main activity", "Can not read file: " + e.toString());
        }

//		Toast.makeText(getBaseContext(), "File read", Toast.LENGTH_SHORT).show();
        return null;
    }

    public static void initInfoFromFile(Context context, Map devicesNames) {

        String contentFile = readFile(DEVICES_NAMES, context);
        System.out.println("initfromfile");
        if (contentFile != null && contentFile.length() > 0) {
            String[] lines = contentFile.split(";");

            for (String line : lines) {
                System.out.println("LINE = " + line);
                String[] info = line.split(",");
                System.out.println(info[0] + "    " + info[1]);
                devicesNames.put(info[0],info[1]);
            }
        }
    }


}
