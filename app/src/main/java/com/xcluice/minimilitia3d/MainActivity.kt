package com.xcluice.minimilitia3d

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout

class MainActivity : Activity() {
    private lateinit var glView: GLSurfaceView
    private lateinit var hudView: HudOverlayView
    private lateinit var engine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        engine = GameEngine(SfxEngine())
        glView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(3)
            setRenderer(GameRenderer(engine) { hudView.postInvalidate() })
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        hudView = HudOverlayView(this, engine)

        val root = FrameLayout(this)
        root.addView(glView)
        root.addView(hudView)
        setContentView(root)
        enableImmersive()

        UpdateChecker.check(this)
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
        enableImmersive()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
        if (engine.state == "playing") engine.state = "paused"
    }

    private fun enableImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
    }
}
