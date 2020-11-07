package com.ysj.lib.route.module.java;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.ysj.lib.base.mock.MockUserLogin;
import com.ysj.lib.base.utils.ToastUtil;
import com.ysj.lib.route.YCR;
import com.ysj.lib.route.annotation.Route;

@Route(path = "/java/LoginActivity")
public class LoginActivity extends AppCompatActivity {

    EditText edtUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_java_activity_login);
        edtUserName = findViewById(R.id.edtUserName);
    }

    public void onLoginClicked(View view) {
        String userName = edtUserName.getText().toString();
        if (userName.isEmpty()) {
            ToastUtil.showShortToast("用户名不能为空");
            return;
        }
        Boolean loginSuccess = (Boolean) YCR.getInstance()
                .build("/base/MockUserLogin")
                .withRouteAction("login")
                .withString("userName", userName)
                .useGreenChannel()
                .navigationSync(this);
        if (!loginSuccess) {
            ToastUtil.showLongToast("合法的用户名为：" + MockUserLogin.LEGAL_USER_NAME);
            return;
        }
        setResult(RESULT_OK);
        finish();
    }
}