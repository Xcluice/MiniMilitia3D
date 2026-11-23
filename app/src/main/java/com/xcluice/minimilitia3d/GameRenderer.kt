package com.xcluice.minimilitia3d

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameRenderer(private val engine: GameEngine, private val onFrameRendered: () -> Unit) : GLSurfaceView.Renderer {
    private lateinit var models: Models3D
    private var program = 0
    private var posLoc = 0
    private var normLoc = 0
    private var colLoc = 0
    private var mvpLoc = 0
    private var modelLoc = 0
    private var camLoc = 0
    private var unlitLoc = 0

    private val projM = FloatArray(16)
    private val viewM = FloatArray(16)
    private val vpM = FloatArray(16)
    private val modelM = FloatArray(16)
    private val mvpM = FloatArray(16)
    private var aspect = 1.77f
    private var lastNs = System.nanoTime()
    private var menuAngle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.11f, 0.14f, 0.19f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDisable(GLES30.GL_CULL_FACE)

        val vs = """#version 300 es
            layout(location = 0) in vec3 aPos;
            layout(location = 1) in vec3 aNorm;
            layout(location = 2) in vec3 aCol;
            uniform mat4 uMVP;
            uniform mat4 uModel;
            out vec3 vNorm;
            out vec3 vCol;
            out vec3 vWorld;
            void main() {
                vec4 w = uModel * vec4(aPos, 1.0);
                vWorld = w.xyz;
                vNorm = mat3(uModel) * aNorm;
                vCol = aCol;
                gl_Position = uMVP * vec4(aPos, 1.0);
            }
        """.trimIndent()

        val fs = """#version 300 es
            precision mediump float;
            in vec3 vNorm;
            in vec3 vCol;
            in vec3 vWorld;
            uniform vec3 uCamPos;
            uniform float uUnlit;
            out vec4 fragColor;
            void main() {
                if (uUnlit > 0.5) {
                    fragColor = vec4(vCol, 1.0);
                    return;
                }
                vec3 N = normalize(vNorm);
                if (!gl_FrontFacing) N = -N;
                vec3 V = normalize(uCamPos - vWorld);
                vec3 L = normalize(vec3(0.55, 0.85, 0.45));
                vec3 H = normalize(L + V);
                float ndl = max(dot(N, L), 0.0);
                float spec = pow(max(dot(N, H), 0.0), 28.0) * 0.25;
                vec3 hemi = mix(vec3(0.20, 0.18, 0.16), vec3(0.46, 0.52, 0.62), N.y * 0.5 + 0.5);
                vec3 col = vCol * (hemi + vec3(1.0, 0.94, 0.84) * ndl * 0.88) + vec3(spec);
                float dist = length(uCamPos - vWorld);
                float fog = clamp((dist - 22.0) / 68.0, 0.0, 0.82);
                col = mix(col, vec3(0.11, 0.14, 0.19), fog);
                fragColor = vec4(col, 1.0);
            }
        """.trimIndent()

        val vShader = compileShader(GLES30.GL_VERTEX_SHADER, vs)
        val fShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fs)
        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vShader)
        GLES30.glAttachShader(program, fShader)
        GLES30.glLinkProgram(program)

        posLoc = 0; normLoc = 1; colLoc = 2
        mvpLoc = GLES30.glGetUniformLocation(program, "uMVP")
        modelLoc = GLES30.glGetUniformLocation(program, "uModel")
        camLoc = GLES30.glGetUniformLocation(program, "uCamPos")
        unlitLoc = GLES30.glGetUniformLocation(program, "uUnlit")

        models = Models3D()
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        GLES30.glViewport(0, 0, w, h)
        aspect = w.toFloat() / h.coerceAtLeast(1).toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = ((now - lastNs) / 1e9).toFloat().coerceIn(0.001f, 0.05f)
        lastNs = now
        engine.step(dt)

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(program)

        val fov = 68f - engine.adsBlend * 24f
        Matrix.perspectiveM(projM, 0, fov, aspect, 0.1f, 140f)

        val camX: Float; val camY: Float; val camZ: Float
        if (engine.state == "playing" || engine.state == "paused") {
            val shakeX = (Random.nextFloat() - 0.5f) * engine.camShake * 0.25f
            val shakeY = (Random.nextFloat() - 0.5f) * engine.camShake * 0.25f
            val bobY = sin(engine.walkBob) * 0.055f * (1f - engine.adsBlend * 0.7f)
            camX = engine.px + shakeX
            camY = engine.py + 1.65f + bobY + shakeY
            camZ = engine.pz
            val dir = engine.lookDir()
            Matrix.setLookAtM(viewM, 0, camX, camY, camZ, camX + dir[0], camY + dir[1], camZ + dir[2], 0f, 1f, 0f)
        } else {
            menuAngle += dt * 0.22f
            camX = sin(menuAngle) * 28f
            camY = 12f
            camZ = cos(menuAngle) * 28f
            Matrix.setLookAtM(viewM, 0, camX, camY, camZ, 0f, 2.5f, 0f, 0f, 1f, 0f)
        }
        Matrix.multiplyMM(vpM, 0, projM, 0, viewM, 0)
        GLES30.glUniform3f(camLoc, camX, camY, camZ)
        GLES30.glUniform1f(unlitLoc, 0f)

        // 1. Outpost Floating Islands, Bridges & Foliage
        for (p in engine.platforms) {
            if (p.isBridge) {
                drawMesh(models.bridge, p.x, p.y, p.z, 0f, 0f, 0f, p.hw * 2f, p.hh * 2f, p.hd * 2f)
            } else {
                drawMesh(models.island, p.x, p.y - 0.35f, p.z, 0f, 0f, 0f, p.hw, 1.9f, p.hd)
                for ((bx, bz) in p.bushes) {
                    drawMesh(models.bush, p.x + bx, p.y + p.hh, p.z + bz, 0f, bx, 0f, 1.8f, 1.8f, 1.8f)
                }
            }
        }

        // 2. Bots (Sarge & Enemy Soldiers) + Floating HP Bars
        synchronized(engine) {
            for (b in engine.bots) {
                val degY = (b.yaw * 180.0 / PI).toFloat()
                drawMesh(if (b.isSarge) models.sarge else models.enemy, b.x, b.y, b.z, 0f, degY, 0f, 1.0f, 1.0f, 1.0f)
                val ratio = (b.hp.toFloat() / b.maxHp.toFloat()).coerceIn(0.05f, 1f)
                GLES30.glUniform1f(unlitLoc, 1f)
                drawMesh(models.hpBar, b.x, b.y + 2.35f, b.z, 0f, degY, 0f, 1.2f * ratio, 1f, 1f)
                GLES30.glUniform1f(unlitLoc, 0f)
            }
            for (pr in engine.projectiles) {
                GLES30.glUniform1f(unlitLoc, 1f)
                drawMesh(if (pr.explosive) models.rocket else models.bullet, pr.x, pr.y, pr.z, 0f, 0f, 0f, 1f, 1f, 1f)
                GLES30.glUniform1f(unlitLoc, 0f)
            }
            for (g in engine.grenades) {
                drawMesh(models.grenade, g.x, g.y, g.z, g.rot * 40f, g.rot * 25f, 0f, 0.8f, 0.8f, 0.8f)
            }
            for (pk in engine.pickups) {
                drawMesh(if (pk.isMed) models.medkit else models.ammoCrate, pk.x, pk.y + sin(pk.age * 3.5f) * 0.2f, pk.z, 0f, pk.age * 90f, 0f, 1f, 1f, 1f)
            }
            GLES30.glUniform1f(unlitLoc, 1f)
            for (ex in engine.explosions) {
                val t = 1f - ex.life / ex.maxLife
                val sc = (ex.maxScale * sin(t.coerceAtMost(0.8f) * 2.0f) * (1f - t * 0.45f)).coerceAtLeast(0.1f)
                drawMesh(models.explosion, ex.x, ex.y, ex.z, 0f, t * 180f, 0f, sc, sc, sc)
            }
            GLES30.glUniform1f(unlitLoc, 0f)
        }

        // 3. COD First-Person 3D Weapon Viewmodel
        if (engine.state == "playing" || engine.state == "paused") {
            GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)
            Matrix.setIdentityM(viewM, 0)
            Matrix.multiplyMM(vpM, 0, projM, 0, viewM, 0)
            GLES30.glUniform3f(camLoc, 0f, 0f, 0f)

            val wMesh = when (engine.weaponIdx) {
                0 -> models.uzi
                1 -> models.magnum
                2 -> models.shotgun
                else -> models.rpg
            }
            val ads = engine.adsBlend
            val gx = 0.34f * (1f - ads) + sin(engine.walkBob * 0.5f) * 0.015f * (1f - ads)
            val gy = -0.28f * (1f - ads) - 0.16f * ads - (if (engine.reloadTimer > 0f) 0.25f else 0f) + cos(engine.walkBob) * 0.012f * (1f - ads)
            val gz = -0.78f + engine.recoilKick * 0.14f + ads * 0.10f
            val pitchKick = engine.recoilKick * 12f - (if (engine.reloadTimer > 0f) 28f else 0f)
            drawMesh(wMesh, gx, gy, gz, pitchKick, 0f, 0f, 0.85f, 0.85f, 0.85f)

            if (engine.muzzleFlash > 0.1f) {
                GLES30.glUniform1f(unlitLoc, 1f)
                drawMesh(models.muzzleFlash, gx, gy + 0.06f, gz - 0.65f, 0f, 0f, 0f, 0.55f * engine.muzzleFlash, 0.55f * engine.muzzleFlash, 0.55f * engine.muzzleFlash)
                GLES30.glUniform1f(unlitLoc, 0f)
            }
        }

        onFrameRendered()
    }

    private fun drawMesh(mesh: Mesh3D, x: Float, y: Float, z: Float, rx: Float, ry: Float, rz: Float, sx: Float, sy: Float, sz: Float) {
        Matrix.setIdentityM(modelM, 0)
        Matrix.translateM(modelM, 0, x, y, z)
        if (ry != 0f) Matrix.rotateM(modelM, 0, ry, 0f, 1f, 0f)
        if (rx != 0f) Matrix.rotateM(modelM, 0, rx, 1f, 0f, 0f)
        if (rz != 0f) Matrix.rotateM(modelM, 0, rz, 0f, 0f, 1f)
        Matrix.scaleM(modelM, 0, sx, sy, sz)
        Matrix.multiplyMM(mvpM, 0, vpM, 0, modelM, 0)
        GLES30.glUniformMatrix4fv(mvpLoc, 1, false, mvpM, 0)
        GLES30.glUniformMatrix4fv(modelLoc, 1, false, modelM, 0)
        mesh.draw(posLoc, normLoc, colLoc)
    }

    private fun compileShader(type: Int, src: String): Int {
        val sh = GLES30.glCreateShader(type)
        GLES30.glShaderSource(sh, src)
        GLES30.glCompileShader(sh)
        return sh
    }
}
