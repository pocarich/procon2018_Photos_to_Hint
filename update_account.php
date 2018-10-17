<?php
/* ユーザアカウント情報更新 */

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
if(!isset($params["user_id"])||!isset($params["password"]))
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => 'some parameters are not set.'
	);
	exit(json_encode($rtn));
}

//変更がない場合終了
if(!array_key_exists("email",$params)&&!array_key_exists("new_password",$params))
{
	$rtn = array(
             'status' => FALSE,
             'error' =>"no params are set."
        );
        exit(json_encode($rtn));
}

//データベース接続
$db = new MyDB("*****","*****","*****","*****");

//ユーザIDが一致するアカウントレコード取得
$select_sql="SELECT * FROM id_table WHERE user_id='{$params["user_id"]}'";
$select_result=$db->query($select_sql);
$select_result=json_decode($select_result,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($select_result['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $select_result['error']
	);
	exit(json_encode($rtn));
}

//ユーザIDが一致するレコードが存在しないかパスワードが一致しない場合認証エラー
if($select_result['count']==0||!password_verify($params["password"],$select_result["result"][0]["password"]))
{
	$rtn=array(
		'status' => TRUE,
                'result' => 1,
		'error' => ""
	);
	exit(json_encode($rtn));
}

//メールアドレスに変更がある場合
if(array_key_exists("email",$params))
{
        //新しいメールアドレスと一致するアカウントレコード数取得
	$result=$db->query("SELECT * FROM id_table WHERE email='{$params["email"]}'");
	$result=json_decode($result,true);

        //エラー発生時エラー内容をJSON形式で送信して終了
	if($result['status']===FALSE)
	{
		$rtn=array(
			'status' => FALSE,
  	              	'result' => -1,
			'error' => $result['error']
		);
		exit(json_encode($rtn));
	}

        //メールアドレスの重複が検出された場合エラー終了
	if($result['count']>0)
	{
		$rtn=array(
			'status' => TRUE,
        	        'result' => 2,
			'error' => ""
		);
		exit(json_encode($rtn));
	}
}

//ユーザアカウント情報更新クエリ
$update_sql="UPDATE id_table SET ";
if(array_key_exists("email",$params))
{
	$update_sql.="email = '{$params["email"]}',";
}
if(array_key_exists("new_password",$params))
{
	$hash=password_hash($params["new_password"],PASSWORD_DEFAULT);
	$update_sql.="password = '{$hash}',";
}
$update_sql=substr($update_sql,0,-1);
$update_sql.=" WHERE user_id='{$params["user_id"]}'";

//ユーザアカウント情報変更
$update_result=$db->query($update_sql);
$update_result=json_decode($update_result,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($update_result['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
	        'result' => -1,
		'error' => $update_result['error']
	);
	exit(json_encode($rtn));
}

//正常終了
$rtn=array(
	'status' => TRUE,
        'result' => 0,
	'error' => ""
);
echo json_encode($rtn);
