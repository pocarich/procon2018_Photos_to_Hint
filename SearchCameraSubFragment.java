package jp.androidbook.photostohint;

/* 探索モードお題スポット撮影フラグメント */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;

public class SearchCameraSubFragment extends Fragment {

    //遷移ID
    public static final int TRANSITION_ID_BACK = 0;
    public static final int TRANSITION_ID_NEXT = 1;

    /* コンストラクタ */
    public SearchCameraSubFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchCameraSubFragment newInstance() {
        //フラグメントインスタンス生成
        SearchCameraSubFragment fragment = new SearchCameraSubFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_search_camera_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //戻るボタン取得
        ImageButton backButton=view.findViewById(R.id.search_camera_sub_fragment_backButton);

        //撮影ボタン取得
        Button captureButton=view.findViewById(R.id.search_camera_sub_fragment_captureButton);

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
                ((CallBack)getParentFragment()).onSearchCameraSubFragmentInteraction(TRANSITION_ID_BACK,null);
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
                    ((CallBack)getParentFragment()).onSearchCameraSubFragmentInteraction(TRANSITION_ID_NEXT,takenImage);
                }
            }
        }
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSearchCameraSubFragmentInteraction(int transitionID,Bitmap takenImage);
    }
}
