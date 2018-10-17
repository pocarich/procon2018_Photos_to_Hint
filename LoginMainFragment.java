package jp.androidbook.photostohint;

/* ログイン画面管理フラグメント */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LoginMainFragment extends Fragment implements LoginSubFragmentSignIn.CallBack,LoginSubFragmentSignUp.CallBack,LoginSubFragmentResetPassword.CallBack{

    //フラグメント遷移コールバック
    private CallBack callBack;

    /* コンストラクタ */
    public LoginMainFragment() {
        // 空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static LoginMainFragment newInstance() {
        //インスタンス生成
        LoginMainFragment fragment = new LoginMainFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* View生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_login_main, container, false);
    }

    /* View生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* LoginSubFragmentSignin生成 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        //フラグメント遷移時のアニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        //バックスタックに追加
        fragmentTransaction.addToBackStack(null);

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.login_main_fragment_fragment_layout,LoginSubFragmentSignIn.newInstance());

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

    /* LoginSubFragmentSignInコールバック */
    @Override
    public void onLoginSubFragmentSignInInteraction(int transitionID,String userID)
    {
        switch(transitionID)
        {
            case LoginSubFragmentSignIn.TRANSITION_ID_LOGIN:
                //ログイン画面-->メイン画面への遷移実行
                callBack.onLoginMainFragmentInteraction(userID);
                break;
            case LoginSubFragmentSignIn.TRANSITION_ID_SIGNUP: {
                /* LoginSubFragmentSignUpにフラグメント遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.login_main_fragment_fragment_layout, LoginSubFragmentSignUp.newInstance());

                //遷移実行
                fragmentTransaction.commit();
            }
                break;
        }
    }

    /* LoginSubFragmentSignUpコールバック */
    @Override
    public void onLoginSubFragmentSignUpInteraction()
    {
        //前のフラグメントに戻る
        getChildFragmentManager().popBackStack();
    }

    /* LoginSubFragmentResetPasswordコールバック */
    @Override
    public void onLoginSubFragmentResetPasswordInteraction()
    {
        /* LoginSubFragmentSignIn生成 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        //フラグメント遷移時のアニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.login_main_fragment_fragment_layout, LoginSubFragmentSignIn.newInstance());

        //遷移実行
        fragmentTransaction.commit();
    }

    /* ログイン画面からのフラグメント遷移コールバック */
    public interface CallBack {
        void onLoginMainFragmentInteraction(String userID);
    }
}
