<?php
/* サインイン */

//データベース操作クラス
class MyDB
{
     //mysqli使用
     public $mysqli;

     /* コンストラクタ */
     function __construct($host_or_ip,$user_id,$password,$db_name)
     {
          //データベースアクセス
          $this->mysqli = new mysqli($host_or_ip,$user_id,$password,$db_name);

          //コネクションエラー発生時
          if($this->mysqli->connect_error)
          {
               //処理中断
               echo $this->mysqli->connect_error;
               exit;
          }
          else
          {
               //文字コード"UTF-8"使用
               $this->mysqli->set_charset("utf8");
          }
     }

     /* デストラクタ */
     function __destruct()
     {
          //データベースアクセス終了
          $this->mysqli->close();
     }

     /* クエリ実行 */
     function query($sql)
     {
          //クエリを実行して結果を受け取る
          $result = $this->mysqli->query($sql);

          //エラー発生時
          if($result == FALSE)
          {
               //エラーをJSON形式で返す
               $error = $this->mysqli->errno.": ".$this->mysqli->error;
               $rtn = array(
                    'status' => FALSE,
                    'count' => 0,
                    'result' => "",
                    'error' => $error
               );
               return json_encode($rtn);
          }

          //ブーリアン結果(SELECT文以外)の場合
          if($result === TRUE)
          {
               //変更を受けたレコード数をJSON形式で返す
               $rtn = array(
                    'status' => TRUE,
                    'count' => $this->mysqli->affected_rows,
                    'result' => "",
                    'error' => ""
               );
               return json_encode($rtn);
          }

          //ブーリアン結果でない場合
          else
          {
               //取得データをJSON形式で返す
               $data = array();
               while($row = $result->fetch_assoc())
               {
                    $data[]=$row;
               }
               $result->close();

               $rtn = array(
                    'status' => TRUE,
                    'count' => $this->mysqli->affected_rows,
                    'result' => $data,
                    'error' =>""
               );
               return json_encode($rtn);
          }

          //文字列のエスケープ
          function escape($str)
          {
               return $this->mysqli->real_escape_string($str);
          }
     }   
}

//ヘッダ：JSON形式、文字コード"UTF-8"
header('Content-Type: application/json; charset=utf-8');

//クライアントからのデータ受信
$params=json_decode(file_get_contents('php://input'),true);

//必要なパラメータが受信データに含まれていない場合エラー
if(!isset($params["user_id"]))
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => 'some parameters are not set.'
	);
	exit(json_encode($rtn));
}

//データベース接続
$db = new MyDB("*****","*****","*****","*****");

//IDが一致するレコード取得
$exist_data=$db->query("SELECT * FROM id_table WHERE user_id='{$params["user_id"]}'");
$exist_data=json_decode($exist_data,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($exist_data['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $exist_data['error']
	);
	exit(json_encode($rtn));
}

//IDが一致するレコードが存在しないなら認証エラー終了
else if(count($exist_data['result'])===0)
{
	$rtn=array(
		'status' => FALSE,
                'result' => 1,
		'error' =>""
	);
	exit(json_encode($rtn));
}

//IDが一致するレコードが存在するなら
else
{
        //パスワードが一致したら認証成功終了
	if(password_verify($params["password"],$exist_data["result"][0]["password"]))
	{
		$rtn=array(
			'status' => TRUE,
                	'result' => $exist_data['result'][0]['email'],
			'error' =>""
		);
		echo json_encode($rtn);
	}

        //パスワードが一致しないなら認証エラー終了
	else
	{
		$rtn=array(
			'status' => FALSE,
                	'result' => 2,
			'error' =>""
		);
		exit(json_encode($rtn));
	}
}
