package com.xcluice.minimilitia3d

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class Platform3D(
    val x: Float, val y: Float, val z: Float,
    val hw: Float, val hh: Float, val hd: Float,
    val isBridge: Boolean = false,
    val bushes: List<Pair<Float, Float>> = emptyList()
)

data class WeaponSpec(
    val id: String, val label: String, val mag: Int,
    val fireRate: Float, val speed: Float, val damage: Int,
    val pellets: Int, val spread: Float, val recoil: Float,
    val reloadTime: Float, val explosive: Boolean = false
)

class Bot3D(
    val isSarge: Boolean,
    var x: Float, var y: Float, var z: Float
) {
    var vx = 0f; var vy = 0f; var vz = 0f
    var yaw = 0f
    var hp = if (isSarge) 140 else 100
    val maxHp = hp
    var fuel = 100f
    var grounded = false
    var aiTimer = 0f
    var fireCd = 0.8f
    var strafeDir = if (Random.nextBoolean()) 1f else -1f
    var jetOn = false
}

class Projectile3D(
    var x: Float, var y: Float, var z: Float,
    var vx: Float, var vy: Float, var vz: Float,
    val damage: Int, val fromPlayer: Boolean,
    val explosive: Boolean, var life: Float
)

class Grenade3D(
    var x: Float, var y: Float, var z: Float,
    var vx: Float, var vy: Float, var vz: Float,
    var fuse: Float = 2.0f, var rot: Float = 0f
)

class Explosion3D(
    val x: Float, val y: Float, val z: Float,
    val maxScale: Float, var life: Float = 0.45f, val maxLife: Float = 0.45f
)

class Pickup3D(
    val x: Float, val y: Float, val z: Float,
    val isMed: Boolean, var age: Float = 0f
)

class GameEngine(val sfx: SfxEngine) {
    val weapons = listOf(
        WeaponSpec("uzi", "UZI", 32, 0.10f, 52f, 16, 1, 0.035f, 0.18f, 1.2f),
        WeaponSpec("magnum", "MAG", 6, 0.38f, 68f, 44, 1, 0.012f, 0.42f, 1.3f),
        WeaponSpec("shotgun", "SHT", 8, 0.65f, 46f, 15, 6, 0.11f, 0.58f, 1.5f),
        WeaponSpec("rpg", "RPG", 3, 1.15f, 32f, 95, 1, 0.01f, 0.72f, 2.0f, explosive = true)
    )

    // 3D Floating Outpost Islands + Sky Bridges (walk & jetpack in full 3D!)
    val platforms = listOf(
        // Central spawn plateau
        Platform3D(0f, 0f, 0f, 7.0f, 0.6f, 7.0f, false, listOf(-3.8f to -3.2f, 3.6f to 3.4f)),
        // 4 outer floating cliffs
        Platform3D(0f, 1.2f, -18f, 6.2f, 0.6f, 6.2f, false, listOf(-2.8f to -2.2f)),
        Platform3D(0f, 1.2f,  18f, 6.2f, 0.6f, 6.2f, false, listOf(2.8f to 2.4f)),
        Platform3D(-18f, 2.5f, 0f, 5.8f, 0.6f, 5.8f, false, listOf(-2.4f to 2.2f)),
        Platform3D( 18f, 2.5f, 0f, 5.8f, 0.6f, 5.8f, false, listOf(2.4f to -2.2f)),
        // High central sniper cliff (jetpack up!)
        Platform3D(0f, 7.2f, 0f, 4.4f, 0.55f, 4.4f, false, listOf(1.8f to -1.6f)),
        // Sky bridges connecting central island to outer cliffs
        Platform3D(0f, 0.7f, -9.5f, 2.2f, 0.25f, 4.0f, true),
        Platform3D(0f, 0.7f,  9.5f, 2.2f, 0.25f, 4.0f, true),
        Platform3D(-9.5f, 1.3f, 0f, 4.0f, 0.25f, 2.2f, true),
        Platform3D( 9.5f, 1.3f, 0f, 4.0f, 0.25f, 2.2f, true)
    )

    @Volatile var state = "menu" // menu | playing | paused | gameover
    @Volatile var px = 0f; @Volatile var py = 1.0f; @Volatile var pz = 2.0f
    @Volatile var vx = 0f; @Volatile var vy = 0f; @Volatile var vz = 0f
    @Volatile var yaw = 0f   // radians (0 looks along -Z)
    @Volatile var pitch = 0f // radians (-1.25..1.25)
    @Volatile var hp = 100
    val maxHp = 100
    @Volatile var fuel = 100f
    @Volatile var grounded = false
    @Volatile var weaponIdx = 0
    val ammo = IntArray(4) { weapons[it].mag }
    @Volatile var fireCd = 0f
    @Volatile var reloadTimer = 0f
    @Volatile var frags = 3
    @Volatile var kills = 0
    @Volatile var wave = 1

    // COD FPS Feel state
    @Volatile var adsTarget = false
    @Volatile var adsBlend = 0f
    @Volatile var recoilKick = 0f
    @Volatile var walkBob = 0f
    @Volatile var hitMarkerTimer = 0f
    @Volatile var damageFlash = 0f
    @Volatile var muzzleFlash = 0f
    @Volatile var camShake = 0f
    @Volatile var feedMsg = "OUTPOST 3D READY"
    @Volatile var feedTimer = 0f

    // Controls from HUD
    @Volatile var moveForward = 0f // -1..1
    @Volatile var moveStrafe = 0f  // -1..1
    @Volatile var inputFire = false
    @Volatile var inputJet = false
    @Volatile var inputJump = false

    val bots = ArrayList<Bot3D>()
    val projectiles = ArrayList<Projectile3D>()
    val grenades = ArrayList<Grenade3D>()
    val explosions = ArrayList<Explosion3D>()
    val pickups = ArrayList<Pickup3D>()
    private var spawnTimer = 0.8f

    @Synchronized
    fun startMatch() {
        px = 0f; py = 1.2f; pz = 3.0f
        vx = 0f; vy = 0f; vz = 0f
        yaw = 0f; pitch = 0f
        hp = maxHp; fuel = 100f; frags = 3
        kills = 0; wave = 1; weaponIdx = 0
        for (i in 0..3) ammo[i] = weapons[i].mag
        fireCd = 0f; reloadTimer = 0f; adsTarget = false; adsBlend = 0f
        hitMarkerTimer = 0f; damageFlash = 0f; camShake = 0f
        bots.clear(); projectiles.clear(); grenades.clear(); explosions.clear(); pickups.clear()
        bots.add(Bot3D(true, 0f, 3.0f, -17f))
        bots.add(Bot3D(false, -17f, 4.0f, 0f))
        bots.add(Bot3D(false, 17f, 4.0f, 0f))
        state = "playing"
        pushFeed("DEPLOYED — ELIMINATE SARGE & ENEMIES")
    }

    private fun pushFeed(msg: String) {
        feedMsg = msg
        feedTimer = 3.2f
    }

    @Synchronized
    fun addLookDelta(dx: Float, dy: Float) {
        if (state != "playing") return
        val sens = if (adsTarget) 0.0028f else 0.0046f
        yaw += dx * sens
        pitch = (pitch - dy * sens).coerceIn(-1.22f, 1.22f)
    }

    @Synchronized
    fun selectWeapon(idx: Int) {
        if (idx in 0..3 && idx != weaponIdx) {
            weaponIdx = idx
            reloadTimer = 0f
            adsTarget = false
        }
    }

    @Synchronized
    fun triggerReload() {
        if (state != "playing") return
        val w = weapons[weaponIdx]
        if (reloadTimer <= 0f && ammo[weaponIdx] < w.mag) {
            reloadTimer = w.reloadTime
            adsTarget = false
        }
    }

    @Synchronized
    fun throwFrag() {
        if (state != "playing" || frags <= 0) return
        frags--
        val dir = lookDir()
        grenades.add(
            Grenade3D(
                px + dir[0] * 0.9f, py + 1.55f + dir[1] * 0.6f, pz + dir[2] * 0.9f,
                dir[0] * 19f + vx * 0.3f, dir[1] * 19f + 5.5f, dir[2] * 19f + vz * 0.3f
            )
        )
        sfx.play("bounce")
    }

    fun lookDir(): FloatArray {
        val cp = cos(pitch)
        return floatArrayOf(sin(yaw) * cp, sin(pitch), -cos(yaw) * cp)
    }

    @Synchronized
    fun step(rawDt: Float) {
        val dt = rawDt.coerceIn(0.001f, 0.045f)
        if (feedTimer > 0f) feedTimer -= dt
        if (state != "playing") return

        // Smooth ADS & decay timers
        val targetA = if (adsTarget) 1f else 0f
        adsBlend += (targetA - adsBlend) * (dt * 12f).coerceAtMost(1f)
        recoilKick = (recoilKick - dt * 4.5f).coerceAtLeast(0f)
        hitMarkerTimer = (hitMarkerTimer - dt).coerceAtLeast(0f)
        damageFlash = (damageFlash - dt * 2.4f).coerceAtLeast(0f)
        muzzleFlash = (muzzleFlash - dt * 14f).coerceAtLeast(0f)
        camShake = (camShake - dt * 2.5f).coerceAtLeast(0f)
        if (fireCd > 0f) fireCd = (fireCd - dt).coerceAtLeast(0f)
        if (reloadTimer > 0f) {
            reloadTimer -= dt
            if (reloadTimer <= 0f) {
                reloadTimer = 0f
                ammo[weaponIdx] = weapons[weaponIdx].mag
            }
        }

        // Player 3D movement relative to camera yaw
        val fx = sin(yaw); val fz = -cos(yaw)
        val rx = cos(yaw); val rz = sin(yaw)
        val speed = if (adsTarget) 5.8f else 9.6f
        val wishX = (fx * moveForward + rx * moveStrafe) * speed
        val wishZ = (fz * moveForward + rz * moveStrafe) * speed
        val accel = if (grounded) 14f else 6f
        vx += (wishX - vx) * (accel * dt).coerceAtMost(1f)
        vz += (wishZ - vz) * (accel * dt).coerceAtMost(1f)

        val horizSpeed = hypot(vx, vz)
        if (grounded && horizSpeed > 0.6f) walkBob += dt * horizSpeed * 1.15f

        // Jump & 3D Jetpack
        if (inputJump && grounded) {
            vy = 9.8f
            grounded = false
            inputJump = false
        }
        if (inputJet && fuel > 2f) {
            vy = (vy + 38f * dt).coerceAtMost(11.5f)
            fuel = (fuel - 32f * dt).coerceAtLeast(0f)
            grounded = false
        } else {
            fuel = (fuel + (if (grounded) 40f else 15f) * dt).coerceAtMost(100f)
        }

        // Gravity & 3D platform collision
        vy = (vy - 22f * dt).coerceAtLeast(-26f)
        val prevY = py
        px = (px + vx * dt).coerceIn(-30f, 30f)
        pz = (pz + vz * dt).coerceIn(-30f, 30f)
        py += vy * dt
        grounded = false
        for (p in platforms) {
            if (px > p.x - p.hw - 0.35f && px < p.x + p.hw + 0.35f &&
                pz > p.z - p.hd - 0.35f && pz < p.z + p.hd + 0.35f
            ) {
                val top = p.y + p.hh
                if (vy <= 0f && prevY >= top - 0.35f && py <= top) {
                    py = top; vy = 0f; grounded = true
                }
            }
        }
        if (py < -14f) {
            px = 0f; py = 10f; pz = 0f; vy = 0f
            damagePlayer(20)
        }

        // Firing
        if (inputFire) firePlayerWeapon()

        // Spawn waves
        val maxBots = (2 + wave).coerceAtMost(7)
        spawnTimer -= dt
        if (bots.size < maxBots && spawnTimer <= 0f) {
            spawnTimer = 2.6f
            val spots = arrayOf(floatArrayOf(0f, 4f, -18f), floatArrayOf(0f, 4f, 18f), floatArrayOf(-18f, 5f, 0f), floatArrayOf(18f, 5f, 0f))
            val sp = spots[Random.nextInt(spots.size)]
            bots.add(Bot3D(Random.nextFloat() < 0.35f, sp[0], sp[1], sp[2]))
        }

        // Step 3D AI Bots
        for (b in bots) stepBot(b, dt)

        // Step 3D Projectiles
        stepProjectiles(dt)

        // Step 3D Bouncing Grenades
        stepGrenades(dt)

        // Step Explosions & Pickups
        val exIt = explosions.iterator()
        while (exIt.hasNext()) {
            val ex = exIt.next()
            ex.life -= dt
            if (ex.life <= 0f) exIt.remove()
        }
        val pkIt = pickups.iterator()
        while (pkIt.hasNext()) {
            val pk = pkIt.next()
            pk.age += dt
            val d = sqrt((pk.x - px) * (pk.x - px) + (pk.y - (py + 0.9f)) * (pk.y - (py + 0.9f)) + (pk.z - pz) * (pk.z - pz))
            if (d < 1.8f) {
                sfx.play("pickup")
                if (pk.isMed) {
                    hp = (hp + 35).coerceAtMost(maxHp)
                    pushFeed("+35 HP MEDKIT")
                } else {
                    frags = (frags + 2).coerceAtMost(6)
                    for (i in 0..3) ammo[i] = weapons[i].mag
                    pushFeed("+2 FRAGS & FULL AMMO")
                }
                pkIt.remove()
            }
        }
    }

    private fun firePlayerWeapon() {
        val w = weapons[weaponIdx]
        if (fireCd > 0f || reloadTimer > 0f) return
        if (ammo[weaponIdx] <= 0) { triggerReload(); return }
        ammo[weaponIdx]--
        fireCd = w.fireRate
        recoilKick = (recoilKick + w.recoil).coerceAtMost(1.0f)
        pitch = (pitch + w.recoil * 0.055f).coerceAtMost(1.22f)
        muzzleFlash = 1f
        sfx.play(w.id)

        val dir = lookDir()
        // Physical recoil push
        vx -= dir[0] * w.recoil * 2.2f
        vz -= dir[2] * w.recoil * 2.2f

        val eyeY = py + 1.62f
        for (i in 0 until w.pellets) {
            val sp = if (adsTarget) w.spread * 0.45f else w.spread
            val dx = dir[0] + (Random.nextFloat() - 0.5f) * sp * 2f
            val dy = dir[1] + (Random.nextFloat() - 0.5f) * sp * 2f
            val dz = dir[2] + (Random.nextFloat() - 0.5f) * sp * 2f
            val len = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(1e-4f)
            projectiles.add(
                Projectile3D(
                    px + dir[0] * 0.8f, eyeY + dir[1] * 0.5f, pz + dir[2] * 0.8f,
                    (dx / len) * w.speed, (dy / len) * w.speed, (dz / len) * w.speed,
                    w.damage, true, w.explosive, if (w.explosive) 2.5f else 1.2f
                )
            )
        }
        if (ammo[weaponIdx] == 0) triggerReload()
    }

    private fun stepBot(b: Bot3D, dt: Float) {
        val dx = px - b.x; val dy = (py + 1.0f) - (b.y + 1.0f); val dz = pz - b.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(0.1f)
        b.yaw = atan2(dx, dz)

        b.aiTimer -= dt
        if (b.aiTimer <= 0f) {
            b.aiTimer = 0.6f + Random.nextFloat() * 0.7f
            b.strafeDir = if (Random.nextBoolean()) 1f else -1f
            b.jetOn = (b.y < py - 0.8f || b.y < 0.2f) && b.fuel > 25f
        }
        if (b.y < -1.5f) b.jetOn = true

        val chase = if (dist > 11f) 1f else if (dist < 6f) -0.5f else 0f
        val nx = dx / dist; val nz = dz / dist
        val tx = (nx * chase - nz * b.strafeDir * 0.6f) * 5.6f
        val tz = (nz * chase + nx * b.strafeDir * 0.6f) * 5.6f
        b.vx += (tx - b.vx) * (4f * dt).coerceAtMost(1f)
        b.vz += (tz - b.vz) * (4f * dt).coerceAtMost(1f)

        if (b.jetOn && b.fuel > 2f) {
            b.vy = (b.vy + 32f * dt).coerceAtMost(8.5f)
            b.fuel = (b.fuel - 28f * dt).coerceAtLeast(0f)
        } else {
            b.fuel = (b.fuel + 24f * dt).coerceAtMost(100f)
        }
        b.vy = (b.vy - 20f * dt).coerceAtLeast(-22f)
        val prevY = b.y
        b.x = (b.x + b.vx * dt).coerceIn(-26f, 26f)
        b.z = (b.z + b.vz * dt).coerceIn(-26f, 26f)
        b.y += b.vy * dt
        b.grounded = false
        for (p in platforms) {
            if (b.x > p.x - p.hw && b.x < p.x + p.hw && b.z > p.z - p.hd && b.z < p.z + p.hd) {
                val top = p.y + p.hh
                if (b.vy <= 0f && prevY >= top - 0.35f && b.y <= top) {
                    b.y = top; b.vy = 0f; b.grounded = true
                }
            }
        }
        if (b.y < -12f) { b.x = 0f; b.y = 10f; b.z = -16f; b.vy = 0f }

        b.fireCd -= dt
        if (dist < 26f && b.fireCd <= 0f) {
            b.fireCd = (1.35f - wave * 0.08f).coerceAtLeast(0.55f) + Random.nextFloat() * 0.5f
            val spd = 28f
            val err = 0.07f
            projectiles.add(
                Projectile3D(
                    b.x, b.y + 1.25f, b.z,
                    (dx / dist + (Random.nextFloat() - 0.5f) * err) * spd,
                    (dy / dist + (Random.nextFloat() - 0.5f) * err) * spd,
                    (dz / dist + (Random.nextFloat() - 0.5f) * err) * spd,
                    if (b.isSarge) 11 else 7, false, false, 1.5f
                )
            )
            sfx.play("uzi")
        }
    }

    private fun stepProjectiles(dt: Float) {
        val it = projectiles.iterator()
        while (it.hasNext()) {
            val pr = it.next()
            pr.life -= dt
            if (pr.explosive) pr.vy -= 3.5f * dt
            pr.x += pr.vx * dt; pr.y += pr.vy * dt; pr.z += pr.vz * dt

            var hit = pr.life <= 0f
            if (!hit) {
                for (p in platforms) {
                    if (pr.x > p.x - p.hw && pr.x < p.x + p.hw &&
                        pr.y > p.y - p.hh && pr.y < p.y + p.hh &&
                        pr.z > p.z - p.hd && pr.z < p.z + p.hd
                    ) { hit = true; break }
                }
            }
            if (!hit && pr.fromPlayer) {
                for (b in bots) {
                    val d = hypot(pr.x - b.x, pr.z - b.z)
                    if (d < 0.78f && pr.y > b.y && pr.y < b.y + 2.0f) {
                        hit = true
                        if (!pr.explosive) damageBot(b, pr.damage)
                        break
                    }
                }
            } else if (!hit && !pr.fromPlayer) {
                val d = hypot(pr.x - px, pr.z - pz)
                if (d < 0.7f && pr.y > py && pr.y < py + 1.9f) {
                    hit = true
                    damagePlayer(pr.damage)
                }
            }
            if (hit) {
                if (pr.explosive) explode(pr.x, pr.y, pr.z, 6.2f, pr.damage)
                it.remove()
            }
        }
    }

    private fun stepGrenades(dt: Float) {
        val it = grenades.iterator()
        while (it.hasNext()) {
            val g = it.next()
            g.fuse -= dt
            g.vy -= 22f * dt
            val prevY = g.y
            g.x += g.vx * dt; g.y += g.vy * dt; g.z += g.vz * dt
            g.rot += dt * 7f
            for (p in platforms) {
                if (g.x > p.x - p.hw && g.x < p.x + p.hw && g.z > p.z - p.hd && g.z < p.z + p.hd) {
                    val top = p.y + p.hh + 0.2f
                    if (prevY >= top - 0.3f && g.y <= top && g.vy < 0f) {
                        g.y = top; g.vy = -g.vy * 0.55f; g.vx *= 0.76f; g.vz *= 0.76f
                        if (g.vy > 1.5f) sfx.play("bounce")
                    }
                }
            }
            if (g.fuse <= 0f || g.y < -15f) {
                explode(g.x, g.y, g.z, 7.2f, 110)
                it.remove()
            }
        }
    }

    private fun explode(x: Float, y: Float, z: Float, radius: Float, dmg: Int) {
        sfx.play("explode")
        camShake = (camShake + 0.7f).coerceAtMost(1.2f)
        explosions.add(Explosion3D(x, y, z, radius * 0.55f))

        val pd = sqrt((px - x) * (px - x) + (py + 0.9f - y) * (py + 0.9f - y) + (pz - z) * (pz - z))
        if (pd < radius) {
            val f = 1f - pd / radius
            vx += ((px - x) / pd.coerceAtLeast(0.2f)) * 14f * f
            vy += 7f * f
            vz += ((pz - z) / pd.coerceAtLeast(0.2f)) * 14f * f
            damagePlayer((dmg * f * 0.45f).toInt())
        }
        val snapshot = bots.toList()
        for (b in snapshot) {
            val bd = sqrt((b.x - x) * (b.x - x) + (b.y + 0.9f - y) * (b.y + 0.9f - y) + (b.z - z) * (b.z - z))
            if (bd < radius) {
                val f = 1f - bd / radius
                b.vx += ((b.x - x) / bd.coerceAtLeast(0.2f)) * 16f * f
                b.vy += 8f * f
                b.vz += ((b.z - z) / bd.coerceAtLeast(0.2f)) * 16f * f
                damageBot(b, (dmg * f).toInt())
            }
        }
    }

    private fun damageBot(b: Bot3D, dmg: Int) {
        if (b.hp <= 0) return
        b.hp -= dmg
        hitMarkerTimer = 0.16f
        sfx.play("hit")
        if (b.hp <= 0) {
            kills++
            pickups.add(Pickup3D(b.x, b.y + 0.7f, b.z, Random.nextFloat() < 0.6f))
            bots.remove(b)
            pushFeed("ELIMINATED ${if (b.isSarge) "SARGE" else "ENEMY SOLDIER"}")
            if (kills % 4 == 0) {
                wave++
                pushFeed("WAVE $wave INCOMING")
            }
        }
    }

    private fun damagePlayer(dmg: Int) {
        if (dmg <= 0 || hp <= 0) return
        hp = (hp - dmg).coerceAtLeast(0)
        damageFlash = 1f
        if (hp <= 0) {
            state = "gameover"
            inputFire = false
            inputJet = false
        }
    }
}
