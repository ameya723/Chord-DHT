package edu.buffalo.cse.cse486_586.simpledht;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class ServerThread extends AsyncTask<String, String, String> {

	int portServer;
	ServerSocket servSocket;
	Socket servAccepted;
	Context context;
	String recv_req[];
	String temp;
	String data;
	String least_hash;
	String high_hash;
	List messageList;
	List seqList;
	String selection;
	static String pno;
	static Boolean flagQ;
	static Cursor tempCursor;
	ContentValues values;
	int i;
	static String value;
	public ServerThread(Context newContext) {
		// TODO Auto-generated constructor stub
		try {
			portServer = 10000;
			servSocket = new ServerSocket(portServer);
			seqList = new ArrayList<String>();
			messageList = new ArrayList<String>();
			i = 0;
			flagQ=false;
			pno=Content_Provider.portStr;
			this.context = newContext;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		//Project2Activity.hash.append(messageList.get(i).toString() + "\n");
		int flag = queryfromCP();
		if (flag == -1) {
			insertintoCP(context);

		} else {
			Log.d("Updated", ":" + messageList.get(i).toString());
		}
		
	}

	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		if (servSocket != null) {
			while (true) {
				try {
					servAccepted = servSocket.accept();
					BufferedReader fromClient = new BufferedReader(
							new InputStreamReader(servAccepted.getInputStream()));
					temp = fromClient.readLine();
					recv_req = temp.split(":");
					Log.d("recv msg is :", temp);

					if (recv_req[0].equals("Initial")) {
						String recvdHash = Content_Provider
								.genHash(recv_req[1]);
						int recv_portno = Integer.parseInt(recv_req[1]) * 2;
						least_hash = Content_Provider
								.genHash(Content_Provider.least_pred);
						high_hash = Content_Provider
								.genHash(Content_Provider.high_succssr);

						if (Content_Provider.myhash.compareTo(recvdHash) < 0) {
							String temphash = Content_Provider
									.genHash(Content_Provider.successor);
							if (temphash.compareTo(recvdHash) > 0) {
								if (recvdHash.compareTo(least_hash) < 0) {
									Content_Provider.least_pred = new Integer(
											recv_portno).toString();

								} else if (recvdHash.compareTo(high_hash) > 0) {
									Content_Provider.high_succssr = new Integer(
											recv_portno).toString();

								}
								data = "Join:" + "P:"
										+ Content_Provider.myportno + ":S:"
										+ Content_Provider.successor + ":"
										+ Content_Provider.least_pred + ":"
										+ Content_Provider.high_succssr;
								send_data(recv_portno, data);
								data = "Join:" + "P:" + recv_portno + ":S:"
										+ "NA" + ":"
										+ Content_Provider.least_pred + ":"
										+ Content_Provider.high_succssr;
								send_data(
										Integer.parseInt(Content_Provider.successor),
										data);
								Content_Provider.successor = new Integer(
										recv_portno).toString();
								Content_Provider.successor = new Integer(
										recv_portno).toString();
								Log.d("succesor and predecessor are :",
										Content_Provider.successor + " , "
												+ Content_Provider.predecessor);
							} else if (temphash.compareTo(recvdHash) < 0) {
								// Log.d("inside first loop ", temp);
								if (Content_Provider.successor
										.equals(Content_Provider.least_pred)) {
									if (recvdHash.compareTo(least_hash) < 0) {
										Content_Provider.least_pred = new Integer(
												recv_portno).toString();

									} else if (recvdHash.compareTo(high_hash) > 0) {
										Content_Provider.high_succssr = new Integer(
												recv_portno).toString();

									}
									Log.d("send " + recv_portno, " p= "
											+ Content_Provider.myportno + ", "
											+ "s= "
											+ Content_Provider.least_pred);
									Log.d("send " + Content_Provider.least_pred,
											" p= " + recv_portno + ", " + "s= "
													+ "NA");
									data = "Join:" + "P:"
											+ Content_Provider.myportno + ":S:"
											+ Content_Provider.least_pred + ":"
											+ Content_Provider.least_pred + ":"
											+ Content_Provider.high_succssr;
									send_data(recv_portno, data);
									data = "Join:" + "P:" + recv_portno + ":S:"
											+ "NA" + ":"
											+ Content_Provider.least_pred + ":"
											+ Content_Provider.high_succssr;
									send_data(
											Integer.parseInt(Content_Provider.least_pred),
											data);
									Content_Provider.successor = new Integer(
											recv_portno).toString();

								} else {
									if (recvdHash.compareTo(least_hash) < 0) {
										Content_Provider.least_pred = new Integer(
												recv_portno).toString();

									} else if (recvdHash.compareTo(high_hash) > 0) {
										Content_Provider.high_succssr = new Integer(
												recv_portno).toString();

									}
									Log.d("send : "
											+ Content_Provider.successor, " : "
											+ temp);
									send_data(
											Integer.parseInt(Content_Provider.successor),
											temp);

								}
							}

						} else if (Content_Provider.myhash.compareTo(recvdHash) > 0) {

							String temphash = Content_Provider
									.genHash(Content_Provider.predecessor);
							if (temphash.compareTo(recvdHash) < 0) {
								if (recvdHash.compareTo(least_hash) < 0) {
									Content_Provider.least_pred = new Integer(
											recv_portno).toString();

								} else if (recvdHash.compareTo(high_hash) > 0) {
									Content_Provider.high_succssr = new Integer(
											recv_portno).toString();

								}
								data = "Join:" + "P:"
										+ Content_Provider.predecessor + ":S:"
										+ Content_Provider.myportno + ":"
										+ Content_Provider.least_pred + ":"
										+ Content_Provider.high_succssr;
								send_data(recv_portno, data);
								data = "Join:" + "P:" + "NA" + ":S:"
										+ recv_portno + ":"
										+ Content_Provider.least_pred + ":"
										+ Content_Provider.high_succssr;
								send_data(
										Integer.parseInt(Content_Provider.predecessor),
										data);
								Content_Provider.predecessor = new Integer(
										recv_portno).toString();
								Log.d("succesor and predecessor are :",
										Content_Provider.successor + " , "
												+ Content_Provider.predecessor);

							} else if (temphash.compareTo(recvdHash) > 0) {

								Log.d("inside first else part loop ", temp);
								if (Content_Provider.predecessor
										.equals(Content_Provider.high_succssr)) {
									if (recvdHash.compareTo(least_hash) < 0) {
										Content_Provider.least_pred = new Integer(
												recv_portno).toString();

									} else if (recvdHash.compareTo(high_hash) > 0) {
										Content_Provider.high_succssr = new Integer(
												recv_portno).toString();

									}
									Log.d("send " + recv_portno, " p= "
											+ Content_Provider.myportno + ", "
											+ "s= "
											+ Content_Provider.least_pred);
									Log.d("send " + Content_Provider.least_pred,
											" p= " + recv_portno + ", " + "s= "
													+ "NA");
									data = "Join:" + "P:"
											+ Content_Provider.high_succssr
											+ ":S:" + Content_Provider.myportno
											+ ":" + Content_Provider.least_pred
											+ ":"
											+ Content_Provider.high_succssr;
									send_data(recv_portno, data);
									data = "Join:" + "P:" + "NA" + ":S:"
											+ recv_portno + ":"
											+ Content_Provider.least_pred + ":"
											+ Content_Provider.high_succssr;
									send_data(
											Integer.parseInt(Content_Provider.high_succssr),
											data);
									Content_Provider.predecessor = new Integer(
											recv_portno).toString();

								}

								else {
									if (recvdHash.compareTo(least_hash) < 0) {
										Content_Provider.least_pred = new Integer(
												recv_portno).toString();

									} else if (recvdHash.compareTo(high_hash) > 0) {
										Content_Provider.high_succssr = new Integer(
												recv_portno).toString();

									}
									Log.d("send : "
											+ Content_Provider.predecessor,
											" : " + temp);
									send_data(
											Integer.parseInt(Content_Provider.predecessor),
											temp);

								}
							}
						}

					} else if (recv_req[0].equals("Join")) {

						if (!recv_req[2].equals("NA")) {
							Content_Provider.predecessor = recv_req[2];
						}
						if (!recv_req[4].equals("NA")) {
							Content_Provider.successor = recv_req[4];
						}
						Content_Provider.least_pred = recv_req[5];
						Content_Provider.high_succssr = recv_req[6];

						Log.d("succesor and predecessor are :",
								Content_Provider.successor + " , "
										+ Content_Provider.predecessor);
					}

					else if (recv_req[0].equals("Message")) {

						seqList.add(recv_req[1]);
						messageList.add(recv_req[2]);
						i = seqList.indexOf(recv_req[1]);
						Log.d("seq no and message are : ", seqList.get(i) + ","
								+ messageList.get(i));
						String tempseqno = Content_Provider.genHash(seqList
								.get(i).toString());
						int tempSuccessor = Integer
								.parseInt(Content_Provider.successor) / 2;
						int temppredecessor = Integer
								.parseInt(Content_Provider.predecessor) / 2;
						if (tempseqno.compareTo(Content_Provider.myhash) > 0) {
							Log.d("first main if", ": " + temp);

							if (Content_Provider.least_pred.equals(new Integer(
									Content_Provider.myportno).toString())) {
								Log.d("inside if", ": " + temp);

								if (tempseqno
										.compareTo(Content_Provider.genHash(new Integer(
												Integer.parseInt(Content_Provider.high_succssr) / 2)
												.toString())) > 0
										&& Content_Provider.predecessor
												.equals(Content_Provider.high_succssr)) {
									Log.d("second if", ": " + temp);

									publishProgress();
								} else {
									send_data(
											Integer.parseInt(Content_Provider.successor),
											temp);

								}
							} else {
								Log.d("first else", ": " + temp);

								send_data(
										Integer.parseInt(Content_Provider.successor),
										temp);
							}

						} else if (tempseqno.compareTo(Content_Provider.myhash) < 0) {
							Log.d("first main else", ": " + temp);

							if (tempseqno.compareTo(Content_Provider
									.genHash(new Integer(temppredecessor)
											.toString())) > 0) {
								Log.d("first inside if else", ": " + temp);

								publishProgress();
							} else {
								if (Content_Provider.least_pred
										.equals(new Integer(
												Content_Provider.myportno)
												.toString())) {
									publishProgress();
								} else {
									Log.d("first else", ": " + temp);

									send_data(
											Integer.parseInt(Content_Provider.predecessor),
											temp);
								}
							}
						}
					}
					else if(recv_req[0].equals("Query")){
					selection = recv_req[1];
					pno = recv_req[2];
					SQLiteQueryBuilder query = new SQLiteQueryBuilder();
					SQLiteDatabase db = Content_Provider.cp_database.getReadableDatabase();
					query.setTables(Databse_for_CP.TABLE_NAME);
					query.setProjectionMap(Content_Provider.hash_map);
					Cursor qc = query.query(db,new String[] {
							Databse_for_CP.COLUMN_VAL
					} , selection, null, null,
							null, null);
					//qc.setNotificationUri(getApplicationContext().getContentResolver(), uri);
					String output="";
					if (qc != null && qc.getCount() != 0) {
						while (qc.moveToNext()) {
							
							output= qc.getString(qc
									.getColumnIndexOrThrow(Databse_for_CP.COLUMN_VAL));
							
						}
					}
					Log.d("count is  :", "" + qc.getCount());

					if(qc.getCount()>0){
						String tosend = "Result:" + output; 
						send_data(Integer.parseInt(pno)*2,tosend);
					}
					else{
						send_data(Integer.parseInt(Content_Provider.successor), temp);
					}
					}
					else if(recv_req[0].equals("Result")){
						value = recv_req[1];
						flagQ=false;
					}
					Log.d("succesor and predecessor are :",
							Content_Provider.successor + " , "
									+ Content_Provider.predecessor);
					Log.d("high succesor and lease predecessor are :",
							Content_Provider.high_succssr + " , "
									+ Content_Provider.least_pred);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return null;
	}

	public void send_data(int curr_portNo, String data_send) {
		// TODO Auto-generated method stub
		try {
			Socket baseSocket = new Socket(Content_Provider.ipAdd, curr_portNo);
			PrintWriter toPeer = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(baseSocket.getOutputStream())), true);
			toPeer.println(data_send);
			baseSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertintoCP(Context new_context) {
		// TODO Auto-generated method stub
		ContentValues new_value = new ContentValues();
		Log.d("values are:",seqList.get(i).toString() + ":" );
		new_value.put(Databse_for_CP.COLUMN_KEY, seqList.get(i).toString());
		new_value.put(Databse_for_CP.COLUMN_VAL, messageList.get(i).toString());
		new_context.getApplicationContext();
		Uri uri = context.getContentResolver().insert(Content_Provider.cp_uri,
				new_value);
		SQLiteDatabase db = Content_Provider.cp_database.getWritableDatabase();
		long r_id = db.insert(Databse_for_CP.TABLE_NAME, null, new_value);
		if (r_id > 0) {
			Uri new_uri = ContentUris.withAppendedId(Content_Provider.cp_uri, r_id);
			context.getApplicationContext().getContentResolver().notifyChange(new_uri, null);
			uri = new_uri;
		}
		Log.d("Data Inserted : ", " " + new_value);
		//throw new IllegalArgumentException("Unknown " + uri);

	}

	private int queryfromCP() {
		// TODO Auto-generated method stub
		String[] projection = { Databse_for_CP.COLUMN_VAL };
		String selection = Databse_for_CP.COLUMN_KEY+"=" + "'" +seqList.get(i).toString()+"'";
		Cursor new_cursor = Content_Provider.query_dump(
				Content_Provider.cp_uri, projection, selection,
				null, null);
		Log.d("key is :", "" + seqList.get(i).toString());
		
		String cursret="";
		if (new_cursor != null && new_cursor.getCount() != 0) {
			while (new_cursor.moveToNext()) {
				
				cursret += new_cursor.getString(new_cursor
						.getColumnIndexOrThrow(Databse_for_CP.COLUMN_VAL));
				
			}
			if(new_cursor.getCount()>0){
				return 1;
				}
				else {
					return -1;
				} 
		} else {
			if(Content_Provider.returnC.isEmpty()){
				Log.d("data not found", "");
				return -1;
			}else if (Content_Provider.returnC.contains(messageList.get(i).toString())) {
				return 1;
			} else {
				return -1;
			}
			
			

			
		}
	}

}
