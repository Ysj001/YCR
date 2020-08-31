package com.ysj.lib.route.module.m1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.annotation.RouteBean
import com.ysj.lib.route.remote.*
import com.ysj.route.generated.routes.`Route$$Path$$m1`
import kotlinx.android.synthetic.main.activity_main.*

@Route("/m1/MainActivity")
class MainActivity : AppCompatActivity() {

    private var i = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv.setOnClickListener {
            contentResolver.query(
                RouteProvider.getMainRouteProviderUri(),
                null,
                null,
                null,
                null
            )?.also {
                val map = HashMap<String, RouteBean>()
                `Route$$Path$$m1`().loadInto(map)

                val remoteParam = RemoteParam()
                for (entry in map) {
                    remoteParam.params[entry.key] = RouteWrapper(entry.value)
                }

                IRouteService.Stub
                    .asInterface(it.extras.getBinder(RouteService.ROUTE_SERVICE))
                    ?.registerRouteGroup("m1", remoteParam)
            }?.close()
        }
    }

}