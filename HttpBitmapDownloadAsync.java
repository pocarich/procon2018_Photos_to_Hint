package jp.androidbook.photostohint;

/* HTTP通信クラス（bitmap取得） */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public final class HttpBitmapDownloadAsync extends AsyncTask<String, Void, Bitmap>
{
    //通信後処理コールバック
    CallBack callBack;

    /* バックグラウンドでの非同期処理 */
    @Override
    protected Bitmap doInBackground(String... params)//params[0]:接続先URL
    {
        //HTTP通信インスタンス
        HttpsURLConnection connection = null;

        //URLインスタンス
        URL url = null;

        //取得bitmap
        Bitmap bitmap = null;

        try {
            //http->httpsにリダイレクト
            params[0] = params[0].replace("http://", "https://");

            //URL生成
            url = new URL(params[0]);

            //HTTP通信インスタンス生成
            connection = (HttpsURLConnection) url.openConnection();

            //リクエストメソッドにPOSTを設定
            connection.setRequestMethod("POST");

            //リダイレクトを自動で許可しない
            connection.setInstanceFollowRedirects(false);

            //データを取得する
            connection.setDoInput(true);

            //データを送信しない
            connection.setDoOutput(false);

            //通信実行
            connection.connect();

            //HTTPステータスコード取得
            final int status = connection.getResponseCode();

            Log.d("status", "" + status);

            //HTTPステータスコードが200(HTTP_OK)なら
            if (status == HttpsURLConnection.HTTP_OK) {
                //入力ストリーム
                InputStream in = null;

                try {
                    //入力ストリーム取得
                    in = connection.getInputStream();

                    //取得データからbitmap生成
                    bitmap = BitmapFactory.decodeStream(in, null, null);

                    //入力ストリームを閉じる
                    in.close();
                }

                //入出力例外発生時
                catch (IOException e) {
                    //ログに例外メッセージ出力
                    e.printStackTrace();
                } finally {
                    //入力ストリームがnullでないなら
                    if (in != null) {
                        //入力ストリームを閉じる
                        in.close();
                    }
                }
            }
        }

        //URL例外発生
        catch (MalformedURLException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();
        }

        //入出力例外発生時
        catch (IOException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();
        }

        //bitmapを返す
        return bitmap;
    }

    /* 通信終了後処理 */
    @Override
    protected void onPostExecute(Bitmap result)
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
        void onAsyncTaskResult(Bitmap bitmap);
    }
}