package com.scan.out;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.scan.bleexample.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends Activity {

    private final static String api_key = "BQD3AQSVPNJLE1Y3";
    private ImageView mapImage;
    private Rect[] regions;
    private Button sendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        regions = new Rect[6];
        for (int j = 0; j < 6; j++) {
            regions[j] = new Rect();
        }
        regions[0].set(0, 0, display.getWidth() / 2, display.getHeight() / 3);
        regions[1].set(display.getWidth() / 2, 0, display.getWidth(), display.getHeight() / 3);
        regions[2].set(0, display.getHeight() / 3, display.getWidth() / 2, display.getHeight() * 2 / 3);
        regions[3].set(display.getWidth() / 2, display.getHeight() / 3, display.getWidth(), display.getHeight() * 2 / 3);
        regions[4].set(0, display.getHeight() * 2 / 3, display.getWidth() / 2, display.getHeight());
        regions[5].set(display.getWidth() / 2, display.getHeight() * 2 / 3, display.getWidth(), display.getHeight());

        sendData = (Button) findViewById(R.id.button2);
        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_SENDTO);
//                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
//                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"brenda.o.ramires@gmail.com"});
//                intent.putExtra(Intent.EXTRA_SUBJECT, "scan");
//                intent.putExtra(android.content.Intent.EXTRA_TEXT, Utils.readFile(Utils.DEVICES_MEASURES, getBaseContext()) + "\n----\n" + Utils.readFile(Utils.DEVICES_NAMES, getBaseContext()));
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intent);
//                }
                sendPostRequest(Utils.readFile(Utils.DEVICES_MEASURES, getBaseContext()) + "\n----\n" + Utils.readFile(Utils.DEVICES_NAMES, getBaseContext()));
            }
        });

        mapImage = (ImageView) findViewById(R.id.image_map);
        mapImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int region = 0;
                for (int i = 0; i < 6; i++) {
                    if (regions[i].contains((int) event.getX(), (int) event.getY())) {
                        region = i;
                        System.out.println("To na regiao" + (region));
                    }
                }

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("regionNumber", region);
                startActivity(intent);

                return false;
            }
        });

    }

    private void sendPostRequest(String content) {

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... content) {

                String csv = content[0];
                csv = "created_at, field1\n2016-01-21 19:52:22 UTC,16";

                HttpClient httpClient = new DefaultHttpClient();

                // In a POST request, we don't pass the values in the URL.
                //Therefore we use only the web page URL as the parameter of the HttpPost argument
                HttpPost httpPost = new HttpPost("http://10.35.99.86:3000/upload");

                // Because we are not passing values over the URL, we should have a mechanism to pass the values that can be
                //uniquely separate by the other end.
                //To achieve that we use BasicNameValuePair
                // We add the content that we want to pass with the POST request to as name-value pairs
                //Now we put those sending details to an ArrayList with type safe of NameValuePair
                List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
                nameValuePairList.add(new BasicNameValuePair("api_key", api_key));
                nameValuePairList.add(new BasicNameValuePair("csv", csv));

                try {
                    // UrlEncodedFormEntity is an entity composed of a list of url-encoded pairs.
                    //This is typically useful while sending an HTTP POST request.
                    UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);

                    // setEntity() hands the entity (here it is urlEncodedFormEntity) to the request.
                    httpPost.setEntity(urlEncodedFormEntity);

                    try {
                        HttpResponse httpResponse = httpClient.execute(httpPost);

                        return EntityUtils.toString(httpResponse.getEntity());

                    } catch (Exception e) {
                        System.out.println("Exception - HttpResponse :" + e);
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    System.out.println("Exception - UrlEncodedFormEntity argument :" + e);
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

               System.out.println("HTTP response = " + result);
                if(result.equals("OK")){
                    Toast.makeText(getApplicationContext(), "csv was sent", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(content);
    }


}
