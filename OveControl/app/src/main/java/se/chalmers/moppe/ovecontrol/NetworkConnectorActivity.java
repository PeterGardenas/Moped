package se.chalmers.moppe.ovecontrol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Erik Magnusson
 * Most code reuused from original WirelessIno app, some new additions
 */

public class NetworkConnectorActivity extends Activity {

    private EditText ed_host = null;
    private EditText ed_port = null;
    private final int javaPort = 9090;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.socket_connection_build);
        ed_host = (EditText) findViewById(R.id.ed_host);
        ed_port = (EditText) findViewById(R.id.ed_port);
        Button btn_connect = (Button) findViewById(R.id.btn_connect);
        Button btnClose = (Button) findViewById(R.id.btn_close);

		/* Fetch earlier defined host ip and port numbers and write them as default
		 * (to avoid retyping this, typically standard, info) */
        SharedPreferences mSharedPreferences = getSharedPreferences("list",MODE_PRIVATE);
        String oldHost = mSharedPreferences.getString("host", null);
        if (oldHost != null){
            ed_host.setText(oldHost);
        }
        String oldPort = mSharedPreferences.getString("port",null);
        if (oldPort != null){
            ed_port.setText(oldPort);
        }

		/* Setup the "connect"-button. On click, new host ip and port numbers should
		 * be stored and a socket connection created (this is done as a background task).
		 */
        btn_connect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String str_host = ed_host.getText().toString().trim();
                String str_port = ed_port.getText().toString().trim();

                SharedPreferences mSharedPreferences = getSharedPreferences("list", MODE_PRIVATE);
                mSharedPreferences.edit().putString("host",str_host).apply();
                mSharedPreferences.edit().putString("port",str_port).apply();

                //Connect to java server
                PostRequester.connect(str_host, "" + javaPort);

				/* Create socket connection in a background task */
                new SocketConnector(NetworkConnectorActivity.this).execute(str_host, str_port);
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
}