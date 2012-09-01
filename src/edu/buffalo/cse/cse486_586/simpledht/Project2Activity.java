package edu.buffalo.cse.cse486_586.simpledht;

import android.R.array;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Formatter;

import com.project1.R;

public class Project2Activity extends Activity {

	static TextView hash;
	Button click;
	Button dump;
	EditText msg;
	Cursor new_cursor;
	String tempval="";

	static int seqno = 0;

	Context myContext;
	static int flag1=0;
	static String returnC="";
	Handler handlerMain = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		hash = (TextView) findViewById(R.id.chatMsg);
		hash.setMovementMethod(new ScrollingMovementMethod());

		click = (Button) findViewById(R.id.testButton);
		dump = (Button) findViewById(R.id.dumpButton);
		msg = (EditText) findViewById(R.id.msgtext);

		click.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				Log.d("Query now","lets c");
				Thread send_data = new Thread(new InsertFunction(), "client thread");
			send_data.start();
				
			}
		});
		
		dump.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
					// TODO Auto-generated method stub
					String[] projection = { 
							Databse_for_CP.COLUMN_VAL };
					Cursor new_cursor = Content_Provider.query_dump(
							Content_Provider.cp_uri,
							projection,null , null, null);

					String return_cursor = "";
					if (new_cursor != null && new_cursor.getCount() != 0) {
						while (new_cursor.moveToNext()) {

							return_cursor += new_cursor.getString(new_cursor
									.getColumnIndexOrThrow(Databse_for_CP.COLUMN_VAL));
							return_cursor += "\n";
						}
						hash.setText("Dump:" + "\n");

						hash.append(return_cursor.toString());
						Log.d("cursor is", return_cursor);
					} else {
						hash.setText("No data Present");
						Log.d("data not found", "");
					}
				
				
			}
		});
		
	}

	Runnable clientHandler = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			hash.append(tempval.toString()+"\n");
		}
	};


	
	static public String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}



	public class InsertFunction implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			
			for (int j = 0; j < 10; j++) {
			ContentValues new_value = new ContentValues();
			new_value.put(Databse_for_CP.COLUMN_KEY, seqno);
			new_value.put(Databse_for_CP.COLUMN_VAL, "Test"+seqno);
			Uri uri = getContentResolver().insert(Content_Provider.cp_uri,
					new_value);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			seqno++;
			}
			seqno=0;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int j = 0; j < 10; j++) {
			String[] projection = { Databse_for_CP.COLUMN_VAL };
			String selection = Databse_for_CP.COLUMN_KEY+"=" + "'" +Integer.toString(seqno)+"'";
			new_cursor = getContentResolver().query(
					Content_Provider.cp_uri, projection, selection,
					null, null);
			tempval="";
			if (new_cursor != null && new_cursor.getCount() != 0) {
				while (new_cursor.moveToNext()) {
					
					tempval= new_cursor.getString(new_cursor
							.getColumnIndexOrThrow(Databse_for_CP.COLUMN_VAL));
					
				}
			}
			handlerMain.post(clientHandler);
			Log.d("To print",":"+new_cursor.getCount());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			seqno++;
			}
			
			seqno=0;
			
			
			

		}

	}

}