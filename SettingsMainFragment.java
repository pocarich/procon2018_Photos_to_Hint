package jp.androidbook.photostohint;

/* 設定モード管理フラグメント */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsMainFragment extends Fragment implements  SettingsSubFragment.CallBack,SettingsSubFragmentAccountInfo.CallBack,SettingsSubFragmentAccountInfoResetMailAddress.CallBack,SettingsSubFragmentAccountInfoResetPassword.CallBack,SettingsLicenseSubFragment.CallBack{

    //フラグメント遷移コールバック
    private CallBack callBack;

    /* コンストラクタ */
    public SettingsMainFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SettingsMainFragment newInstance() {
        //フラグメントインスタンス生成
        SettingsMainFragment fragment = new SettingsMainFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_settings_main, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* SettingsSubFragment生絵師 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        //遷移時アニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        //SettingsSubFragmentインスタンス生成
        SettingsSubFragment settingsSubFragment = SettingsSubFragment.newInstance();

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.settings_main_fragment_fragment_layout,settingsSubFragment);

        //遷移実行
        fragmentTransaction.commit();
    }

    /* フラグメント生成時処理 */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //アクティヴィティがコールバックを実装している場合
        if (context instanceof CallBack) {
            //コールバック設定
            callBack = (CallBack) context;
        }

        //アクティヴィティがコールバックを実装していない場合
        else {
            //実行時例外を投げる
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /* フラグメント破棄時処理 */
    @Override
    public void onDetach() {
        super.onDetach();

        //コールバックを外す
        callBack = null;
    }

    /* SettingsSubFragmentコールバック */
    @Override
    public void onSettingsSubFragmentInteraction(int transitionID)
    {
        //遷移IDに基づいてフラグメント遷移
        switch(transitionID)
        {
            case 0: {
                /* SettingsSubFragmentAccountInfoに遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //SettingsSubFragmentAccountInfoインスタンス生成
                SettingsSubFragmentAccountInfo settingsSubFragmentAccountInfo = SettingsSubFragmentAccountInfo.newInstance();

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.settings_main_fragment_fragment_layout, settingsSubFragmentAccountInfo);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //遷移実行
                fragmentTransaction.commit();
            }
                break;
            case 1: {
                /* SettingsLicenseSubFragmentに遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //フラグメント遷移時アニメーション設定フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //SettingsLicenseSubFragmentインスタンス生成
                SettingsLicenseSubFragment settingsLicenseSubFragment = SettingsLicenseSubFragment.newInstance();

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.settings_main_fragment_fragment_layout, settingsLicenseSubFragment);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //遷移実行
                fragmentTransaction.commit();
            }
                break;
        }
    }

    /* SettingsSubFragmentAccountInfoコールバック */
    @Override
    public void onSettingsSubFragmentAccountInfoInteraction(int transitionID)
    {
        //遷移IDに基づいてフラグメント遷移
        switch(transitionID)
        {
            case SettingsSubFragmentAccountInfo.TRANSITION_ID_BACK:
                //1つ前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
                break;
            case SettingsSubFragmentAccountInfo.TRANSITION_ID_RESET_PASSWORD: {
                /* SettingsSubFragmentAccountInfoResetPasswordに遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //SettingsSubFragmentAccountInfoResetPasswordインスタンス生成
                SettingsSubFragmentAccountInfoResetPassword settingsSubFragmentAccountInfoResetPassword = SettingsSubFragmentAccountInfoResetPassword.newInstance();

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.settings_main_fragment_fragment_layout, settingsSubFragmentAccountInfoResetPassword);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //遷移実行
                fragmentTransaction.commit();
            }
                break;
            case SettingsSubFragmentAccountInfo.TRANSITION_ID_RESET_MAIL_ADDRESS: {
                /* SettingsSubFragmentAccountInfoResetMailAddressに遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //SettingsSubFragmentAccountInfoResetMailAddressインスタンス生成
                SettingsSubFragmentAccountInfoResetMailAddress settingsSubFragmentAccountInfoResetMailAddress = SettingsSubFragmentAccountInfoResetMailAddress.newInstance();

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.settings_main_fragment_fragment_layout, settingsSubFragmentAccountInfoResetMailAddress);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //遷移実行
                fragmentTransaction.commit();
            }
                break;
            case SettingsSubFragmentAccountInfo.TRANSITION_ID_LOGOUT:
                //ログアウトコールバック実行
                callBack.onSettingsMainFragmentInteraction();
                break;
        }
    }

    /* SettingsSubFragmentAccountInfoResetMailAddressコールバック */
    @Override
    public void onSettingsSubFragmentAccountInfoResetMailAddressInteraction()
    {
        //1つ前のフラグメントに戻る
        getChildFragmentManager().popBackStack();
    }

    /* SettingsSubFragmentAccountInfoResetPasswordコールバック */
    @Override
    public void onSettingsSubFragmentAccountInfoResetPasswordInteraction()
    {
        //1つ前のフラグメントに戻る
        getChildFragmentManager().popBackStack();
    }

    /* SettingsLicenseSubFragmentコールバック */
    @Override
    public void onSettingsLicenseSubFragmentInteraction()
    {
        //1つ前のフラグメントに戻る
        getChildFragmentManager().popBackStack();
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSettingsMainFragmentInteraction();
    }
}
