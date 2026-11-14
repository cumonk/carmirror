package com.example.carmirror

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.MessageInfo
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.validation.HostValidator
import androidx.car.app.AppManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MirrorCarAppService : CarAppService() {
    // Sadece geliştirici modunda yan yükleme için. Mağazaya yüklenecekse gerçek host listesi kullanılmalı.
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = MirrorSession()
}

class MirrorSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = MirrorScreen(carContext)
}

class MirrorScreen(carContext: CarContext) : Screen(carContext) {

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            val s = surfaceContainer.surface ?: return
            MirrorController.attach(s, surfaceContainer.width, surfaceContainer.height, surfaceContainer.dpi)
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            MirrorController.detach()
        }
    }

    init {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(surfaceCallback)
        MirrorController.onChange = { invalidate() }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                MirrorController.detach()
                MirrorController.onChange = null
                carContext.getCarService(AppManager::class.java).setSurfaceCallback(null)
            }
        })
    }

    override fun onGetTemplate(): Template {
        val stop = Action.Builder()
            .setTitle("Çıkış")
            .setOnClickListener {
                carContext.startService(
                    Intent(carContext, MirrorForegroundService::class.java)
                        .setAction(MirrorForegroundService.ACTION_STOP)
                )
                carContext.finishCarApp()
            }
            .build()

        val builder = NavigationTemplate.Builder()
            .setActionStrip(ActionStrip.Builder().addAction(stop).build())
            .setBackgroundColor(CarColor.createCustom(0xFF000000.toInt(), 0xFF000000.toInt()))

        if (!MirrorController.isProjecting) {
            builder.setNavigationInfo(
                MessageInfo.Builder("Telefonda \"Yansıtmayı Başlat\" düğmesine bas").build()
            )
        }
        return builder.build()
    }
}
