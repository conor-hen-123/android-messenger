package com.example.conor.messenger3;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.emitter.Emitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import static android.util.Base64.encode;
import static com.example.conor.messenger3.cryptotest.generateECKeys;
import static org.bouncycastle.util.encoders.Base64.decode;

public class ChatBoxActivity extends AppCompatActivity {

    public RecyclerView myRecylerView;
    public List<Message> MessageList;
    public ChatBoxAdapter chatBoxAdapter;
    public EditText messagetxt;
    public Button send;
    public String fileurl = Environment.getExternalStorageDirectory().getPath();
    Bitmap image;

    //declare socket object
    private Socket socket;

    public String Username, FriendUserName;
    //public String Password;
    PublicKey ownPublicKey, friendPublicKey;
    SecretKey sharedSecretKey;
    KeyPair keyPair;
    public static byte[] iv = new SecureRandom().generateSeed(16);
    private Socket socket2;

    TextView textFile;

    private static final int PICKFILE_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);

        messagetxt = (EditText) findViewById(R.id.message);
        send = (Button) findViewById(R.id.send);

        // //Username = (String) getIntent().getExtras().getString(FriendAdapter.ownusername);
        // //Password = (String) getIntent().getExtras().getString(MainActivity.PASSWORD);
        Bundle values = getIntent().getExtras();
        Username = values.getString("user");
        FriendUserName = values.getString("friend");
        //Password = values.getString("PASSWORD");

        Button buttonPick = (Button) findViewById(R.id.buttonpick);
        textFile = (TextView) findViewById(R.id.textfile);

        buttonPick.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                Intent new_intent = Intent.createChooser(intent, "choose");
                startActivityForResult(new_intent, PICKFILE_RESULT_CODE);
            }
        });


        try {

            socket = IO.socket("http://192.168.1.150:3000/");

            socket.connect();

            socket.emit("connectroom", Username);
            // emit username to add to friend's friendlist
            socket.emit("userjoinedchat", Username);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //setting up recyler
        MessageList = new ArrayList<>();
        myRecylerView = (RecyclerView) findViewById(R.id.messagelist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());

        //initialize ECC key pair
        keyPair = generateECKeys();

        socket.on("sendkeyalarm", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try{socket.emit("sendpublickey", publicKeytoString(keyPair.getPublic()), Username, FriendUserName);} catch (GeneralSecurityException e){e.printStackTrace();}

            }
        });
        socket.on("test", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.d("test", "received");
                    }
                });

                socket.on("generatesharedkey", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        sharedSecretKey = generateSharedSecret(keyPair.getPrivate(), keyPair.getPublic());
                        Log.d("sharedkey1", sharedSecretKey.toString());

                    }
                });
        socket.on("receivepublickey", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //PublicKey data = (PublicKey) args[0];
                        String keystring = (String) args[0];
                        friendPublicKey = stringToPublicKey(keystring);
                        //PublicKey data = (PublicKey) args[0];
                        //friendPublicKey = (PublicKey) data.get("friendpublickey");

                        Log.d("publickey", (String) args[0]);
                        // Create shared AES secret key to encrypt/decrypt the message
                        sharedSecretKey = generateSharedSecret(keyPair.getPrivate(),
                                friendPublicKey);
                        if(sharedSecretKey == null){
                            Log.d("sharedkey2", "null");
                        }
                        else {
                            Log.d("sharedkey", sharedSecretKey.toString());
                        }

                    }
                });
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve the username and the message content and fire the event messagedetection
                if (!messagetxt.getText().toString().isEmpty()) {

                    String cipherText = encryptString(sharedSecretKey, messagetxt.getText().toString());
                    socket.emit("messagedetection", Username, cipherText);
                    if(cipherText == null){
                        Log.d("ciphertext", "null");
                    }
                    else{Log.d("ciphertext", cipherText);}
                    messagetxt.setText(" ");
                }


            }
        });

        //implementing socket listeners
        socket.on("userjoinedthechat", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];

                        Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
        socket.on("userdisconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];

                        Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            //extract data from the fired event

                            String username = data.getString("senderUsername");
                            String cipherText = data.getString("ciphertext");
                            //Log.d("user", username);
                            // Log.d("cipher", cipherText);

                            String message = decryptString(sharedSecretKey, cipherText);
                            Message m = new Message(username, message);
                            MessageList.add(m);
                            chatBoxAdapter = new ChatBoxAdapter(MessageList);
                            chatBoxAdapter.notifyDataSetChanged();
                            myRecylerView.setAdapter(chatBoxAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


 socket.on("receiveimage", new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String imgdata = decryptString(sharedSecretKey,(String) args[0]);
                    Bitmap image = decodeImage(imgdata);
                    //ContextWrapper c = new ContextWrapper(getApplicationContext());
                    //File dir = c.getDir("imagedirectory", ContextWrapper.MODE_PRIVATE);
                    File path = new File(Environment.getExternalStorageDirectory(), "image.jpg");
                    FileOutputStream out = null;

                    try {out = new FileOutputStream(path);
                        image.compress(Bitmap.CompressFormat.PNG, 100, out);
                        Log.d("receiving image", "receiving image");
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                }
            });
        }
    });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    //String FilePath = data.getData().getPath();
                    //encodeImage(FilePath);
                    Uri uri =data.getData();
                    try {image = getBitmapFromUri(uri);}
                    catch (IOException e){
                        Log.d("image error", "image error");
                    }

                    ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 100, outputArray);
                    byte[] b = outputArray.toByteArray();
                    data.putExtra("image",Base64.encodeToString(b, Base64.DEFAULT));
                    setResult(Activity.RESULT_OK, data);

                   // try{
                        //data.putExtra("image", encodeImage(FilePath));
                       // setResult(Activity.RESULT_OK, data);
                        //finish();
                       // break;
                        socket.emit("sendimage", encryptString(sharedSecretKey, Base64.encodeToString(b, Base64.DEFAULT)));
                        //Log.d("a","sending image");
                  //  }catch(JSONException e){
                       // Log.d("sending image failed","sending image failed");

                }
                //textFile.setText(FilePath);
                //Uri uri =data.getData();
                // File file = new File(uri.getPath());
                //Log.d("filepath", FilePath);
                //Log.d("uri", uri.getPath());
                   /* try {image = getBitmapFromUri(uri);}
                    catch (IOException e){
                        Log.d("image error", "image error");
                    }
                }
                if(image == null){
                    Log.d("image not saved", "image not saved");
                }
                else{
                    Log.d("image ok", "image ok");*/
                break;
        }
        }

private String encodeImage(String path) {
    File imagefile = new File(path);
    FileInputStream finput = null;
    try {
        finput = new FileInputStream(imagefile);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }

    Bitmap image = BitmapFactory.decodeStream(finput);
    ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.JPEG, 100, outputArray);
    byte[] b = outputArray.toByteArray();
    return Base64.encodeToString(b, Base64.DEFAULT);

}

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.emit("disconnect", Username);
        socket.disconnect();
    }

    public static KeyPair generateECKeys() {
        try {
            ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("brainpoolp256r1");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    "ECDH", new org.bouncycastle.jce.provider.BouncyCastleProvider());

            keyPairGenerator.initialize(parameterSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            return keyPair;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SecretKey generateSharedSecret(PrivateKey privateKey,
                                                 PublicKey publicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);

            SecretKey key = keyAgreement.generateSecret("AES");
            return key;
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap decodeImage(String data)
    {
        byte[] b = Base64.decode(data,Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b,0,b.length);
        return bmp;
    }

    public static String encryptString(SecretKey key, String plainText) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] plainTextBytes = plainText.getBytes("UTF-8");
            byte[] cipherText;

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            cipherText = new byte[cipher.getOutputSize(plainTextBytes.length)];
            int encryptLength = cipher.update(plainTextBytes, 0,
                    plainTextBytes.length, cipherText, 0);
            encryptLength += cipher.doFinal(cipherText, encryptLength);

            return bytesToHex(cipherText);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | UnsupportedEncodingException | ShortBufferException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptString(SecretKey key, String cipherText) {
        try {
            Key decryptionKey = new SecretKeySpec(key.getEncoded(),
                    key.getAlgorithm());
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] cipherTextBytes = hexToBytes(cipherText);
            byte[] plainText;

            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, ivSpec);
            plainText = new byte[cipher.getOutputSize(cipherTextBytes.length)];
            int decryptLength = cipher.update(cipherTextBytes, 0,
                    cipherTextBytes.length, plainText, 0);
            decryptLength += cipher.doFinal(plainText, decryptLength);

            return new String(plainText, "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException
                | ShortBufferException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytesToHex(byte[] data, int length) {
        String digits = "0123456789ABCDEF";
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i != length; i++) {
            int v = data[i] & 0xff;

            buffer.append(digits.charAt(v >> 4));
            buffer.append(digits.charAt(v & 0xf));
        }

        return buffer.toString();
    }

    public static String bytesToHex(byte[] data) {
        return bytesToHex(data, data.length);
    }

    public static byte[] hexToBytes(String string) {
        int length = string.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4) + Character
                    .digit(string.charAt(i + 1), 16));
        }
        return data;
    }
    public static String publicKeytoString(PublicKey publ) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("ECDH", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        X509EncodedKeySpec spec = fact.getKeySpec(publ,
                X509EncodedKeySpec.class);
        String s = new String(org.bouncycastle.util.encoders.Base64.encode(spec.getEncoded()));
        return  s;
    }

    public static PublicKey stringToPublicKey(String s) {
        byte[] bytes = s.getBytes();
        byte[] publicBytes = decode(bytes);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("ECDH", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            return pubKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        PublicKey keyfail = null;
        return keyfail;
    }

}