package com.zhuyongit.usericonupload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 图像上传 , 拍照上传 ， 从图库上传
 */
public class MainActivity extends ActionBarActivity {

    private static final int CRAMRA_REQUEST_CODE = 1;
    private static final int GRALLRAY_REQUEST_CODE = 2;
    private static final int CROP_REQUEST_CODE = 3 ;

    private ImageView imageView;
    private Button btnCramra;
    private Button btnGrallay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化UI
        initView();

        // 设置按钮监听事件
        setListener();

    }

    /**
     * 初始化UI
     */
    private void initView() {
        imageView = (ImageView) findViewById(R.id.imageView);
        btnCramra = (Button) findViewById(R.id.btnCamra);
        btnGrallay = (Button) findViewById(R.id.btnGrllay);
    }

    /**
     * 设置按钮监听事件
     */
    private void setListener() {
        btnCramra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动摄像头
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CRAMRA_REQUEST_CODE);
            }
        });

        btnGrallay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动图库
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GRALLRAY_REQUEST_CODE);
            }
        });
    }

    /**
     * 将Bitmap保存到本地 ， 并返回File Uri
     *
     * @param bitmap
     * @return 图像 File uri
     */
    private Uri saveBitmap(Bitmap bitmap) {
        File tempDir = new File(Environment.getExternalStorageDirectory().getPath() + "/zhuyongit/");
        if (tempDir.exists()) {
            tempDir.mkdir();
        }
        File img = new File(tempDir.getAbsolutePath() + "user_icon.png");
        try {
            FileOutputStream fos = new FileOutputStream(img);
            // 保存图片
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(img);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 将Content Uri 转换成File Uri
     *
     * @param uri 图像Content Uri
     * @return uri 图像File uri
     */
    private Uri convertUri(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bm = BitmapFactory.decodeStream(is);
            is.close();
            return saveBitmap(bm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 启动图像裁剪
     *
     * @param uri 图像File Uri
     */
    private void startImageZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    /**
     * 上传图片
     * @param bitmap 图片对象
     */
    private void sendImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        bitmap.compress(Bitmap.CompressFormat.PNG,80,stream) ;
        byte[] bytes = stream.toByteArray() ;
        String img = new String(Base64.encode(bytes,Base64.DEFAULT)) ;

        AsyncHttpClient client = new AsyncHttpClient() ;
        RequestParams params = new RequestParams() ;
        params.add("img", img);
        String url = "http://192.168.1.113/api/ImagUpLoad.php" ;
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Toast.makeText(MainActivity.this,"上传成功！",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(MainActivity.this,"上传失败！",Toast.LENGTH_SHORT).show();
            }
        }) ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 摄像头
        if (requestCode == CRAMRA_REQUEST_CODE) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Bitmap bm = bundle.getParcelable("data");
                    Uri fileUri = saveBitmap(bm) ;
                    startImageZoom(fileUri);
                }
            }
        }
        // 图库
        else if (requestCode == GRALLRAY_REQUEST_CODE) {
            if (data != null) {
                Uri uri = data.getData();
                Uri fileUri = convertUri(uri) ;
                startImageZoom(fileUri);
            }
        }
        // 裁剪
        else if (requestCode == CROP_REQUEST_CODE) {
            if (data != null) {
                Bundle bundle = data.getExtras() ;
                if (bundle != null) {
                    Bitmap bm = bundle.getParcelable("data") ;
                    imageView.setImageBitmap(bm);
                    sendImage(bm) ;
                }
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
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
}
