package com.example.talk.xmpp;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText usernameEt, pwdEt, addFriendEt, sendTextEt;
    private Button loginBtn, connectionBtn, registBtn, addFriendBtn, sendTextBtn,sendPictureBtn,sendVedioBtn;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what
                    ) {
                case 1:
                    Toast.makeText(getApplicationContext(), "链接成功", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    sendTextEt.setText((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        initView();
    }

    private void initView() {
        usernameEt = (EditText) findViewById(R.id.et_username);
        pwdEt = (EditText) findViewById(R.id.et_pwd);
        addFriendEt = (EditText) findViewById(R.id.et_friend_id);
        sendTextEt = (EditText) findViewById(R.id.et_send_text);


        loginBtn = (Button) findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);
        registBtn = (Button) findViewById(R.id.btn_regist);
        registBtn.setOnClickListener(this);
        connectionBtn = (Button) findViewById(R.id.btn_connection);
        connectionBtn.setOnClickListener(this);
        addFriendBtn = (Button) findViewById(R.id.btn_add_friend);
        addFriendBtn.setOnClickListener(this);
        sendTextBtn = (Button) findViewById(R.id.btn_send_text);
        sendTextBtn.setOnClickListener(this);
        sendPictureBtn = (Button) findViewById(R.id.btn_send_picture);
        sendPictureBtn.setOnClickListener(this);
        sendVedioBtn = (Button) findViewById(R.id.btn_send_vedio);
        sendVedioBtn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private XMPPTCPConnection connection;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connection:
                new Thread() {
                    @Override
                    public void run() {
                        connection = getConnection();
                        try {
                            connection.connect();
                        } catch (SmackException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                break;
            case R.id.btn_regist:
                final String username = usernameEt.getText().toString().trim();
                final String pwd = pwdEt.getText().toString().trim();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
                    Toast.makeText(getApplicationContext(), "用户名和密码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        if (connection.isConnected()) {
                            connection.disconnect();
                        }
                        try {
                            connection.connect();
                            AccountManager manager = AccountManager.getInstance(connection);
                            if (manager.supportsAccountCreation()) {
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("email", "demo@demo.com");
                                map.put("android", "create");
                                manager.createAccount(Localpart.from(username), pwd, map);
                                mHandler.obtainMessage(2).sendToTarget();
                            }
                        } catch (SmackException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                break;
            case R.id.btn_login:
                final String loginId = usernameEt.getText().toString().trim();
                final String loginPwd = pwdEt.getText().toString().trim();
                if (TextUtils.isEmpty(loginId) || TextUtils.isEmpty(loginPwd)) {
                    Toast.makeText(getApplicationContext(), "用户名和密码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        if (connection.isConnected()) {
                            connection.disconnect();
                        }
                        try {
                            connection.connect();
                            connection.login(loginId, loginPwd);
                            Presence presence = new Presence(Presence.Type.available);
                            presence.setStatus("我在线");
                            connection.sendStanza(presence);
                            mHandler.obtainMessage(3).sendToTarget();
                        } catch (SmackException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                break;
            case R.id.btn_add_friend:
                final String friendIdStr = addFriendEt.getText().toString().trim();
                if (TextUtils.isEmpty(friendIdStr)) {
                    Toast.makeText(getApplicationContext(), "添加好友不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                new Thread(){
                    @Override
                    public void run() {
                        Roster roster = Roster.getInstanceFor(connection);
                        try {
                            roster.createEntry(JidCreate.entityBareFrom(friendIdStr), friendIdStr,null);
                        } catch (SmackException.NotLoggedInException e) {
                            e.printStackTrace();
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (XmppStringprepException e) {
                            e.printStackTrace();
                        }
                    }
                }
               .start();
                break;
            case R.id.btn_send_text:
                final String firendStr = addFriendEt.getText().toString().trim();
                final String sendTextStr = sendTextEt.getText().toString().trim();
                if (TextUtils.isEmpty(firendStr)) {
                    Toast.makeText(getApplicationContext(), "添加好友不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(sendTextStr)) {
                    Toast.makeText(getApplicationContext(), "发送文本不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                new Thread(){
                    @Override
                    public void run() {
                        ChatManager chatManager = ChatManager.getInstanceFor(connection);
                        chatManager.addChatListener(new ChatManagerListener() {
                            @Override
                            public void chatCreated(Chat chat, boolean createdLocally) {
                                chat.addMessageListener(new ChatMessageListener() {
                                    @Override
                                    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
                                        String content =message.getBody();
                                        if(TextUtils.isEmpty(content))return;
                                        mHandler.obtainMessage(4,content).sendToTarget();
                                    }
                                });
                            }
                        });
                        try {
                            Chat chat = chatManager.createChat(JidCreate.entityBareFrom(firendStr));
                            try {
                                chat.sendMessage(sendTextStr);
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (XmppStringprepException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                break;
            case R.id.btn_send_picture:
Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
                break;
            case R.id.btn_send_vedio:
//                Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                Intent i=new Intent(Intent.ACTION_GET_CONTENT
                );
                i.setType("video/*");
                startActivityForResult(i,2);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case 1:
                if(data==null||data.getData()==null)return;
                String[] projection=new String[]{MediaStore.Images.ImageColumns.DATA};
        Cursor cursor=getContentResolver().query(data.getData(),projection,null,null,null);
                if(cursor==null||cursor.getCount()==0){
                    Toast.makeText(getApplicationContext(),"图片不存在",Toast.LENGTH_LONG).show();
                    return;
                }
                int index=cursor.getColumnIndex(projection[0]);
                cursor.moveToFirst();
             final  String path= cursor.getString(index);
                cursor.close();
                new Thread(){
                    @Override
                    public void run() {
                        FileTransferManager fileTransferManager=FileTransferManager.getInstanceFor(connection);
                        try {
                            OutgoingFileTransfer transfer=fileTransferManager.createOutgoingFileTransfer(JidCreate.entityFullFrom(addFriendEt.getText().toString().trim()));
                            try {
                                transfer.sendFile(new File(path),"pic");
                            } catch (SmackException e) {
                                e.printStackTrace();
                            }
                        } catch (XmppStringprepException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                break;
            case 2:
                if(data==null||data.getData()==null)return;
                projection=new String[]{MediaStore.MediaColumns.DATA};

                cursor=getContentResolver().query(data.getData(),null,null,null,null);
                if(cursor==null||cursor.getCount()==0){
                    Toast.makeText(getApplicationContext(),"图片不存在",Toast.LENGTH_LONG).show();
                    return;
                }
                cursor.moveToFirst();
                for(int i=0;i<cursor.getColumnCount();i++){
                    String name=cursor.getColumnName(i);
                    String value=cursor.getString(i);
                    System.out.println(value);
                }

//                index=cursor.getColumnIndex(projection[0]);
//                final  String videoPath= cursor.getString(index);
                final String videoPath=queryContentImg(data.getData());
                cursor.close();
                new Thread(){
                    @Override
                    public void run() {
                        FileTransferManager fileTransferManager=FileTransferManager.getInstanceFor(connection);
                        fileTransferManager.addFileTransferListener(new FileTransferListener() {
                            @Override
                            public void fileTransferRequest(FileTransferRequest request) {
                                IncomingFileTransfer incomingFileTransfer=request.accept();
                            }
                        });
                        try {
                            OutgoingFileTransfer transfer=fileTransferManager.createOutgoingFileTransfer(JidCreate.entityFullFrom(addFriendEt.getText().toString().trim()));
                            try {
                                transfer.sendFile(new File(videoPath),"快看视频");
                            } catch (SmackException e) {
                                e.printStackTrace();
                            }
                        } catch (XmppStringprepException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();


                break;
        }
    }

    private XMPPTCPConnection getConnection() {
        String server = "192.168.1.5";
        int port = 5222;
        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
        builder.setHost(server);
        builder.setPort(port);
        DomainBareJid domain = null;
        try {
            domain = JidCreate.domainBareFrom(Domainpart.from("leellun-pc"));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        builder.setXmppDomain(domain);

        builder.setCompressionEnabled(false);
        builder.setDebuggerEnabled(true);
        //发送状态;
        builder.setSendPresence(true);

        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        XMPPTCPConnection connection = new XMPPTCPConnection(builder.build());
        return connection;

    }
    /**
     * 更具图片uri查询图片地址
     * @param uri
     * @return
     * @return
     * @exception
     * @author LiuLun
     * @Time 2015年5月21日下午4:52:17
     */
    private String queryContentImg(Uri uri){
        Cursor cursor=getContentResolver().query(uri,new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
        while(cursor.moveToNext()){
            String path=cursor.getString(0);
            if(!TextUtils.isEmpty(path)&&new File(path).exists()){
                cursor.close();
                return path;
            }
            path=getPath(getApplicationContext(),uri);
            if(!TextUtils.isEmpty(path)&&new File(path).exists()){
                cursor.close();
                return path;
            }
        }
        return null;
    }
    /**
     * 得到图片路径
     *
     * @param context
     * @param uri
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri
                    .getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if ("com.android.providers.downloads.documents".equals(uri
                    .getAuthority())) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if ("com.android.providers.media.documents".equals(uri
                    .getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if ("com.google.android.apps.photos.content".equals(uri
                    .getAuthority()))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else{
            return selectImage(context, uri);
        }

        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        String result = null;
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                result = cursor.getString(index);
            }
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return result;
    }
    public static String selectImage(Context context, Uri selectedImage) {
        // Log.e(TAG, selectedImage.toString());
        if (selectedImage != null) {
            String uriStr = selectedImage.toString();
            String path = uriStr.substring(10, uriStr.length());
            if (path.startsWith("com.sec.android.gallery3d")) {
                Log.e("tag",
                        "It's auto backup pic path:" + selectedImage.toString());
                return null;
            }
        }
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }
}
