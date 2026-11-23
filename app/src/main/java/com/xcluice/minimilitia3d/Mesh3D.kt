package com.xcluice.minimilitia3d

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Mesh3D(private val buffer: FloatBuffer, val vertexCount: Int) {
    fun draw(posLoc: Int, normLoc: Int, colLoc: Int) {
        val stride = 9 * 4
        buffer.position(0)
        GLES30.glEnableVertexAttribArray(posLoc)
        GLES30.glVertexAttribPointer(posLoc, 3, GLES30.GL_FLOAT, false, stride, buffer)

        buffer.position(3)
        GLES30.glEnableVertexAttribArray(normLoc)
        GLES30.glVertexAttribPointer(normLoc, 3, GLES30.GL_FLOAT, false, stride, buffer)

        buffer.position(6)
        GLES30.glEnableVertexAttribArray(colLoc)
        GLES30.glVertexAttribPointer(colLoc, 3, GLES30.GL_FLOAT, false, stride, buffer)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
    }
}

class MeshBuilder {
    private val data = ArrayList<Float>(4096)

    private fun pushVert(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float, r: Float, g: Float, b: Float) {
        data.add(x); data.add(y); data.add(z)
        data.add(nx); data.add(ny); data.add(nz)
        data.add(r); data.add(g); data.add(b)
    }

    private fun rotPoint(x: Float, y: Float, z: Float, rx: Float, ry: Float, rz: Float): FloatArray {
        var px = x; var py = y; var pz = z
        if (rx != 0f) {
            val c = cos(rx); val s = sin(rx)
            val ny = py * c - pz * s; val nz = py * s + pz * c
            py = ny; pz = nz
        }
        if (ry != 0f) {
            val c = cos(ry); val s = sin(ry)
            val nx = px * c + pz * s; val nz = -px * s + pz * c
            px = nx; pz = nz
        }
        if (rz != 0f) {
            val c = cos(rz); val s = sin(rz)
            val nx = px * c - py * s; val ny = px * s + py * c
            px = nx; py = ny
        }
        return floatArrayOf(px, py, pz)
    }

    fun addTri(
        a: FloatArray, b: FloatArray, c: FloatArray,
        r: Float, g: Float, bl: Float
    ) {
        val e1x = b[0] - a[0]; val e1y = b[1] - a[1]; val e1z = b[2] - a[2]
        val e2x = c[0] - a[0]; val e2y = c[1] - a[1]; val e2z = c[2] - a[2]
        var nx = e1y * e2z - e1z * e2y
        var ny = e1z * e2x - e1x * e2z
        var nz = e1x * e2y - e1y * e2x
        val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-6f)
        nx /= len; ny /= len; nz /= len
        pushVert(a[0], a[1], a[2], nx, ny, nz, r, g, bl)
        pushVert(b[0], b[1], b[2], nx, ny, nz, r, g, bl)
        pushVert(c[0], c[1], c[2], nx, ny, nz, r, g, bl)
    }

    fun addBox(
        sx: Float, sy: Float, sz: Float,
        px: Float = 0f, py: Float = 0f, pz: Float = 0f,
        rx: Float = 0f, ry: Float = 0f, rz: Float = 0f,
        r: Float, g: Float, b: Float
    ) {
        val hx = sx * 0.5f; val hy = sy * 0.5f; val hz = sz * 0.5f
        val corners = arrayOf(
            floatArrayOf(-hx, -hy,  hz), floatArrayOf( hx, -hy,  hz),
            floatArrayOf( hx,  hy,  hz), floatArrayOf(-hx,  hy,  hz),
            floatArrayOf(-hx, -hy, -hz), floatArrayOf( hx, -hy, -hz),
            floatArrayOf( hx,  hy, -hz), floatArrayOf(-hx,  hy, -hz)
        ).map {
            val pt = rotPoint(it[0], it[1], it[2], rx, ry, rz)
            floatArrayOf(pt[0] + px, pt[1] + py, pt[2] + pz)
        }
        val quads = arrayOf(
            intArrayOf(0, 1, 2, 3), intArrayOf(5, 4, 7, 6),
            intArrayOf(4, 0, 3, 7), intArrayOf(1, 5, 6, 2),
            intArrayOf(3, 2, 6, 7), intArrayOf(4, 5, 1, 0)
        )
        for (q in quads) {
            addTri(corners[q[0]], corners[q[1]], corners[q[2]], r, g, b)
            addTri(corners[q[0]], corners[q[2]], corners[q[3]], r, g, b)
        }
    }

    fun addSphere(
        rx: Float, ry: Float, rz: Float,
        px: Float = 0f, py: Float = 0f, pz: Float = 0f,
        segU: Int = 14, segV: Int = 10,
        r: Float, g: Float, b: Float,
        phiStart: Float = 0f, phiEnd: Float = PI.toFloat()
    ) {
        val grid = Array(segV + 1) { iv ->
            val phi = phiStart + (phiEnd - phiStart) * (iv.toFloat() / segV)
            val sp = sin(phi); val cp = cos(phi)
            Array(segU + 1) { iu ->
                val th = (2.0 * PI * iu / segU).toFloat()
                floatArrayOf(px + rx * sp * cos(th), py + ry * cp, pz + rz * sp * sin(th))
            }
        }
        for (iv in 0 until segV) {
            for (iu in 0 until segU) {
                val a = grid[iv][iu]
                val d = grid[iv][iu + 1]
                val bb = grid[iv + 1][iu]
                val c = grid[iv + 1][iu + 1]
                addTri(a, d, bb, r, g, b)
                addTri(d, c, bb, r, g, b)
            }
        }
    }

    fun addCylinder(
        rTop: Float, rBot: Float, h: Float,
        px: Float = 0f, py: Float = 0f, pz: Float = 0f,
        rotX: Float = 0f, rotY: Float = 0f, rotZ: Float = 0f,
        seg: Int = 12,
        r: Float, g: Float, b: Float
    ) {
        val hy = h * 0.5f
        val topPts = Array(seg + 1) { i ->
            val th = (2.0 * PI * i / seg).toFloat()
            val p = rotPoint(rTop * cos(th), hy, rTop * sin(th), rotX, rotY, rotZ)
            floatArrayOf(p[0] + px, p[1] + py, p[2] + pz)
        }
        val botPts = Array(seg + 1) { i ->
            val th = (2.0 * PI * i / seg).toFloat()
            val p = rotPoint(rBot * cos(th), -hy, rBot * sin(th), rotX, rotY, rotZ)
            floatArrayOf(p[0] + px, p[1] + py, p[2] + pz)
        }
        val topC = rotPoint(0f, hy, 0f, rotX, rotY, rotZ).let { floatArrayOf(it[0] + px, it[1] + py, it[2] + pz) }
        val botC = rotPoint(0f, -hy, 0f, rotX, rotY, rotZ).let { floatArrayOf(it[0] + px, it[1] + py, it[2] + pz) }

        for (i in 0 until seg) {
            addTri(topPts[i], topPts[i + 1], botPts[i], r, g, b)
            addTri(topPts[i + 1], botPts[i + 1], botPts[i], r, g, b)
            if (rTop > 1e-4f) addTri(topC, topPts[i + 1], topPts[i], r, g, b)
            if (rBot > 1e-4f) addTri(botC, botPts[i], botPts[i + 1], r, g, b)
        }
    }

    fun build(): Mesh3D {
        val arr = FloatArray(data.size) { data[it] }
        val bb = ByteBuffer.allocateDirect(arr.size * 4).order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(arr)
        fb.position(0)
        return Mesh3D(fb, arr.size / 9)
    }
}
