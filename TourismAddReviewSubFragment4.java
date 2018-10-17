package jp.androidbook.photostohint;

/* 観光モード口コミ投稿フラグメント4 */

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TourismAddReviewSubFragment4 extends Fragment {

    //Bundleキー
    private static final String USER_LATITUDE_KEY="USER_LATITUDE";
    private static final String USER_LONGITUDE_KEY="USER_LONGIUTDE";
    private static final String REGION_NAME_KEY="REGION_NAME";
    private static final String COMMENT_KEY="COMMENT";
    private static final String TAKEN_IMAGE_KEY="TAKEN_IMAGE";

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //メインコンテンツ画面表示
    private ConstraintLayout mainLayout;

    //地名
    private String regionName;

    //ユーザ位置
    private LatLng nowLocation;

    //コメント
    private String comment;

    //撮影写真
    private Bitmap takenImage;

    /* コンストラクタ */
    public TourismAddReviewSubFragment4() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static TourismAddReviewSubFragment4 newInstance(String regionName,LatLng userLocation,String comment,Bitmap takenImage) {
        //フラグメントインスタンス生成
        TourismAddReviewSubFragment4 fragment = new TourismAddReviewSubFragment4();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルに各種パラメータ追加
        args.putString(REGION_NAME_KEY,regionName);
        args.putDouble(USER_LATITUDE_KEY,userLocation.latitude);
        args.putDouble(USER_LONGITUDE_KEY,userLocation.longitude);
        args.putString(COMMENT_KEY,comment);

        //バイト配列出力ストリーム生成
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        //撮影写真をJPEG形式でバイト配列出力ストリームに入力
        takenImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        //撮影写真のバイト配列をバンドルに追加
        args.putByteArray(TAKEN_IMAGE_KEY, stream.toByteArray());

        //フラグメントインスタンスにバンドルセット
        fragment.setArguments(args);

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* フラグメント生成時処理 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //newInstanceで設定したパラメータを取得
        if (getArguments() != null) {
            //バイト配列からtakenImage生成
            byte[] takenImageBytes = getArguments().getByteArray(TAKEN_IMAGE_KEY);
            takenImage = BitmapFactory.decodeByteArray(takenImageBytes, 0, takenImageBytes.length);

            regionName=getArguments().getString(REGION_NAME_KEY);
            nowLocation=new LatLng(getArguments().getDouble(USER_LATITUDE_KEY),getArguments().getDouble(USER_LONGITUDE_KEY));
            comment=getArguments().getString(COMMENT_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_tourism_add_review_sub_fragment4, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.tourism_add_review_sub_fragment4_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.tourism_add_review_sub_fragment4_connection_failure_layout);

        //メインコンテンツ画面表示取得
        mainLayout=view.findViewById(R.id.tourism_add_review_sub_fragment4_main_layout);

        //通信再試行ボタン取得
        final Button retryConnectionButton=view.findViewById(R.id.tourism_add_review_sub_fragment4_connection_failure_button);

        //確認ボタン取得
        final Button confirmationButton=view.findViewById(R.id.tourism_add_review_sub_fragment4_confirmation_button);

        //確認ボタン押下時処理設定
        confirmationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //フラグメント遷移コールバック実行
                ((CallBack)getParentFragment()).onTourismAddReviewSubFragment4Interaction();
            }
        });

        //通信再試行ボタン押下時処理設定
        retryConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //アップロードHTTP通信実行
                connection();
            }
        });

        //アップロードHTTP通信実行
        connection();

        //撮影写真保存
        saveTakenImage();
    }

    /* アップロードHTTP通信 */
    private void connection()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ画面表示OFF
        mainLayout.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json=new JSONObject();

            //JSONに各種パラメータ追加
            json.put("latitude",""+nowLocation.latitude);
            json.put("longitude",""+nowLocation.longitude);
            json.put("message",comment);
            json.put("region_name",regionName);
            json.put("image",Function.encodeTobase64(takenImage));

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //受信データが空なら（通信失敗時）
                    if(data=="") {
                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);

                        //コールバック処理中断
                        return;
                    }

                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if (status) {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //メインコンテンツ画面表示ON
                            mainLayout.setVisibility(View.VISIBLE);
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else
                        {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //通信失敗画面表示ON
                            connectionFailureLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/review/review_upload.php",json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //通信画面表示OFF
            connectionLayout.setVisibility(View.GONE);

            //通信失敗画面表示ON
            connectionFailureLayout.setVisibility(View.VISIBLE);
        }
    }

    /* 撮影写真保存 */
    private void saveTakenImage()
    {
        //APIレベルが23未満または、外部ストレージ書き込みが許可されている場合
        if(Build.VERSION.SDK_INT<23||ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {

            //保存先ディレクトリ指定(なければ作成)
            final String fileDir = Environment.getExternalStorageDirectory().getPath() + "/DCIM/PhotosToHint/";
            File file = new File(fileDir);
            if (!file.exists()) {
                file.mkdir();
            }

            //日付時間を元にファイル名決定
            Date date=new Date();
            SimpleDateFormat fileNameDate=new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName=fileNameDate.format(date)+".jpg";
            String attachName=file.getAbsolutePath()+"/"+fileName;

            try{
                //JPEG形式で撮影写真保存
                FileOutputStream out=new FileOutputStream(attachName);
                takenImage.compress(Bitmap.CompressFormat.JPEG,100,out);
                out.flush();
                out.close();

                //データベースに登録
                ContentValues values=new ContentValues();
                ContentResolver contentResolver=getActivity().getContentResolver();
                values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
                values.put(MediaStore.Images.Media.TITLE,fileName);
                values.put("_data",attachName);
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

                //トーストで結果表示
                Toast.makeText(getContext(), "端末に写真を保存しました。", Toast.LENGTH_SHORT).show();
            }

            //例外発生時
            catch(IOException e) {
                //ログに例外メッセージ出力
                e.printStackTrace();
            }
        }
    }

    /* フラグメント破棄時処理 */
    @Override
    public void onDetach() {
        super.onDetach();

        //takenImageクリア
        Function.cleanupBitmap(takenImage);
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onTourismAddReviewSubFragment4Interaction();
    }
}