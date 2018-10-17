package jp.androidbook.photostohint;

/* 設定モード設定項目選択フラグメント */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SettingsSubFragment extends Fragment {

    /* コンストラクタ */
    public SettingsSubFragment(){
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SettingsSubFragment newInstance() {
        //フラグメントインスタンス生成
        SettingsSubFragment fragment = new SettingsSubFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_settings_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //アカウント管理ボタン取得
        final Button accountButton = view.findViewById(R.id.settings_sub_fragment_account_button);

        //ライセンスボタン取得
        final Button licenseButton = view.findViewById(R.id.settings_sub_fragment_license_button);

        //アカウント管理ボタン押下時処理設定
        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //フラグメント遷移コールバック実行
                ((CallBack) getParentFragment()).onSettingsSubFragmentInteraction(0);
            }
        });

        //ライセンスボタン押下時処理設定
        licenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //フラグメント遷移コールバック実行
                ((CallBack) getParentFragment()).onSettingsSubFragmentInteraction(1);
            }
        });
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSettingsSubFragmentInteraction(int transitionID);
    }
}
