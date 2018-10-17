package jp.androidbook.photostohint;

/* HTTP通信クラス（JSON取得） */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public final class HttpResponsAsync extends AsyncTask<String, Void, String>
{

    //通信後処理コールバック
    CallBack callBack;

    /* バックグラウンドでの非同期処理 */
    @Override
    protected String doInBackground(String... params)//params[0]:接続先URL,params[1]:送信データ(JSON)
    {
        //HTTP通信インスタンス
        HttpsURLConnection con = null;

        //URLインスタンス
        URL url = null;

        //取得JSON
        String result = "";

        try {
            //http->httpsにリダイレクト
            params[0] = params[0].replace("http://", "https://");

            //URL生成
            url = new URL(params[0]);

            //接続用HttpURLConnectionオブジェクト作成
            con = (HttpsURLConnection) url.openConnection();

            //リクエストメソッドの設定
            con.setRequestMethod("POST");

            //ヘッダ送信データタイプ->JSON
            con.setRequestProperty("Content-Type", "application/json");

            //リダイレクトを自動で許可しない
            con.setInstanceFollowRedirects(false);

            //データ受信を行う
            con.setDoInput(true);

            //params[1](送信データ)が存在するときデータ送信許可
            con.setDoOutput(params.length > 1);

            //通信実行
            con.connect();

            //params[1](送信データ)が存在するとき
            if (params.length > 1) {
                //データ送信用出力ストリーム生成
                OutputStream os = con.getOutputStream();

                //上の出力ストリームを出力先としたプリントストリーム生成
                PrintStream ps = new PrintStream(os);

                //params[1](送信データ)を送信
                ps.print(params[1]);

                //プリントストリームを閉じる
                ps.close();

                //出力ストリームを閉じる
                os.close();
            }

            //HTTPステータスコード取得
            int status = con.getResponseCode();

            Log.d("status", "" + status);

            //HTTPステータスコードが200(HTTP_OK)なら
            if (status == HttpsURLConnection.HTTP_OK) {
                //文字列バッファ生成
                StringBuffer sb = new StringBuffer();

                //入力ストリーム（受信データ）取得
                InputStream in = con.getInputStream();

                //一時保存用文字列
                String tempStr = "";

                //文字コード"UTF-8"で受信データを読み込むリーダー生成
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                //1行ずつ読み込む
                while ((tempStr = br.readLine()) != null) {
                    //文字列バッファに行ごとのデータ追加
                    sb.append(tempStr);
                }
                try {
                    //入力ストリーム（受信データ）を閉じる
                    in.close();
                }

                //例外発生時
                catch (Exception e) {
                    //ログに例外メッセージ出力
                    e.printStackTrace();
                }

                //文字列バッファからJSON文字列取得
                result = sb.toString();
            }

        }

        //URL例外発生時
        catch (MalformedURLException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();
        }

        //入出力例外発生時
        catch (IOException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();
        }

        Log.d("result", result);

        //受信JSON文字列を返す
        return result;
    }

    /* 通信終了後処理 */
    @Override
    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);

        //通信後処理コールバック実行
        callBack.onAsyncTaskResult(result);
    }

    /* 通信後処理コールバックセッター */
    public void setCallBack(CallBack callBack)
    {
        this.callBack = callBack;
    }

    /* 通信後処理コールバック */
    public interface CallBack
    {
        void onAsyncTaskResult(String st);
    }
}
