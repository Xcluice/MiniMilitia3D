package com.xcluice.minimilitia3d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class HudOverlayView(context: Context, private val engine: GameEngine) : View(context) {
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(215, 20, 24, 30) }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 255, 255, 255); style = Paint.Style.STROKE; strokeWidth = 2.5f
    }
    private val olivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(107, 142, 78) }
    private val amberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(217, 138, 43) }
    private val dangerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(200, 75, 49) }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(238, 241, 245); typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER
    }
    private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(152, 162, 179); typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.LEFT
    }
    private val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(230, 238, 241, 245); strokeWidth = 3f }

    private var joyPointerId = -1
    private var lookPointerId = -1
    private var firePointerId = -1
    private var jetPointerId = -1
    private var lastLookX = 0f; private var lastLookY = 0f
    private var joyKnobX = 0f; private var joyKnobY = 0f

    // Layout rects recomputed on size
    private var joyCx = 0f; private var joyCy = 0f; private var joyR = 110f
    private val fireRect = RectF()
    private val adsRect = RectF()
    private val jetRect = RectF()
    private val jumpRect = RectF()
    private val fragRect = RectF()
    private val reloadRect = RectF()
    private val pauseRect = RectF()
    private val muteRect = RectF()
    private val playBtnRect = RectF()
    private val weaponRects = Array(4) { RectF() }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val u = h / 10f
        joyR = u * 1.55f
        joyCx = u * 2.2f
        joyCy = h - u * 2.3f

        fireRect.set(w - u * 2.6f, h - u * 3.1f, w - u * 0.7f, h - u * 1.2f)
        jetRect.set(w - u * 4.5f, h - u * 2.3f, w - u * 2.9f, h - u * 0.7f)
        jumpRect.set(w - u * 4.5f, h - u * 4.1f, w - u * 2.9f, h - u * 2.6f)
        adsRect.set(w - u * 2.4f, h - u * 4.9f, w - u * 0.9f, h - u * 3.4f)
        fragRect.set(w - u * 6.2f, h - u * 2.1f, w - u * 4.8f, h - u * 0.8f)
        reloadRect.set(w - u * 6.2f, h - u * 3.7f, w - u * 4.8f, h - u * 2.4f)

        pauseRect.set(w - u * 1.8f, u * 0.35f, w - u * 0.35f, u * 1.15f)
        muteRect.set(w - u * 3.45f, u * 0.35f, w - u * 1.95f, u * 1.15f)

        val slotW = u * 1.45f
        val totalW = slotW * 4 + u * 0.45f
        val startX = (w - totalW) * 0.5f
        for (i in 0..3) {
            val sx = startX + i * (slotW + u * 0.15f)
            weaponRects[i].set(sx, h - u * 1.25f, sx + slotW, h - u * 0.25f)
        }
        playBtnRect.set(w * 0.5f - u * 3.2f, h * 0.58f, w * 0.5f + u * 3.2f, h * 0.72f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat(); val u = h / 10f

        if (engine.state != "playing") {
            drawOverlay(canvas, w, h, u)
            return
        }

        // Damage red edge vignette
        if (engine.damageFlash > 0.02f) {
            dangerPaint.alpha = (engine.damageFlash * 110).toInt()
            canvas.drawRect(0f, 0f, w, h, dangerPaint)
            dangerPaint.alpha = 255
        }

        // Top-left Radar + HP/JET bars
        val radR = u * 1.05f
        val radCx = u * 1.35f; val radCy = u * 1.4f
        canvas.drawCircle(radCx, radCy, radR, bgPaint)
        canvas.drawCircle(radCx, radCy, radR, borderPaint)
        // Player dot
        canvas.drawCircle(radCx, radCy, 6f, olivePaint)
        synchronized(engine) {
            val cy = cos(engine.yaw); val sy = sin(engine.yaw)
            for (b in engine.bots) {
                val dx = b.x - engine.px; val dz = b.z - engine.pz
                val rx = (dx * cy + dz * sy) / 28f * radR
                val ry = (-dx * sy + dz * cy) / 28f * radR
                val d = hypot(rx, ry)
                val sc = if (d > radR - 7f) (radR - 7f) / d else 1f
                canvas.drawCircle(radCx + rx * sc, radCy + ry * sc, if (b.isSarge) 7f else 5f, if (b.isSarge) amberPaint else dangerPaint)
            }
        }

        // Vitals panel beside radar
        val vp = RectF(u * 2.65f, u * 0.35f, u * 6.5f, u * 1.65f)
        canvas.drawRoundRect(vp, 18f, 18f, bgPaint)
        canvas.drawRoundRect(vp, 18f, 18f, borderPaint)
        mutedPaint.textSize = u * 0.28f
        canvas.drawText("HP ${engine.hp}", vp.left + 18f, vp.top + u * 0.42f, mutedPaint)
        val hpTrack = RectF(vp.left + u * 1.15f, vp.top + u * 0.22f, vp.right - 18f, vp.top + u * 0.46f)
        canvas.drawRoundRect(hpTrack, 8f, 8f, borderPaint)
        canvas.drawRoundRect(RectF(hpTrack.left, hpTrack.top, hpTrack.left + hpTrack.width() * (engine.hp / 100f), hpTrack.bottom), 8f, 8f, olivePaint)

        canvas.drawText("JET ${engine.fuel.toInt()}", vp.left + 18f, vp.top + u * 0.98f, mutedPaint)
        val fuelTrack = RectF(vp.left + u * 1.15f, vp.top + u * 0.78f, vp.right - 18f, vp.top + u * 1.02f)
        canvas.drawRoundRect(fuelTrack, 8f, 8f, borderPaint)
        canvas.drawRoundRect(RectF(fuelTrack.left, fuelTrack.top, fuelTrack.left + fuelTrack.width() * (engine.fuel / 100f), fuelTrack.bottom), 8f, 8f, amberPaint)

        // Score & Wave center-top
        val sp = RectF(w * 0.5f - u * 2.0f, u * 0.35f, w * 0.5f + u * 2.0f, u * 1.15f)
        canvas.drawRoundRect(sp, 18f, 18f, bgPaint)
        canvas.drawRoundRect(sp, 18f, 18f, borderPaint)
        textPaint.textSize = u * 0.34f
        canvas.drawText("WAVE ${engine.wave}  •  ${engine.kills} KILLS", sp.centerX(), sp.centerY() + u * 0.12f, textPaint)

        if (engine.feedTimer > 0f) {
            textPaint.textSize = u * 0.28f
            canvas.drawText(engine.feedMsg, w * 0.5f, u * 1.65f, textPaint)
        }

        // Top-right buttons
        drawButton(canvas, muteRect, if (engine.sfx.muted) "SFX OFF" else "SFX ON", false, u * 0.26f)
        drawButton(canvas, pauseRect, "PAUSE", false, u * 0.26f)

        // Dynamic COD Crosshair + Hitmarker
        val cx = w * 0.5f; val cy = h * 0.5f
        val gap = (14f + engine.recoilKick * 28f) * (1f - engine.adsBlend * 0.55f)
        val arm = 16f
        canvas.drawLine(cx - gap - arm, cy, cx - gap, cy, crossPaint)
        canvas.drawLine(cx + gap, cy, cx + gap + arm, cy, crossPaint)
        canvas.drawLine(cx, cy - gap - arm, cx, cy - gap, crossPaint)
        canvas.drawLine(cx, cy + gap, cx, cy + gap + arm, crossPaint)
        if (engine.hitMarkerTimer > 0f) {
            val hm = 14f
            amberPaint.strokeWidth = 4f
            canvas.drawLine(cx - hm, cy - hm, cx - 5f, cy - 5f, amberPaint)
            canvas.drawLine(cx + hm, cy - hm, cx + 5f, cy - 5f, amberPaint)
            canvas.drawLine(cx - hm, cy + hm, cx - 5f, cy + 5f, amberPaint)
            canvas.drawLine(cx + hm, cy + hm, cx + 5f, cy + 5f, amberPaint)
        }

        // Left Move Thumbstick
        canvas.drawCircle(joyCx, joyCy, joyR, bgPaint)
        canvas.drawCircle(joyCx, joyCy, joyR, borderPaint)
        canvas.drawCircle(joyCx + joyKnobX, joyCy + joyKnobY, joyR * 0.38f, borderPaint)
        canvas.drawCircle(joyCx + joyKnobX, joyCy + joyKnobY, joyR * 0.36f, bgPaint)

        // Action buttons
        drawButton(canvas, fireRect, "FIRE", engine.inputFire, u * 0.36f, true)
        drawButton(canvas, adsRect, "ADS", engine.adsTarget, u * 0.28f)
        drawButton(canvas, jetRect, "JET", engine.inputJet, u * 0.28f)
        drawButton(canvas, jumpRect, "JUMP", false, u * 0.26f)
        drawButton(canvas, fragRect, "FRAG ${engine.frags}", false, u * 0.24f)
        drawButton(canvas, reloadRect, if (engine.reloadTimer > 0f) "RLD..." else "RLD", false, u * 0.26f)

        // Bottom Weapon Dock
        for (i in 0..3) {
            val wSpec = engine.weapons[i]
            val active = engine.weaponIdx == i
            val ammoTxt = if (active && engine.reloadTimer > 0f) "..." else "${engine.ammo[i]}"
            drawButton(canvas, weaponRects[i], "${wSpec.label} $ammoTxt", active, u * 0.26f)
        }
    }

    private fun drawButton(canvas: Canvas, r: RectF, label: String, active: Boolean, ts: Float, isPrimary: Boolean = false) {
        val fill = if (active) (if (isPrimary) amberPaint else olivePaint) else bgPaint
        canvas.drawRoundRect(r, 24f, 24f, fill)
        canvas.drawRoundRect(r, 24f, 24f, borderPaint)
        textPaint.textSize = ts
        canvas.drawText(label, r.centerX(), r.centerY() + ts * 0.35f, textPaint)
    }

    private fun drawOverlay(canvas: Canvas, w: Float, h: Float, u: Float) {
        canvas.drawColor(Color.argb(185, 12, 15, 20))
        val card = RectF(w * 0.5f - u * 4.4f, h * 0.16f, w * 0.5f + u * 4.4f, h * 0.84f)
        canvas.drawRoundRect(card, 32f, 32f, bgPaint)
        canvas.drawRoundRect(card, 32f, 32f, borderPaint)

        textPaint.textSize = u * 0.62f
        val title = when (engine.state) {
            "paused" -> "GAME PAUSED"
            "gameover" -> "MISSION FAILED"
            else -> "MINI MILITIA 3D"
        }
        canvas.drawText(title, w * 0.5f, h * 0.32f, textPaint)

        textPaint.textSize = u * 0.30f
        val sub = when (engine.state) {
            "gameover" -> "Wave ${engine.wave}  •  ${engine.kills} Kills"
            "paused" -> "Wave ${engine.wave}  •  ${engine.kills} Kills"
            else -> "COD-Style 3D FPS • Drag Right Side to Aim • Left Stick to Move"
        }
        canvas.drawText(sub, w * 0.5f, h * 0.44f, textPaint)

        canvas.drawRoundRect(playBtnRect, 22f, 22f, olivePaint)
        textPaint.textSize = u * 0.38f
        val btnLbl = if (engine.state == "paused") "RESUME" else "DEPLOY TO OUTPOST"
        canvas.drawText(btnLbl, playBtnRect.centerX(), playBtnRect.centerY() + u * 0.13f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val idx = event.actionIndex
        val pid = event.getPointerId(idx)
        val x = event.getX(idx)
        val y = event.getY(idx)

        if (engine.state != "playing") {
            if (action == MotionEvent.ACTION_DOWN && playBtnRect.contains(x, y)) {
                if (engine.state == "paused") engine.state = "playing"
                else engine.startMatch()
                invalidate()
            }
            return true
        }

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                when {
                    pauseRect.contains(x, y) -> engine.state = "paused"
                    muteRect.contains(x, y) -> engine.sfx.muted = !engine.sfx.muted
                    fireRect.contains(x, y) -> { firePointerId = pid; engine.inputFire = true }
                    jetRect.contains(x, y) -> { jetPointerId = pid; engine.inputJet = true }
                    jumpRect.contains(x, y) -> engine.inputJump = true
                    adsRect.contains(x, y) -> engine.adsTarget = !engine.adsTarget
                    fragRect.contains(x, y) -> engine.throwFrag()
                    reloadRect.contains(x, y) -> engine.triggerReload()
                    weaponRects.indexOfFirst { it.contains(x, y) } >= 0 -> {
                        engine.selectWeapon(weaponRects.indexOfFirst { it.contains(x, y) })
                    }
                    hypot(x - joyCx, y - joyCy) < joyR * 1.4f && joyPointerId == -1 -> {
                        joyPointerId = pid
                        updateJoy(x, y)
                    }
                    lookPointerId == -1 -> {
                        lookPointerId = pid
                        lastLookX = x; lastLookY = y
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    val px = event.getX(i); val py = event.getY(i)
                    if (id == joyPointerId) updateJoy(px, py)
                    else if (id == lookPointerId) {
                        engine.addLookDelta(px - lastLookX, py - lastLookY)
                        lastLookX = px; lastLookY = py
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (pid == joyPointerId) {
                    joyPointerId = -1; joyKnobX = 0f; joyKnobY = 0f
                    engine.moveForward = 0f; engine.moveStrafe = 0f
                }
                if (pid == lookPointerId) lookPointerId = -1
                if (pid == firePointerId) { firePointerId = -1; engine.inputFire = false }
                if (pid == jetPointerId) { jetPointerId = -1; engine.inputJet = false }
            }
        }
        return true
    }

    private fun updateJoy(x: Float, y: Float) {
        var dx = x - joyCx; var dy = y - joyCy
        val d = hypot(dx, dy)
        if (d > joyR) { dx = dx / d * joyR; dy = dy / d * joyR }
        joyKnobX = dx; joyKnobY = dy
        engine.moveStrafe = dx / joyR
        engine.moveForward = -dy / joyR
    }
}
