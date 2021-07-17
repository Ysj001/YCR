package com.ysj.lib.route.module.java;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ysj.lib.base.YCRConst;
import com.ysj.lib.base.mock.MockUserLogin;
import com.ysj.lib.base.utils.ToastUtil;
import com.ysj.lib.ycr.YCR;
import com.ysj.lib.ycr.annotation.Route;

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
        Boolean loginSuccess = YCR.getInstance()
                .build(YCRConst.route.base_MockUserLogin)
                .withRouteAction("login")
                .withString("userName", userName)
                .navigationSync(this);
        if (!loginSuccess) {
            ToastUtil.showLongToast("合法的用户名为：" + MockUserLogin.LEGAL_USER_NAME);
            return;
        }
        setResult(RESULT_OK);
        finish();
    }
}