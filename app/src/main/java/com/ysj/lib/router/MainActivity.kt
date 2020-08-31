package com.ysj.lib.router

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.annotation.RouteBean
import com.ysj.lib.route.remote.*
import com.ysj.route.generated.routes.`Route$$Path$$app`
import kotlinx.android.synthetic.main.activity_main.*

@Route("/app/MainActivity")
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var i = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv.setOnClickListener {
//            Router.getInstance()
//                .build("/m1/MainActivity")
//                .navigation(this)
            contentResolver.query(
                RouteProvider.getMainRouteProviderUri(),
                null,
                null,
                null,
                null
            )?.also {
                val map = HashMap<String, RouteBean>()
                `Route$$Path$$app`().loadInto(map)

                val remoteParam = RemoteParam()
                for (entry in map) {
                    remoteParam.params[entry.key] = RouteWrapper(entry.value)
                }

                IRouteService.Stub
                    .asInterface(it.extras.getBinder(RouteService.ROUTE_SERVICE))
                    ?.registerRouteGroup("app", remoteParam)
            }?.close()
        }
    }

}