package edu.buffalo.cse.cse486_586.simpledht;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Content_Provider extends ContentProvider {
	static Context context;
	String base = "5554";
	static String portStr;
	static String predecessor;
	static String successor;
	static String least_pred;
	static String high_succssr;
	static String ipAdd = "10.0.2.2";
	static String myhash;
	static int myportno;
	static int seqno = 0;
	static String toSend;
	TelephonyManager tel;
	static String returnC="";
	
	public static Databse_for_CP cp_database;
	ContentValues curr_values;
	int portno;
	public static final Uri cp_uri = Uri
			.parse("content://edu.buffalo.cse.cse486_586.simpledht.provider/CP_Database");

	public Content_Provider() {
		cp_database = new Databse_for_CP(getContext());

		// context = this.getContext();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub

//		if (Project2Activity.flag1 == 1) {
			curr_values = values;
			Thread send_data = new Thread(new Data_sent(), "client thread");
			send_data.start();
			return uri;
//		} else {
//			SQLiteDatabase db = cp_database.getWritableDatabase();
//			long r_id = db.insert(Databse_for_CP.TABLE_NAME, null, values);
//			if (r_id > 0) {
//				Uri new_uri = ContentUris.withAppendedId(cp_uri, r_id);
//				getContext().getContentResolver().notifyChange(new_uri, null);
//				return new_uri;
//			}
//		}
			
		
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		cp_database = new Databse_for_CP(getContext());
		new ServerThread(this.getContext()).execute("hi");
//		 tel = (TelephonyManager) this
//		.getSystemService(Context.TELEPHONY_SERVICE);
		
		tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		portStr = tel.getLine1Number().substring(
				tel.getLine1Number().length() - 4);
		//portStr= tel.getLine1Number();
		try {
			myhash = genHash(portStr);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myportno = Integer.parseInt(portStr) * 2;
		// new ServerThread(this.getApplicationContext()).execute("hi");
		if (base.equals(portStr)) {
			int temp_base = Integer.parseInt(base) * 2;
			successor = new Integer(temp_base).toString();
			predecessor = new Integer(temp_base).toString();
			least_pred = predecessor;
			high_succssr = successor;
		} else {
			Thread clientT = new Thread(new ClientThread(), "client thread");
			clientT.start();
		}
		initial_cursor();

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		SQLiteDatabase db = cp_database.getReadableDatabase();
		query.setTables(Databse_for_CP.TABLE_NAME);
		query.setProjectionMap(hash_map);
		Cursor qc = query.query(db, projection, selection, selectionArgs, null,
				null, null);
		qc.setNotificationUri(getContext().getContentResolver(), uri);
		if(qc.getCount()>0){
		return qc;
		}
		else{

			Socket newSocket;
			try {
				String msg = "Query:"+selection+":"+portStr;
				newSocket = new Socket(ipAdd,
						Integer.parseInt(successor));
				PrintWriter tonext = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(newSocket.getOutputStream())),
						true);
				tonext.println(msg);
				newSocket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
				String [] columns = new String[1];
				columns[0]=Databse_for_CP.COLUMN_VAL;
				MatrixCursor matrix = new MatrixCursor(columns);
				ServerThread.flagQ = true;
				while(ServerThread.flagQ)
				{
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				matrix.addRow(new Object[]{ServerThread.value});
				ServerThread.flagQ=true;
				return matrix;
			
			
		}
		
	}

	public static Cursor query_dump(Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		SQLiteDatabase db = cp_database.getReadableDatabase();
		query.setTables(Databse_for_CP.TABLE_NAME);
		query.setProjectionMap(hash_map);
		Cursor qc = query.query(db, projection, selection, selectionArgs, null,
				null, null);
		
		// qc.setNotificationUri(uri);
		return qc;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static HashMap<String, String> hash_map;
	static {
		hash_map = new HashMap<String, String>();
		hash_map.put(Databse_for_CP.COLUMN_KEY, Databse_for_CP.COLUMN_KEY);
		hash_map.put(Databse_for_CP.COLUMN_VAL, Databse_for_CP.COLUMN_VAL);
	}
	
	static public String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	public class ClientThread implements Runnable {
		String handshake;
		String tempno;

		public void run() {
			// TODO Auto-generated method stub
			try {
				handshake = "Initial" + ":" + portStr;
				portno = Integer.parseInt(base) * 2;
				Log.d("succesor and predecessor are :", predecessor + " , "
						+ successor);
				Socket cliSocket = new Socket(ipAdd, portno);
				PrintWriter toServer = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(cliSocket.getOutputStream())),
						true);
				toServer.println(handshake);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public static void initial_cursor(){
		String[] projection = { 
				Databse_for_CP.COLUMN_VAL };
		Cursor new_cursor = Content_Provider.query_dump(
				Content_Provider.cp_uri,
				projection,null , null, null);

		if (new_cursor != null && new_cursor.getCount() != 0) {
			while (new_cursor.moveToNext()) {

				returnC += new_cursor.getString(new_cursor
						.getColumnIndexOrThrow(Databse_for_CP.COLUMN_VAL));
				returnC += "\n";
			}
			//hash.setText(returnC.toString());
			//Log.d("cursor is", returnC);
		} else {
			Log.d("data not found", "");
		}
	}
	
	public class Data_sent implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			try {

				//
				toSend = "Message:"
						+ curr_values.get(Databse_for_CP.COLUMN_KEY) + ":"
						+ curr_values.get(Databse_for_CP.COLUMN_VAL);

				Log.d("message is ", ": " + toSend);
			

				Socket newSocket = new Socket(ipAdd,
						myportno);
				PrintWriter tonext = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(newSocket.getOutputStream())),
						true);
				tonext.println(toSend);
				newSocket.close();
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
