package jp.androidbook.photostohint;

/* 観光モード口コミ投稿フラグメント */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class TourismAddReviewSubFragment extends Fragment {

    //遷移ID
    public static final int TRANSITION_ID_BACK = 0;
    public static final int TRANSITION_ID_NEXT = 1;

    /* コンストラクタ */
    public TourismAddReviewSubFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static TourismAddReviewSubFragment newInstance() {
        //フラグメントインスタンス生成
        TourismAddReviewSubFragment fragment = new TourismAddReviewSubFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_tourism_add_review_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //戻るボタン取得
        final ImageButton backButton=view.findViewById(R.id.tourism_add_review_sub_fragment_backButton);

        //撮影ボタン取得
        final Button captureButton=view.findViewById(R.id.tourism_add_review_sub_fragment_captureButton);

        //戻るボタン押下時処理設定
        backButton.setOnClickListener(new View.OnClickListener() {
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
                ((CallBack)getParentFragment()).onTourismAddReviewSubFragmentInteraction(TRANSITION_ID_BACK,null);
            }
        });

        //撮影ボタン押下時処理設定
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //撮影開始
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,Constants.Common.REQUEST_CODE_CAMERA);
            }
        });

        //外部ストレージ書き込みパーミッションチェック
        checkWriteExtarnalStorage();
    }

    /* 外部ストレージ書き込みパーミッションチェック */
    private void checkWriteExtarnalStorage() {
        //APIレベルが23以上のとき
        if (Build.VERSION.SDK_INT >= 23) {

            //外部ストレージ書き込みパーミッションが許可されていないとき
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                //初めてのパーミッションリクエストでない場合
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //パーミッションリクエスト
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.Permission.REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                }

                //初めてのパーミッションリクエストの場合
                else {
                    //トーストで説明表示
                    Toast toast = Toast.makeText(getActivity(), "許可されないと端末への写真の保存ができません。", Toast.LENGTH_SHORT);
                    toast.show();

                    //パーミッションリクエスト
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.Permission.REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                }
            }
        }
    }

    /* パーミッションリクエスト結果コールバック */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //外部ストレージ書き込みパーミッションのリクエストのとき
        if(requestCode==Constants.Permission.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
        {
            //拒否された場合
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED)
            {
                //トーストで警告表示
                Toast toast = Toast.makeText(getActivity(),"許可されないと端末への写真の保存ができません。",Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    /* 撮影終了時コールバック */
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        //撮影終了時で正常終了した場合
        if(requestCode==Constants.Common.REQUEST_CODE_CAMERA&&resultCode== Activity.RESULT_OK)
        {
            //出力データがあるなら
            if(data.getExtras()!=null)
            {
                //写真取得
                Bitmap takenImage=(Bitmap)data.getExtras().get("data");

                //写真が存在したら
                if(takenImage!=null)
                {
                    //フラグメント遷移コールバック実行
                    ((CallBack)getParentFragment()).onTourismAddReviewSubFragmentInteraction(TRANSITION_ID_NEXT,takenImage);
                }
            }
        }
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onTourismAddReviewSubFragmentInteraction(int transitionID,Bitmap takenImage);
    }
}
