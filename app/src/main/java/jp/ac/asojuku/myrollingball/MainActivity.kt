package jp.ac.asojuku.myrollingball

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
    , SensorEventListener, //センサーの反応を受け取るためのインターフェース
    SurfaceHolder.Callback{     //SurfaceViewを実装するのための窓口Holderのコールバックインターフェース

    //instance property
    //SurfaceViewの幅と高さの初期値を設定
    private var surfaceWidth:Int = 0;
    private var surfaceHeight:Int = 0;
    //ボールの半径
    private val radius = 50.0f;
    // ボールの移動量を計算するための係数
    private var coef = 750.0f;

    //ボールの座標
    //X座標
    private var ballX:Float = 0f;
    //Y座標
    private var ballY:Float = 0f;
    //X座標の重力加速度
    private var vx:Float = 0f;
    //Y座標の重力加速度
    private var vy:Float = 0f;

    //前回の時間を記録する変数
    private var time:Long = 0L;

    // 誕生時のライフサイクルイベント
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)//画面レイアウトを設定

        //SurfaceHolderをView部品から取得
        val holder = surfaceView.holder

        //SurfaceHolderのコールバックに自クラスへの通知を追加
        holder.addCallback(this);
    }

    //追加したライフサイクルメソッド
    override fun onResume() {
        super.onResume()
        //resetButtonがクリックされたら実行
        resetButton.setOnClickListener {
            //ボールの初期位置を再設定
            this.ballX = (surfaceWidth / 2).toFloat()
            this.ballY = (surfaceHeight / 8).toFloat()
            //X座標の重力加速度
            vx = 0f;
            //Y座標の重力加速度
            vy = 0f;
            //前回の時間を記録する変数
            time = 0L;
            //移動量を再設定
            coef = 750.0f
            textView.setText(R.string.tenstionText)
        }
    }

    // 精度が変わった時のイベントコールバック
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    // センサーの値が変わった時のイベントコールバック
    override fun onSensorChanged(event: SensorEvent?) {

        //ブロックの座標（left,top,right,bottom）
        val clearBlock = arrayOf((surfaceWidth*-1).toFloat(),(surfaceHeight/1.1).toFloat(),(surfaceWidth/10).toFloat(),(surfaceHeight).toFloat())
        val block1 = arrayOf((surfaceWidth/10).toFloat(),(surfaceHeight/1.1).toFloat(),(surfaceWidth).toFloat(),(surfaceHeight).toFloat())
        val block2 = arrayOf((surfaceWidth/1.7).toFloat(),(surfaceHeight/4).toFloat(),(surfaceWidth).toFloat(),(surfaceHeight/6).toFloat())
        val block3 = arrayOf((surfaceWidth/7.5).toFloat(),(surfaceHeight/2).toFloat(),(surfaceWidth/1.3).toFloat(),(surfaceHeight/2.2).toFloat())
        val circle1 = arrayOf((surfaceWidth/10).toFloat(),(surfaceHeight/12).toFloat(),100.0f)

        //eventの中身がnullなら何もせずにreturn
        if(event == null){
            return;
        }

        //センサーが変わった時にボールを描画する情報を計算する
        //一番最初のセンサー検知の初期時間を取得
        if(time == 0L){
            //最初は現在のミリ秒システム時刻を設定
            time = System.currentTimeMillis()
        }

        //eventのセンサー種別が加速度センサーだったら以下を実行
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //センサーの取得した値（左右の変化：X軸、上下の変化：Y軸）
            //横左右の値
            val x = event.values[0]*-1
            //縦上下の値
            val y = event.values[1]

            //前回の時間(time)かの経過時間を計算（現在時間ー前回時間＝経過時間）
            //計算結果はFloatにしておく
            var t = ((System.currentTimeMillis()) - time).toFloat()

            //timeに今の時間を「前の時間」として記録
            time = (System.currentTimeMillis())

            //ミリ秒単位を秒単位で扱うために1000で割る
            t /= 1000.0f;

            //移動距離を計算（ボールの座標をどれだけ動かすか）
            //X軸の移動距離
            val dx = (vx * t) + (x * t * t) / 2.0f;
            //Y軸の移動距離
            val dy = (vy * t) + (y * t * t) / 2.0f;

            //ボールの新しいX座標
            this.ballX += (dx * coef);
            //ボールの新しいY座標
            this.ballY += (dy * coef);

            //今の瞬間の加速度を代入しなおす
            this.vx += (x * t);
            this.vy += (y * t);

            //画面の端に来たら跳ね返る処理
            //左右について
            if((this.ballX - radius < 0) && vx < 0){
                //左に向かってボールが左にはみ出したとき
                //ボールを反転させて勢いをつける
                vx = (vx * -1) / 1.5f;
                //ボールがはみ出しているのを補正
                ballX = this.radius;
            }else if((this.ballX + radius > this.surfaceWidth) && vx > 0){
                //右に向かってボールが右にはみ出したとき
                //ボールを反転させて勢いをつける
                vx = (vx * -1) / 1.5f;
                //ボールのはみ出しを補正する
                this.ballX = (this.surfaceWidth - radius)
            }
            //上下について
            if((this.ballY - radius < 0) && vy < 0){
                //上に向かってボールが上にはみ出したとき
                //ボールを反転させて勢いをつける
                vy = (vy * -1) / 1.5f;
                //ボールがはみ出しているのを補正
                ballY = this.radius;
            }else if((this.ballY + radius > this.surfaceHeight) && vy > 0){
                //右に向かってボールが右にはみ出したとき
                //ボールを反転させて勢いをつける
                vy = (vy * -1) /1.5f;
                //ボールのはみ出しを補正する
                this.ballY = (this.surfaceHeight - radius)
            }

            //クリアブロックに当たった時の判定を作る
            if (clearBlock[0]  < ballX + radius && clearBlock[2] > ballX - radius){
                if (clearBlock[1] < ballY + radius && clearBlock[3] > ballY - radius){
                    this.coef = 0.0f
                    textView.setText(R.string.clearText)
                }
            }
            //ブロックに当たった時の判定を作る
            if (block1[0]  < (ballX + radius) && block1[2] > (ballX - radius) && block1[1] < (ballY + radius) && block1[3] > (ballY - radius)){
                    this.coef = 0.0f
                    textView.setText(R.string.missText)
            }

            //ブロックに当たった時の判定を作る
            if (block2[0]  < (ballX + radius) && block2[2] > (ballX - radius)){
                if (block2[1] < (ballY + radius) && block2[3] > (ballY - radius)){
                    this.coef = 0.0f
                    textView.setText(R.string.missText)
                }
            }
            //ブロックに当たった時の判定を作る
            if (block3[0]  < (ballX + radius) && block3[2] > (ballX - radius)){
                if (block3[1] < (ballY + radius) && block3[3] > (ballY - radius)){
                    this.coef = 0.0f
                    textView.setText(R.string.missText)
                }
            }
            //ブロックに当たった時の判定を作る
            if ((circle1[0] + circle1[2]) > (ballX + radius) && (circle1[0] - circle1[2]) > (ballX - radius)){
                if ((circle1[1] + circle1[2]) > (ballY + radius) && (circle1[1] - circle1[2]) > (ballY - radius)){
                    this.coef = 0.0f
                    textView.setText(R.string.missText)
                }
            }

            //キャンバスに描画する命令
            this.drawCanvas(clearBlock,block1,block2,block3,circle1);
        }
    }

    //Surfaceのキャンバスに描画する処理をまとめたメソッド
    private fun drawCanvas(clearBlock: Array<Float>,
                           block1: Array<Float>,
                           block2: Array<Float>,
                           block3: Array<Float>,
                           circle1: Array<Float>){

        //キャンバスをロックして取得する
        val canvas = surfaceView.holder.lockCanvas()
        //キャンバスに背景を設定する(dark gray)
        canvas.drawColor(Color.DKGRAY)
        //キャンバスに円を描いてボールにする
        canvas.drawCircle(
            this.ballX,//ボールのX座標
            this.ballY,//ボールのY座標
            this.radius,//ボールの半径
            Paint().apply {//Paintの匿名クラス
                //ボールの色を黄色にする
                this.color = Color.YELLOW
            }
        )

        //clearBlockを描画
        canvas?.drawRect(clearBlock[0],clearBlock[1],clearBlock[2],clearBlock[3],Paint().apply{this.color = Color.YELLOW})
        //block1を描画
        canvas?.drawRect(block1[0],block1[1],block1[2],block1[3],Paint().apply{this.color = Color.RED})
        //block2を描画
        canvas?.drawRect(block2[0],block2[1],block2[2],block2[3],Paint().apply{this.color = Color.RED})
        //block3を描画
        canvas?.drawRect(block3[0],block3[1],block3[2],block3[3],Paint().apply{this.color = Color.RED})
        //circle1を描画
        canvas?.drawCircle(circle1[0],circle1[1],circle1[2],Paint().apply{this.color = Color.RED})

        //キャンバスのロックを解除してキャンバスを描画
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    //Surfaceが生成された時のイベントに反応して呼ばれるコールバック
    //画面が表示状態になった時のイベント処理
    override fun surfaceCreated(holder: SurfaceHolder?) {
        // センサーマネージャをOSから取得
        val sensorManager =
            this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        // 加速度センサー(Accelerometer)を指定してセンサーマネージャからセンサーを取得
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // リスナー登録して加速度センサーの監視を開始
        sensorManager.registerListener(
            this,  // イベントリスナー機能をもつインスタンス（自クラスのインスタンス）
            accSensor, // 監視するセンサー（加速度センサー）
            SensorManager.SENSOR_DELAY_GAME // センサーの更新頻度
        )
    }

    //Surfaceが更新されたときにイベントに反応して呼ばれるコールバック
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        //Surfaceが変化するたびに幅と高さを設定
        this.surfaceWidth = width;
        this.surfaceHeight = height;

        //ボールの初期位置を設定
        this.ballX = (surfaceWidth / 2).toFloat()
        this.ballY = (surfaceHeight / 8).toFloat()
    }

    //Surfaceが破棄されたときにイベントに反応して呼ばれるコールバック
    //画面を非表示になった時のイベント処理
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // センサーマネージャを取得
        val sensorManager =
            this.getSystemService(Context.SENSOR_SERVICE) as
                    SensorManager;
        // センサーマネージャに登録したリスナーを解除（OFFにする）
        sensorManager.unregisterListener(this)
    }


}