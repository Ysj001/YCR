package com.ysj.lib.route.module.java;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ysj.lib.base.mock.MockUserLogin;
import com.ysj.lib.ycr.YCR;
import com.ysj.lib.ycr.annotation.Route;
import com.ysj.lib.ycr.annotation.RouteParam;

import java.util.Arrays;

/**
 * 演示自动注入参数
 * <p>
 *
 * @author Ysj
 * Create time: 2020/11/27
 */
@Route(path = "/java/InjectParamActivity")
public class InjectParamActivity extends AppCompatActivity {

    @RouteParam
    int i;

    @RouteParam
    boolean b;

    @RouteParam
    String str;

    @RouteParam
    MockUserLogin.UserInfo userInfo;

    @RouteParam
    Bundle bd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 通过该代码标记改类需要注入
        YCR.getInstance().inject(this);
        setContentView(contentView());
    }

    @SuppressLint("SetTextI18n")
    private TextView contentView() {
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setText("" + '\n' +
                "i: " + i + '\n' +
                "b: " + b + '\n' +
                "str: " + str + '\n' +
                "userInfo: " + userInfo + '\n' +
                "bd-key: " + Arrays.toString(bd.keySet().toArray()) + '\n' +
                "");
        return tv;
    }
}
