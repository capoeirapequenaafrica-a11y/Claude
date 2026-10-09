package com.example.engine

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(v: Vector3) = Vector3(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector3) = Vector3(x - v.x, y - v.y, z - v.z)
    operator fun times(s: Float) = Vector3(x * s, y * s, z * s)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vector3 {
        val l = length()
        return if (l > 0.0001f) Vector3(x / l, y / l, z / l) else this
    }

    fun dot(v: Vector3): Float = x * v.x + y * v.y + z * v.z

    fun cross(v: Vector3): Vector3 = Vector3(
        y * v.z - z * v.y,
        z * v.x - x * v.z,
        x * v.y - y * v.x
    )

    fun rotateX(angleRad: Float): Vector3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vector3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(angleRad: Float): Vector3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vector3(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateZ(angleRad: Float): Vector3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vector3(x * c - y * s, x * s + y * c, z)
    }
}

data class Face3D(
    val i0: Int,
    val i1: Int,
    val i2: Int,
    val normal: Vector3,
    val color: Color = Color(0xFF06B6D4)
)

data class Mesh3D(
    val name: String,
    val category: String,
    val vertices: List<Vector3>,
    val faces: List<Face3D>,
    val edges: List<Pair<Int, Int>>
) {
    val vertexCount: Int get() = vertices.size
    val polygonCount: Int get() = faces.size
}

data class GlbStructureSummary(
    val magic: String = "glTF (0x46546C67)",
    val version: Int = 2,
    val totalByteLength: Int,
    val jsonChunkLength: Int,
    val binaryChunkLength: Int,
    val accessorsCount: Int,
    val bufferViewsCount: Int,
    val materialsCount: Int = 1,
    val hasPbrMetallicRoughness: Boolean = true
)

object Engine3D {

    fun createCrystalCube(): Mesh3D {
        val s = 1.2f
        val v = listOf(
            Vector3(-s, -s, -s), // 0
            Vector3(s, -s, -s),  // 1
            Vector3(s, s, -s),   // 2
            Vector3(-s, s, -s),  // 3
            Vector3(-s, -s, s),  // 4
            Vector3(s, -s, s),   // 5
            Vector3(s, s, s),    // 6
            Vector3(-s, s, s),   // 7
            // Center apex facets
            Vector3(0f, 0f, s * 1.5f),  // 8 front apex
            Vector3(0f, 0f, -s * 1.5f), // 9 back apex
            Vector3(0f, s * 1.5f, 0f),  // 10 top apex
            Vector3(0f, -s * 1.5f, 0f)  // 11 bottom apex
        )

        val faces = mutableListOf<Face3D>()
        fun addTri(a: Int, b: Int, c: Int, col: Color) {
            val v0 = v[a]
            val v1 = v[b]
            val v2 = v[c]
            val norm = (v1 - v0).cross(v2 - v0).normalize()
            faces.add(Face3D(a, b, c, norm, col))
        }

        val c1 = Color(0xFF06B6D4) // Cyan
        val c2 = Color(0xFF6366F1) // Indigo
        val c3 = Color(0xFF8B5CF6) // Violet
        val c4 = Color(0xFF0EA5E9) // Sky

        // Front facets to apex 8
        addTri(4, 5, 8, c1)
        addTri(5, 6, 8, c2)
        addTri(6, 7, 8, c3)
        addTri(7, 4, 8, c4)

        // Back facets to apex 9
        addTri(1, 0, 9, c1)
        addTri(2, 1, 9, c2)
        addTri(3, 2, 9, c3)
        addTri(0, 3, 9, c4)

        // Top facets to apex 10
        addTri(3, 2, 10, c2)
        addTri(2, 6, 10, c1)
        addTri(6, 7, 10, c3)
        addTri(7, 3, 10, c4)

        // Bottom facets to apex 11
        addTri(0, 4, 11, c3)
        addTri(4, 5, 11, c1)
        addTri(5, 1, 11, c2)
        addTri(1, 0, 11, c4)

        val edges = mutableListOf<Pair<Int, Int>>()
        for (f in faces) {
            edges.add(Pair(minOf(f.i0, f.i1), maxOf(f.i0, f.i1)))
            edges.add(Pair(minOf(f.i1, f.i2), maxOf(f.i1, f.i2)))
            edges.add(Pair(minOf(f.i2, f.i0), maxOf(f.i2, f.i0)))
        }

        return Mesh3D("Cyber Crystal Cube", "Estrutura Geométrica", v, faces, edges.distinct())
    }

    fun createCyberDrone(): Mesh3D {
        val v = mutableListOf<Vector3>()
        v.add(Vector3(0f, 0f, 0.4f))   // 0 top
        v.add(Vector3(0f, 0f, -0.4f))  // 1 bottom
        v.add(Vector3(-0.6f, -0.6f, 0f)) // 2
        v.add(Vector3(0.6f, -0.6f, 0f))  // 3
        v.add(Vector3(0.6f, 0.6f, 0f))   // 4
        v.add(Vector3(-0.6f, 0.6f, 0f))  // 5

        val armDist = 1.4f
        val rHeight = 0.2f
        v.add(Vector3(-armDist, -armDist, rHeight)) // 6 FL rotor
        v.add(Vector3(armDist, -armDist, rHeight))  // 7 FR rotor
        v.add(Vector3(armDist, armDist, rHeight))   // 8 BR rotor
        v.add(Vector3(-armDist, armDist, rHeight))  // 9 BL rotor
        v.add(Vector3(0f, -0.9f, 0f)) // 10 sensor

        val faces = mutableListOf<Face3D>()
        fun addTri(a: Int, b: Int, c: Int, col: Color) {
            val v0 = v[a]
            val v1 = v[b]
            val v2 = v[c]
            val norm = (v1 - v0).cross(v2 - v0).normalize()
            faces.add(Face3D(a, b, c, norm, col))
        }

        val colBody = Color(0xFF06B6D4)
        val colArm = Color(0xFF6366F1)
        val colEye = Color(0xFF10B981)

        addTri(2, 3, 0, colBody)
        addTri(3, 4, 0, colBody)
        addTri(4, 5, 0, colBody)
        addTri(5, 2, 0, colBody)

        addTri(3, 2, 1, colBody)
        addTri(4, 3, 1, colBody)
        addTri(5, 4, 1, colBody)
        addTri(2, 5, 1, colBody)

        addTri(2, 6, 0, colArm)
        addTri(3, 7, 0, colArm)
        addTri(4, 8, 0, colArm)
        addTri(5, 9, 0, colArm)

        addTri(2, 3, 10, colEye)
        addTri(3, 0, 10, colEye)
        addTri(0, 2, 10, colEye)

        val edges = mutableListOf<Pair<Int, Int>>()
        for (f in faces) {
            edges.add(Pair(minOf(f.i0, f.i1), maxOf(f.i0, f.i1)))
            edges.add(Pair(minOf(f.i1, f.i2), maxOf(f.i1, f.i2)))
            edges.add(Pair(minOf(f.i2, f.i0), maxOf(f.i2, f.i0)))
        }

        return Mesh3D("Autonomous Cyber Drone", "Robótica & Hardware", v, faces, edges.distinct())
    }

    fun createMechAndroid(): Mesh3D {
        val v = mutableListOf<Vector3>()
        v.add(Vector3(0f, 1.4f, 0.4f))   // 0
        v.add(Vector3(0f, 1.4f, -0.4f))  // 1
        v.add(Vector3(-0.4f, 1.1f, 0f))  // 2
        v.add(Vector3(0.4f, 1.1f, 0f))   // 3
        v.add(Vector3(0f, 1.7f, 0f))     // 4 head top

        v.add(Vector3(-0.7f, 0.9f, 0.3f))  // 5
        v.add(Vector3(0.7f, 0.9f, 0.3f))   // 6
        v.add(Vector3(0.5f, 0.0f, 0.2f))   // 7 waist R
        v.add(Vector3(-0.5f, 0.0f, 0.2f))  // 8 waist L
        v.add(Vector3(0f, 0.5f, 0.5f))     // 9 core reactor

        v.add(Vector3(-0.4f, -1.3f, 0f)) // 10 L foot
        v.add(Vector3(0.4f, -1.3f, 0f))  // 11 R foot
        v.add(Vector3(-1.2f, 0.2f, 0f)) // 12 L hand
        v.add(Vector3(1.2f, 0.2f, 0f))  // 13 R hand

        val faces = mutableListOf<Face3D>()
        fun addTri(a: Int, b: Int, c: Int, col: Color) {
            val v0 = v[a]
            val v1 = v[b]
            val v2 = v[c]
            val norm = (v1 - v0).cross(v2 - v0).normalize()
            faces.add(Face3D(a, b, c, norm, col))
        }

        val colPlates = Color(0xFF67E8F9)
        val colCore = Color(0xFF8B5CF6)
        val colFrame = Color(0xFF0284C7)

        addTri(0, 3, 4, colPlates)
        addTri(3, 1, 4, colPlates)
        addTri(1, 2, 4, colPlates)
        addTri(2, 0, 4, colPlates)

        addTri(5, 6, 9, colCore)
        addTri(6, 7, 9, colCore)
        addTri(7, 8, 9, colCore)
        addTri(8, 5, 9, colCore)

        addTri(5, 8, 12, colFrame)
        addTri(6, 7, 13, colFrame)
        addTri(8, 10, 7, colFrame)
        addTri(7, 11, 10, colFrame)

        val edges = mutableListOf<Pair<Int, Int>>()
        for (f in faces) {
            edges.add(Pair(minOf(f.i0, f.i1), maxOf(f.i1, f.i0)))
            edges.add(Pair(minOf(f.i1, f.i2), maxOf(f.i2, f.i1)))
            edges.add(Pair(minOf(f.i2, f.i0), maxOf(f.i0, f.i2)))
        }

        return Mesh3D("Holographic Android", "Biomecânica 3D", v, faces, edges.distinct())
    }

    fun createArchitectureTower(): Mesh3D {
        val v = mutableListOf<Vector3>()
        val floors = 4
        val floorH = 0.6f
        val baseR = 1.0f

        for (fl in 0..floors) {
            val y = (fl - floors / 2f) * floorH
            val r = baseR * (1.0f - fl * 0.15f)
            v.add(Vector3(-r, y, -r)) // 0
            v.add(Vector3(r, y, -r))  // 1
            v.add(Vector3(r, y, r))   // 2
            v.add(Vector3(-r, y, r))  // 3
        }
        v.add(Vector3(0f, (floors / 2f + 1f) * floorH, 0f)) // spire apex

        val faces = mutableListOf<Face3D>()
        fun addTri(a: Int, b: Int, c: Int, col: Color) {
            val v0 = v[a]
            val v1 = v[b]
            val v2 = v[c]
            val norm = (v1 - v0).cross(v2 - v0).normalize()
            faces.add(Face3D(a, b, c, norm, col))
        }

        val cGlass = Color(0xFF06B6D4)
        val cFrame = Color(0xFF6366F1)

        for (fl in 0 until floors) {
            val b = fl * 4
            val t = (fl + 1) * 4
            addTri(b, t, t + 1, cGlass)
            addTri(b, t + 1, b + 1, cGlass)
            addTri(b + 1, t + 1, t + 2, cFrame)
            addTri(b + 1, t + 2, b + 2, cFrame)
            addTri(b + 2, t + 2, t + 3, cGlass)
            addTri(b + 2, t + 3, b + 3, cGlass)
            addTri(b + 3, t + 3, t, cFrame)
            addTri(b + 3, t, b, cFrame)
        }

        val topB = floors * 4
        val apex = v.size - 1
        addTri(topB, apex, topB + 1, Color(0xFFF59E0B))
        addTri(topB + 1, apex, topB + 2, Color(0xFFF59E0B))
        addTri(topB + 2, apex, topB + 3, Color(0xFFF59E0B))
        addTri(topB + 3, apex, topB, Color(0xFFF59E0B))

        val edges = mutableListOf<Pair<Int, Int>>()
        for (f in faces) {
            edges.add(Pair(minOf(f.i0, f.i1), maxOf(f.i1, f.i0)))
            edges.add(Pair(minOf(f.i1, f.i2), maxOf(f.i2, f.i1)))
            edges.add(Pair(minOf(f.i2, f.i0), maxOf(f.i0, f.i2)))
        }

        return Mesh3D("Cyber Tower & Hub", "Arquitetura CAD", v, faces, edges.distinct())
    }

    fun createTorus(): Mesh3D {
        val v = mutableListOf<Vector3>()
        val faces = mutableListOf<Face3D>()
        val mainR = 1.0f
        val tubeR = 0.35f
        val mainSegments = 12
        val tubeSegments = 8

        for (i in 0 until mainSegments) {
            val u = i.toFloat() / mainSegments * 2f * PI.toFloat()
            for (j in 0 until tubeSegments) {
                val valV = j.toFloat() / tubeSegments * 2f * PI.toFloat()
                val x = (mainR + tubeR * cos(valV)) * cos(u)
                val y = tubeR * sin(valV)
                val z = (mainR + tubeR * cos(valV)) * sin(u)
                v.add(Vector3(x, y, z))
            }
        }

        fun getIdx(i: Int, j: Int) = (i % mainSegments) * tubeSegments + (j % tubeSegments)

        val col1 = Color(0xFF06B6D4)
        val col2 = Color(0xFF8B5CF6)

        for (i in 0 until mainSegments) {
            for (j in 0 until tubeSegments) {
                val i0 = getIdx(i, j)
                val i1 = getIdx(i + 1, j)
                val i2 = getIdx(i + 1, j + 1)
                val i3 = getIdx(i, j + 1)

                val col = if ((i + j) % 2 == 0) col1 else col2
                val v0 = v[i0]
                val v1 = v[i1]
                val v2 = v[i2]
                val norm = (v1 - v0).cross(v2 - v0).normalize()

                faces.add(Face3D(i0, i1, i2, norm, col))
                faces.add(Face3D(i0, i2, i3, norm, col))
            }
        }

        val edges = mutableListOf<Pair<Int, Int>>()
        for (f in faces) {
            edges.add(Pair(minOf(f.i0, f.i1), maxOf(f.i1, f.i0)))
            edges.add(Pair(minOf(f.i1, f.i2), maxOf(f.i2, f.i1)))
            edges.add(Pair(minOf(f.i2, f.i0), maxOf(f.i0, f.i2)))
        }

        return Mesh3D("Parametric Torus Ring", "Variedade Geométrica", v, faces, edges.distinct())
    }

    // ==========================================
    // PARSERS FOR CUSTOM GLB AND OBJ 3D ASSETS
    // ==========================================

    fun parseGlbBytes(bytes: ByteArray, name: String = "Custom Model.glb"): Mesh3D {
        if (bytes.size < 20) throw IllegalArgumentException("Arquivo GLB muito curto (${bytes.size} bytes)")
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        val magic = buffer.int
        if (magic != 0x46546C67) {
            // Check if it's text-based OBJ
            val textSample = String(bytes.take(minOf(bytes.size, 512)).toByteArray())
            if (textSample.contains("v ") || textSample.contains("#")) {
                return parseObjText(String(bytes, Charsets.UTF_8), name)
            }
            throw IllegalArgumentException("Header inválido. Esperado glTF (0x46546C67), recebido 0x${Integer.toHexString(magic)}")
        }

        val version = buffer.int
        val totalLength = buffer.int

        // Chunk 0: JSON
        val chunk0Length = buffer.int
        val chunk0Type = buffer.int
        if (chunk0Length > buffer.remaining()) {
            throw IllegalArgumentException("Chunk JSON excede os limites do arquivo")
        }
        val jsonBytes = ByteArray(chunk0Length)
        buffer.get(jsonBytes)
        val jsonString = String(jsonBytes, Charsets.UTF_8)
        val json = JSONObject(jsonString)

        // Chunk 1: BIN
        var binBytes = ByteArray(0)
        if (buffer.remaining() >= 8) {
            val chunk1Length = buffer.int
            val chunk1Type = buffer.int
            val available = minOf(chunk1Length, buffer.remaining())
            binBytes = ByteArray(available)
            buffer.get(binBytes)
        }

        val binBuffer = ByteBuffer.wrap(binBytes).order(ByteOrder.LITTLE_ENDIAN)

        val accessors = json.optJSONArray("accessors") ?: throw IllegalArgumentException("GLB sem accessors")
        val bufferViews = json.optJSONArray("bufferViews") ?: throw IllegalArgumentException("GLB sem bufferViews")
        val meshes = json.optJSONArray("meshes") ?: throw IllegalArgumentException("GLB sem malhas")

        if (meshes.length() == 0) throw IllegalArgumentException("Nenhuma malha no GLB")
        val meshObj = meshes.getJSONObject(0)
        val primitives = meshObj.getJSONArray("primitives")
        val prim = primitives.getJSONObject(0)
        val attributes = prim.getJSONObject("attributes")
        val posAccIndex = attributes.getInt("POSITION")

        val posAcc = accessors.getJSONObject(posAccIndex)
        val posCount = posAcc.getInt("count")
        val posBvIndex = posAcc.getInt("bufferView")
        val posAccOffset = posAcc.optInt("byteOffset", 0)

        val posBv = bufferViews.getJSONObject(posBvIndex)
        val posBvOffset = posBv.optInt("byteOffset", 0)
        val posStride = posBv.optInt("byteStride", 12)

        val rawVertices = mutableListOf<Vector3>()
        for (i in 0 until posCount) {
            val offset = posBvOffset + posAccOffset + (i * posStride)
            if (offset + 12 <= binBytes.size) {
                binBuffer.position(offset)
                val x = binBuffer.float
                val y = binBuffer.float
                val z = binBuffer.float
                rawVertices.add(Vector3(x, y, z))
            }
        }

        if (rawVertices.isEmpty()) throw IllegalArgumentException("Nenhum vértice pôde ser extraído da malha GLB")

        val faces = mutableListOf<Face3D>()
        val hasIndices = prim.has("indices")
        if (hasIndices) {
            val indAccIndex = prim.getInt("indices")
            val indAcc = accessors.getJSONObject(indAccIndex)
            val indCount = indAcc.getInt("count")
            val indCompType = indAcc.getInt("componentType")
            val indBvIndex = indAcc.getInt("bufferView")
            val indAccOffset = indAcc.optInt("byteOffset", 0)

            val indBv = bufferViews.getJSONObject(indBvIndex)
            val indBvOffset = indBv.optInt("byteOffset", 0)
            val baseOffset = indBvOffset + indAccOffset

            val indexList = mutableListOf<Int>()
            for (i in 0 until indCount) {
                val offset = when (indCompType) {
                    5121 -> baseOffset + i
                    5123 -> baseOffset + i * 2
                    5125 -> baseOffset + i * 4
                    else -> baseOffset + i * 2
                }
                if (offset < binBytes.size) {
                    binBuffer.position(offset)
                    val idx = when (indCompType) {
                        5121 -> (binBuffer.get().toInt() and 0xFF)
                        5123 -> (binBuffer.short.toInt() and 0xFFFF)
                        5125 -> binBuffer.int
                        else -> (binBuffer.short.toInt() and 0xFFFF)
                    }
                    indexList.add(idx)
                }
            }

            for (i in 0 until indexList.size - 2 step 3) {
                val i0 = indexList[i]
                val i1 = indexList[i + 1]
                val i2 = indexList[i + 2]
                if (i0 < rawVertices.size && i1 < rawVertices.size && i2 < rawVertices.size) {
                    val v0 = rawVertices[i0]
                    val v1 = rawVertices[i1]
                    val v2 = rawVertices[i2]
                    val norm = (v1 - v0).cross(v2 - v0).normalize()
                    faces.add(Face3D(i0, i1, i2, norm, Color(0xFF06B6D4)))
                }
            }
        } else {
            for (i in 0 until rawVertices.size - 2 step 3) {
                val v0 = rawVertices[i]
                val v1 = rawVertices[i + 1]
                val v2 = rawVertices[i + 2]
                val norm = (v1 - v0).cross(v2 - v0).normalize()
                faces.add(Face3D(i, i + 1, i + 2, norm, Color(0xFF06B6D4)))
            }
        }

        // Center and normalize coordinates to radius ~1.2
        val minX = rawVertices.minOf { it.x }
        val maxX = rawVertices.maxOf { it.x }
        val minY = rawVertices.minOf { it.y }
        val maxY = rawVertices.maxOf { it.y }
        val minZ = rawVertices.minOf { it.z }
        val maxZ = rawVertices.maxOf { it.z }

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f
        val centerZ = (minZ + maxZ) / 2f
        val maxSpan = maxOf(maxX - minX, maxY - minY, maxZ - minZ, 0.001f)
        val scale = 2.4f / maxSpan

        val normalizedVertices = rawVertices.map { v ->
            Vector3((v.x - centerX) * scale, (v.y - centerY) * scale, (v.z - centerZ) * scale)
        }

        val edges = mutableListOf<Pair<Int, Int>>()
        for (f in faces) {
            edges.add(Pair(minOf(f.i0, f.i1), maxOf(f.i1, f.i0)))
            edges.add(Pair(minOf(f.i1, f.i2), maxOf(f.i2, f.i1)))
            edges.add(Pair(minOf(f.i2, f.i0), maxOf(f.i0, f.i2)))
        }

        val cleanName = name.substringAfterLast("/").removeSuffix(".glb").ifBlank { "Custom GLB" }
        return Mesh3D(cleanName, "Custom GLB Import", normalizedVertices, faces, edges.distinct())
    }

    fun parseObjText(text: String, name: String = "Custom Model.obj"): Mesh3D {
        val rawVertices = mutableListOf<Vector3>()
        val faces = mutableListOf<Face3D>()

        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("v ")) {
                val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
                if (parts.size >= 4) {
                    val x = parts[1].toFloatOrNull() ?: 0f
                    val y = parts[2].toFloatOrNull() ?: 0f
                    val z = parts[3].toFloatOrNull() ?: 0f
                    rawVertices.add(Vector3(x, y, z))
                }
            } else if (trimmed.startsWith("f ")) {
                val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
                if (parts.size >= 4) {
                    fun parseIdx(p: String): Int {
                        val sub = p.split("/")[0]
                        val raw = sub.toIntOrNull() ?: 1
                        return if (raw > 0) raw - 1 else rawVertices.size + raw
                    }
                    val i0 = parseIdx(parts[1])
                    val i1 = parseIdx(parts[2])
                    val i2 = parseIdx(parts[3])
                    if (i0 in rawVertices.indices && i1 in rawVertices.indices && i2 in rawVertices.indices) {
                        val v0 = rawVertices[i0]
                        val v1 = rawVertices[i1]
                        val v2 = rawVertices[i2]
                        val norm = (v1 - v0).cross(v2 - v0).normalize()
                        faces.add(Face3D(i0, i1, i2, norm, Color(0xFF6366F1)))
                    }
                }
            }
        }

        if (rawVertices.isEmpty()) throw IllegalArgumentException("Nenhum vértice encontrado no arquivo .OBJ")

        val minX = rawVertices.minOf { it.x }
        val maxX = rawVertices.maxOf { it.x }
        val minY = rawVertices.minOf { it.y }
        val maxY = rawVertices.maxOf { it.y }
        val minZ = rawVertices.minOf { it.z }
        val maxZ = rawVertices.maxOf { it.z }

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f
        val centerZ = (minZ + maxZ) / 2f
        val maxSpan = maxOf(maxX - minX, maxY - minY, maxZ - minZ, 0.001f)
        val scale = 2.4f / maxSpan

        val normalized = rawVertices.map { v ->
            Vector3((v.x - centerX) * scale, (v.y - centerY) * scale, (v.z - centerZ) * scale)
        }

        val edges = mutableListOf<Pair<Int, Int>>()
        for (f in faces) {
            edges.add(Pair(minOf(f.i0, f.i1), maxOf(f.i1, f.i0)))
            edges.add(Pair(minOf(f.i1, f.i2), maxOf(f.i2, f.i1)))
            edges.add(Pair(minOf(f.i2, f.i0), maxOf(f.i0, f.i2)))
        }

        val cleanName = name.substringAfterLast("/").removeSuffix(".obj").ifBlank { "Custom OBJ" }
        return Mesh3D(cleanName, "Custom OBJ Import", normalized, faces, edges.distinct())
    }

    fun loadModelFromBytes(bytes: ByteArray, filename: String): Mesh3D {
        return if (filename.endsWith(".obj", ignoreCase = true)) {
            parseObjText(String(bytes, Charsets.UTF_8), filename)
        } else {
            try {
                parseGlbBytes(bytes, filename)
            } catch (e: Exception) {
                try {
                    parseObjText(String(bytes, Charsets.UTF_8), filename)
                } catch (e2: Exception) {
                    throw e
                }
            }
        }
    }

    suspend fun fetchModelFromUrl(url: String): Pair<ByteArray, String> = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw java.io.IOException("Falha no download (HTTP ${response.code})")
        }
        val bytes = response.body?.bytes() ?: throw java.io.IOException("Resposta vazia")
        val cleanName = url.substringAfterLast("/").substringBefore("?").ifBlank { "downloaded_model.glb" }
        Pair(bytes, cleanName)
    }

    // Wavefront .OBJ exporter
    fun exportToObj(mesh: Mesh3D): String {
        val sb = StringBuilder()
        sb.append("# Replica Studio 3D Wavefront OBJ Exporter\n")
        sb.append("# Model: ${mesh.name}\n")
        sb.append("# Vertices: ${mesh.vertexCount}, Faces: ${mesh.polygonCount}\n\n")

        for (v in mesh.vertices) {
            sb.append(String.format("v %.4f %.4f %.4f\n", v.x, v.y, v.z))
        }
        sb.append("\n")

        for (f in mesh.faces) {
            sb.append(String.format("vn %.4f %.4f %.4f\n", f.normal.x, f.normal.y, f.normal.z))
        }
        sb.append("\n")

        for ((idx, f) in mesh.faces.withIndex()) {
            val nIdx = idx + 1
            sb.append("f ${f.i0 + 1}//$nIdx ${f.i1 + 1}//$nIdx ${f.i2 + 1}//$nIdx\n")
        }

        return sb.toString()
    }

    // glTF 2.0 / GLB JSON Structure representation
    fun exportGltfJson(mesh: Mesh3D): String {
        val vertexBytes = mesh.vertexCount * 3 * 4
        val normalBytes = mesh.polygonCount * 3 * 4
        val indexBytes = mesh.polygonCount * 3 * 2
        val totalBinBytes = vertexBytes + normalBytes + indexBytes

        return """
{
  "asset": {
    "version": "2.0",
    "generator": "Replica Studio 3D GLB Engine v1.0"
  },
  "scene": 0,
  "scenes": [
    {
      "name": "${mesh.name}_Scene",
      "nodes": [0]
    }
  ],
  "nodes": [
    {
      "name": "${mesh.name}",
      "mesh": 0,
      "translation": [0.0, 0.0, 0.0],
      "rotation": [0.0, 0.0, 0.0, 1.0],
      "scale": [1.0, 1.0, 1.0]
    }
  ],
  "meshes": [
    {
      "name": "${mesh.name}_Mesh",
      "primitives": [
        {
          "attributes": {
            "POSITION": 0,
            "NORMAL": 1
          },
          "indices": 2,
          "material": 0,
          "mode": 4
        }
      ]
    }
  ],
  "materials": [
    {
      "name": "Replica_CyberPBR",
      "pbrMetallicRoughness": {
        "baseColorFactor": [0.024, 0.714, 0.831, 1.0],
        "metallicFactor": 0.85,
        "roughnessFactor": 0.25
      },
      "emissiveFactor": [0.1, 0.05, 0.25],
      "doubleSided": true
    }
  ],
  "accessors": [
    {
      "bufferView": 0,
      "byteOffset": 0,
      "componentType": 5126,
      "count": ${mesh.vertexCount},
      "type": "VEC3",
      "max": [1.5, 1.5, 1.5],
      "min": [-1.5, -1.5, -1.5]
    },
    {
      "bufferView": 1,
      "byteOffset": 0,
      "componentType": 5126,
      "count": ${mesh.faces.size},
      "type": "VEC3"
    },
    {
      "bufferView": 2,
      "byteOffset": 0,
      "componentType": 5123,
      "count": ${mesh.faces.size * 3},
      "type": "SCALAR"
    }
  ],
  "bufferViews": [
    { "buffer": 0, "byteOffset": 0, "byteLength": $vertexBytes, "target": 34962 },
    { "buffer": 0, "byteOffset": $vertexBytes, "byteLength": $normalBytes, "target": 34962 },
    { "buffer": 0, "byteOffset": ${vertexBytes + normalBytes}, "byteLength": $indexBytes, "target": 34963 }
  ],
  "buffers": [
    {
      "byteLength": $totalBinBytes
    }
  ]
}
        """.trimIndent()
    }

    fun getGlbStructureSummary(mesh: Mesh3D): GlbStructureSummary {
        val vertexBytes = mesh.vertexCount * 3 * 4
        val normalBytes = mesh.polygonCount * 3 * 4
        val indexBytes = mesh.polygonCount * 3 * 2
        val binBytes = vertexBytes + normalBytes + indexBytes
        val jsonBytes = exportGltfJson(mesh).toByteArray().size

        return GlbStructureSummary(
            totalByteLength = 12 + 8 + jsonBytes + 8 + binBytes,
            jsonChunkLength = jsonBytes,
            binaryChunkLength = binBytes,
            accessorsCount = 3,
            bufferViewsCount = 3
        )
    }
}
