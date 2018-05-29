package com.dragonfly.testblockchain;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import ch.decent.sdk.DecentListener;
import ch.decent.sdk.DecentWalletManager;
import ch.decent.sdk.model.DecentTransaction;

public class MainActivity extends AppCompatActivity {
    private DecentWalletManager decent;
    private TextView tv_dct;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "这笔交易已经被广播到网络了", Toast.LENGTH_SHORT).show();
                    String dect= add(Float.parseFloat(SharedPreferenceUtil.getPrefString(MainActivity.this,"DCT","dct",null)),0.01f);
                    tv_dct.setText(dect);
                    SharedPreferenceUtil.setPrefString(MainActivity.this,"DCT","dct",dect);
                    break;
                case 3:
                    Toast.makeText(MainActivity.this, "交易未成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button fab = (Button) findViewById(R.id.fab);
        tv_dct= (TextView) findViewById(R.id.tv_dct);
        if(SharedPreferenceUtil.getPrefString(this,"DCT","dct",null)==null){
            SharedPreferenceUtil.setPrefString(this,"DCT","dct","1.50");
            tv_dct.setText("1.50");
        }else{
            tv_dct.setText(SharedPreferenceUtil.getPrefString(this,"DCT","dct",null));
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "开始交易，请等待……", Toast.LENGTH_SHORT).show();
                handler.sendEmptyMessage(1);
                try {
//          发送该值的帐户的名称;0.01 DCT;0.005 dct费;
                    decent.createTransactionFromName("uc79c6ab2fef3f2497f85719825f4e000", 1000000, 500000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

//    这是你连接到节点的地址
        decent = new DecentWalletManager("wss://stage.decentgo.com:8090", new DecentListener() {
            @Override
            public void onTransactionCreated(DecentTransaction decentTransaction) {
//    我们需要一些来自区块链的信息在广播事务到网络之前，
//    一旦它被创建，就把它推到网络上
                Log.d("DECENT", "transaction created");
                decent.pushTransaction(decentTransaction);
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onTransactionPushed(DecentTransaction decentTransaction) {
//        这笔交易已经被广播到网络，都完成了
                Log.d("DECENT", "transaction pushed");
                handler.sendEmptyMessage(2);
            }

            @Override
            public void onError(Exception e) {
//        可以很少看到“签名不规范”的异常，只是重试一下
                Log.e("DECENT", "transaction error", e);
                handler.sendEmptyMessage(3);

            }
        });
//    他是我们寄钱的账户
//    要获得帐户id和从区块链获得的活跃的pub键，您可以使用以下api
//    curl -k --data '{"jsonrpc": "2.0", "method": "get_account_by_name", "params": ["u44606a2e10e0d17c638b898bb1f63207"], "id": 1}' https://stage.decentgo.com:8090/rpc
        decent.importWallet(
                "5JsqeipVFGAJvuaAVAvu9mpjx9qdnPQpfEJZH92jC37EQUjMEJj",
                "DCT7eVYHqTodxMuAoR1ifQaFEghiEZBE2oRTkCoAbpWnBGEmtLuqn",
                "1.2.21",
        "u2dc10b3d9c5360a929614c00c878462b"
    );

    }

    public String add(float d1,float d2){
        BigDecimal b1=new BigDecimal(Float.toString(d1));
        BigDecimal b2=new BigDecimal(Float.toString(d2));
        java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.00");
        return myformat.format( b1.add(b2).floatValue());
    }
}