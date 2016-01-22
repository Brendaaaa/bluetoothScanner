package com.scan.out;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.scan.bleexample.R;

public class MapActivity extends Activity {

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
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"brenda.o.ramires@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "scan");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, Utils.readFile(Utils.DEVICES_MEASURES, getBaseContext()) + "\n----\n" + Utils.readFile(Utils.DEVICES_NAMES, getBaseContext()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
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

}
