package jp.androidbook.photostohint;

/* 設定モードライセンス表示フラグメント */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;

public class SettingsLicenseSubFragment extends Fragment {

    /* コンストラクタ */
    public SettingsLicenseSubFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SettingsLicenseSubFragment newInstance() {
        //フラグメントインスタンス生成
        SettingsLicenseSubFragment fragment = new SettingsLicenseSubFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_settings_license_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //戻るボタン取得
        final ImageButton backButton=view.findViewById(R.id.settings_license_sub_fragment_back_button);

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
                ((CallBack) getParentFragment()).onSettingsLicenseSubFragmentInteraction();
            }
        });
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSettingsLicenseSubFragmentInteraction();
    }
}
