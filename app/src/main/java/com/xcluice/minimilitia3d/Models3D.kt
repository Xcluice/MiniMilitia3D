package com.xcluice.minimilitia3d

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Models3D {
    val noobie = buildSoldier(0)
    val sarge = buildSoldier(1)
    val enemy = buildSoldier(2)

    val uzi = buildUzi()
    val magnum = buildMagnum()
    val shotgun = buildShotgun()
    val rpg = buildRpg()
    val grenade = buildGrenade()

    val island = buildIsland()
    val bridge = buildBridge()
    val bush = buildBush()
    val explosion = buildExplosion()

    val bullet = MeshBuilder().apply { addSphere(0.10f, 0.10f, 0.18f, segU = 8, segV = 6, r = 1f, g = 0.92f, b = 0.55f) }.build()
    val rocket = MeshBuilder().apply {
        addCylinder(0.08f, 0.10f, 0.55f, rotX = (PI / 2).toFloat(), seg = 8, r = 0.25f, g = 0.42f, b = 0.22f)
        addCylinder(0.01f, 0.09f, 0.22f, pz = -0.35f, rotX = -(PI / 2).toFloat(), seg = 8, r = 0.95f, g = 0.65f, b = 0.15f)
    }.build()
    val medkit = MeshBuilder().apply {
        addBox(0.65f, 0.45f, 0.45f, r = 0.92f, g = 0.93f, b = 0.95f)
        addBox(0.32f, 0.12f, 0.48f, r = 0.35f, g = 0.62f, b = 0.28f)
        addBox(0.12f, 0.32f, 0.48f, r = 0.35f, g = 0.62f, b = 0.28f)
    }.build()
    val ammoCrate = MeshBuilder().apply {
        addBox(0.65f, 0.42f, 0.45f, r = 0.32f, g = 0.38f, b = 0.24f)
        addBox(0.68f, 0.10f, 0.48f, py = 0.16f, r = 0.85f, g = 0.55f, b = 0.18f)
    }.build()
    val hpBar = MeshBuilder().apply { addBox(1.0f, 0.12f, 0.04f, r = 0.85f, g = 0.32f, b = 0.22f) }.build()
    val muzzleFlash = MeshBuilder().apply {
        addSphere(0.16f, 0.16f, 0.26f, segU = 8, segV = 6, r = 1.0f, g = 0.85f, b = 0.35f)
    }.build()

    // 0 = Noobie, 1 = Sarge, 2 = Enemy Soldier
    private fun buildSoldier(variant: Int): Mesh3D {
        val m = MeshBuilder()
        val hr = if (variant == 0) 0.18f else if (variant == 1) 0.38f else 0.16f
        val hg = if (variant == 0) 0.40f else if (variant == 1) 0.28f else 0.19f
        val hb = if (variant == 0) 0.14f else if (variant == 1) 0.17f else 0.24f

        val jr = if (variant == 0) 0.22f else if (variant == 1) 0.64f else 0.18f
        val jg = if (variant == 0) 0.36f else if (variant == 1) 0.54f else 0.20f
        val jb = if (variant == 0) 0.18f else if (variant == 1) 0.34f else 0.25f

        val pr = if (variant == 0) 0.15f else if (variant == 1) 0.54f else 0.12f
        val pg = if (variant == 0) 0.25f else if (variant == 1) 0.45f else 0.14f
        val pb = if (variant == 0) 0.13f else if (variant == 1) 0.28f else 0.18f

        // Chibi head (+Z faces forward)
        m.addSphere(0.44f, 0.42f, 0.42f, 0f, 1.42f, 0f, 14, 10, 0.80f, 0.56f, 0.38f)

        // Eyes / Sunglasses
        if (variant == 1) {
            m.addBox(0.18f, 0.11f, 0.08f, -0.15f, 1.44f, 0.40f, r = 0.06f, g = 0.07f, b = 0.09f)
            m.addBox(0.18f, 0.11f, 0.08f,  0.15f, 1.44f, 0.40f, r = 0.06f, g = 0.07f, b = 0.09f)
            m.addBox(0.12f, 0.03f, 0.05f,  0.00f, 1.46f, 0.42f, r = 0.85f, g = 0.68f, b = 0.25f)
        } else {
            for (sx in floatArrayOf(-1f, 1f)) {
                m.addBox(0.13f, 0.09f, 0.06f, sx * 0.14f, 1.44f, 0.39f, r = 0.95f, g = 0.95f, b = 0.95f)
                m.addBox(0.06f, 0.06f, 0.07f, sx * 0.14f, 1.44f, 0.41f, r = 0.10f, g = 0.12f, b = 0.15f)
                m.addBox(0.18f, 0.055f, 0.07f, sx * 0.15f, 1.53f, 0.40f, rz = -sx * 0.2f, r = 0.22f, g = 0.14f, b = 0.08f)
            }
        }

        // Beard (Noobie & Sarge)
        if (variant == 0 || variant == 1) {
            m.addSphere(0.45f, 0.28f, 0.43f, 0f, 1.40f, 0.01f, 12, 8, 0.22f, 0.14f, 0.08f, (PI * 0.48).toFloat(), PI.toFloat())
        }
        // Gritting teeth
        m.addBox(0.24f, 0.08f, 0.06f, 0f, 1.25f, 0.40f, r = 0.95f, g = 0.95f, b = 0.95f)

        // Headgear
        if (variant == 1) {
            m.addCylinder(0.68f, 0.70f, 0.05f, 0f, 1.70f, 0f, seg = 16, r = hr, g = hg, b = hb)
            m.addCylinder(0.22f, 0.44f, 0.34f, 0f, 1.88f, 0f, seg = 14, r = hr, g = hg, b = hb)
            m.addBox(0.12f, 0.12f, 0.06f, 0f, 1.84f, 0.35f, r = 0.85f, g = 0.68f, b = 0.25f)
        } else {
            m.addSphere(0.50f, 0.42f, 0.50f, 0f, 1.48f, 0f, 16, 10, hr, hg, hb, 0f, (PI * 0.56).toFloat())
            m.addCylinder(0.51f, 0.52f, 0.07f, 0f, 1.44f, 0f, seg = 16, r = hr * 0.85f, g = hg * 0.85f, b = hb * 0.85f)
        }

        // Torso & Uniform
        m.addCylinder(0.32f, 0.36f, 0.64f, 0f, 0.74f, 0f, seg = 14, r = jr, g = jg, b = jb)
        if (variant == 0) {
            // Diagonal brass bandolier
            for (i in 0..6) {
                val t = -0.24f + i * 0.08f
                m.addBox(0.05f, 0.09f, 0.05f, t, 0.74f + t * 0.8f, 0.36f, rz = -0.7f, r = 0.85f, g = 0.68f, b = 0.25f)
            }
        } else if (variant == 1) {
            // Green tie
            m.addBox(0.09f, 0.36f, 0.06f, 0f, 0.80f, 0.34f, r = 0.15f, g = 0.35f, b = 0.16f)
        } else {
            // Dark tactical chest rig
            m.addBox(0.42f, 0.34f, 0.12f, 0f, 0.78f, 0.30f, r = 0.10f, g = 0.12f, b = 0.15f)
        }

        // Belt
        m.addCylinder(0.37f, 0.37f, 0.08f, 0f, 0.45f, 0f, seg = 14, r = 0.18f, g = 0.12f, b = 0.08f)

        // Jetpack on back (-Z)
        m.addBox(0.36f, 0.38f, 0.16f, 0f, 0.78f, -0.36f, r = 0.12f, g = 0.13f, b = 0.15f)
        for (sx in floatArrayOf(-1f, 1f)) {
            m.addCylinder(0.08f, 0.08f, 0.42f, sx * 0.12f, 0.78f, -0.42f, seg = 10, r = 0.68f, g = 0.70f, b = 0.74f)
            m.addCylinder(0.05f, 0.08f, 0.10f, sx * 0.12f, 0.54f, -0.42f, seg = 10, r = 0.95f, g = 0.58f, b = 0.15f)
        }

        // Arms & Boots
        for (sx in floatArrayOf(-1f, 1f)) {
            m.addBox(0.16f, 0.42f, 0.16f, sx * 0.44f, 0.76f, 0.08f, rx = -0.35f, r = jr, g = jg, b = jb)
            m.addBox(0.14f, 0.14f, 0.14f, sx * 0.46f, 0.55f, 0.20f, r = 0.10f, g = 0.11f, b = 0.12f)
            m.addCylinder(0.13f, 0.12f, 0.28f, sx * 0.16f, 0.28f, 0f, seg = 10, r = pr, g = pg, b = pb)
            m.addBox(0.20f, 0.16f, 0.30f, sx * 0.16f, 0.08f, 0.05f, r = 0.10f, g = 0.11f, b = 0.12f)
        }
        return m.build()
    }

    // Weapons point along -Z for natural First-Person COD Viewmodel orientation
    private fun buildUzi(): Mesh3D {
        val m = MeshBuilder()
        m.addBox(0.12f, 0.18f, 0.62f, 0f, 0.05f, -0.10f, r = 0.38f, g = 0.41f, b = 0.45f)
        m.addBox(0.14f, 0.10f, 0.26f, 0f, -0.02f, -0.28f, r = 0.12f, g = 0.13f, b = 0.15f)
        m.addCylinder(0.025f, 0.025f, 0.28f, 0f, 0.06f, -0.52f, rotX = (PI / 2).toFloat(), seg = 10, r = 0.72f, g = 0.75f, b = 0.78f)
        m.addBox(0.09f, 0.26f, 0.11f, 0f, -0.14f, -0.04f, r = 0.12f, g = 0.13f, b = 0.15f)
        m.addBox(0.06f, 0.44f, 0.08f, 0f, -0.32f, -0.04f, r = 0.32f, g = 0.35f, b = 0.38f)
        m.addBox(0.05f, 0.06f, 0.04f, 0f, 0.16f, -0.35f, r = 0.85f, g = 0.55f, b = 0.18f)
        return m.build()
    }

    private fun buildMagnum(): Mesh3D {
        val m = MeshBuilder()
        m.addCylinder(0.045f, 0.045f, 0.62f, 0f, 0.10f, -0.38f, rotX = (PI / 2).toFloat(), seg = 12, r = 0.86f, g = 0.88f, b = 0.92f)
        m.addBox(0.05f, 0.06f, 0.58f, 0f, 0.05f, -0.36f, r = 0.80f, g = 0.82f, b = 0.86f)
        m.addBox(0.08f, 0.19f, 0.32f, 0f, 0.04f, 0.04f, r = 0.82f, g = 0.85f, b = 0.90f)
        m.addCylinder(0.095f, 0.095f, 0.18f, 0f, 0.06f, -0.02f, rotX = (PI / 2).toFloat(), seg = 12, r = 0.75f, g = 0.78f, b = 0.82f)
        m.addBox(0.09f, 0.32f, 0.13f, 0f, -0.16f, 0.22f, rx = 0.35f, r = 0.12f, g = 0.12f, b = 0.13f)
        m.addBox(0.02f, 0.05f, 0.04f, 0f, 0.16f, -0.64f, r = 0.85f, g = 0.55f, b = 0.18f)
        return m.build()
    }

    private fun buildShotgun(): Mesh3D {
        val m = MeshBuilder()
        m.addBox(0.11f, 0.15f, 0.38f, 0f, 0.04f, 0.05f, r = 0.22f, g = 0.24f, b = 0.27f)
        m.addCylinder(0.032f, 0.032f, 0.92f, 0f, 0.08f, -0.52f, rotX = (PI / 2).toFloat(), seg = 10, r = 0.24f, g = 0.26f, b = 0.30f)
        m.addCylinder(0.028f, 0.028f, 0.76f, 0f, 0.02f, -0.44f, rotX = (PI / 2).toFloat(), seg = 10, r = 0.15f, g = 0.16f, b = 0.18f)
        m.addCylinder(0.052f, 0.052f, 0.28f, 0f, 0.02f, -0.36f, rotX = (PI / 2).toFloat(), seg = 10, r = 0.42f, g = 0.24f, b = 0.13f)
        m.addBox(0.09f, 0.15f, 0.42f, 0f, -0.05f, 0.38f, rx = -0.2f, r = 0.42f, g = 0.24f, b = 0.13f)
        return m.build()
    }

    private fun buildRpg(): Mesh3D {
        val m = MeshBuilder()
        m.addCylinder(0.055f, 0.055f, 1.15f, 0f, 0.04f, -0.10f, rotX = (PI / 2).toFloat(), seg = 12, r = 0.52f, g = 0.48f, b = 0.26f)
        m.addCylinder(0.072f, 0.072f, 0.40f, 0f, 0.04f, -0.05f, rotX = (PI / 2).toFloat(), seg = 12, r = 0.44f, g = 0.26f, b = 0.13f)
        m.addCylinder(0.11f, 0.048f, 0.20f, 0f, 0.04f, -0.74f, rotX = -(PI / 2).toFloat(), seg = 12, r = 0.20f, g = 0.34f, b = 0.16f)
        m.addCylinder(0.02f, 0.11f, 0.28f, 0f, 0.04f, -0.96f, rotX = -(PI / 2).toFloat(), seg = 12, r = 0.20f, g = 0.34f, b = 0.16f)
        m.addCylinder(0.01f, 0.025f, 0.07f, 0f, 0.04f, -1.12f, rotX = -(PI / 2).toFloat(), seg = 8, r = 0.85f, g = 0.68f, b = 0.22f)
        return m.build()
    }

    private fun buildGrenade(): Mesh3D {
        val m = MeshBuilder()
        m.addSphere(0.24f, 0.30f, 0.24f, 0f, 0f, 0f, 10, 8, 0.32f, 0.38f, 0.22f)
        m.addCylinder(0.10f, 0.13f, 0.10f, 0f, 0.30f, 0f, seg = 10, r = 0.92f, g = 0.78f, b = 0.14f)
        m.addBox(0.06f, 0.28f, 0.08f, 0.18f, 0.18f, 0f, rz = 0.2f, r = 0.72f, g = 0.75f, b = 0.78f)
        return m.build()
    }

    private fun buildIsland(): Mesh3D {
        val m = MeshBuilder()
        // Tapered rocky cliff underside (unit radius ~1.0, top at y = 0.5)
        m.addCylinder(1.02f, 0.22f, 1.50f, 0f, -0.35f, 0f, seg = 10, r = 0.54f, g = 0.42f, b = 0.31f)
        // Grassy top plateau cap
        m.addCylinder(1.00f, 1.05f, 0.24f, 0f, 0.42f, 0f, seg = 10, r = 0.26f, g = 0.48f, b = 0.18f)
        // Boulders on top
        m.addSphere(0.18f, 0.12f, 0.16f, -0.52f, 0.58f, 0.22f, 8, 6, 0.42f, 0.40f, 0.38f)
        m.addSphere(0.14f, 0.10f, 0.14f, 0.48f, 0.56f, -0.25f, 8, 6, 0.42f, 0.40f, 0.38f)
        return m.build()
    }

    private fun buildBridge(): Mesh3D {
        val m = MeshBuilder()
        m.addBox(1.0f, 0.22f, 1.0f, 0f, 0f, 0f, r = 0.42f, g = 0.31f, b = 0.20f)
        m.addBox(1.0f, 0.08f, 0.10f, 0f, 0.14f,  0.44f, r = 0.24f, g = 0.26f, b = 0.29f)
        m.addBox(1.0f, 0.08f, 0.10f, 0f, 0.14f, -0.44f, r = 0.24f, g = 0.26f, b = 0.29f)
        return m.build()
    }

    private fun buildBush(): Mesh3D {
        val m = MeshBuilder()
        for (i in 0 until 7) {
            val a = (2.0 * PI * i / 7.0).toFloat()
            m.addBox(0.55f, 0.06f, 0.36f, cos(a) * 0.38f, 0.36f, sin(a) * 0.38f, rx = 0.25f, ry = -a, rz = -0.35f, r = 0.17f, g = 0.46f, b = 0.16f)
        }
        return m.build()
    }

    private fun buildExplosion(): Mesh3D {
        val m = MeshBuilder()
        m.addSphere(0.40f, 0.40f, 0.40f, segU = 10, segV = 8, r = 1.0f, g = 0.94f, b = 0.55f)
        for (i in 0 until 18) {
            val y = 1f - (i / 17f) * 2f
            val rad = sqrt((1f - y * y).coerceAtLeast(0f))
            val th = i * 2.39996f
            val dx = cos(th) * rad; val dz = sin(th) * rad
            val len = 0.75f + (i % 3) * 0.22f
            val isOuter = i % 3 == 0
            m.addBox(
                0.22f, len, 0.22f,
                dx * len * 0.45f, y * len * 0.45f, dz * len * 0.45f,
                rx = dz * 1.1f, ry = th, rz = -dx * 1.1f,
                r = if (isOuter) 0.92f else 1.0f,
                g = if (isOuter) 0.22f else 0.54f,
                b = 0.06f
            )
        }
        return m.build()
    }
}
