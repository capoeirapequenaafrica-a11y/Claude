package com.example

import com.example.data.model.FeatureItem
import com.example.data.model.StoreListingData
import com.example.engine.ContrastEngine
import com.example.engine.ListingLinterEngine
import com.example.engine.ParityEngine
import com.example.engine.SweepEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testParityEngineCalculation() {
    val features = listOf(
      FeatureItem("1", "proj", "Feat 1", "Auth", "must", original = true, cloneStatus = "yes"),
      FeatureItem("2", "proj", "Feat 2", "Auth", "must", original = true, cloneStatus = "yes"),
      FeatureItem("3", "proj", "Feat 3", "Dashboard", "should", original = true, cloneStatus = "partial"),
      FeatureItem("4", "proj", "Feat 4", "Settings", "could", original = true, cloneStatus = "no")
    )
    val result = ParityEngine.calculateParity(features)
    // Weight: must=3, must=3, should=2, could=1 => total = 9
    // Earned: 3*1 + 3*1 + 2*0.5 + 1*0 = 7
    // Score: 100 * 7 / 9 = 77.8
    assertEquals(77.8, result.featureScore, 0.1)
    assertEquals(2, result.mustHaveDone)
    assertEquals(2, result.mustHaveTotal)
    assertFalse(result.isShippable) // Score < 80
  }

  @Test
  fun testContrastEngineLuminanceAndRatio() {
    // Pure black on pure white: ratio should be 21:1
    val ratio = ContrastEngine.ratio("#000000", "#FFFFFF")
    assertEquals(21.0, ratio, 0.1)

    val checkNormal = ContrastEngine.checkPair("text", "bg", "#000000", "#FFFFFF", "normal")
    assertTrue(checkNormal.passAa)
    assertTrue(checkNormal.passAaa)

    // Low contrast pair
    val lowRatio = ContrastEngine.ratio("#888888", "#999999")
    val checkLow = ContrastEngine.checkPair("text", "bg", "#888888", "#999999", "normal")
    assertFalse(checkLow.passAa)
  }

  @Test
  fun testSweepEngineDetectsCompetitorLeaking() {
    val code = "val service = CalendlyClient(url = 'https://calendly.com')"
    val hits = SweepEngine.performSweep(
      content = code,
      avoidNames = listOf("Calendly"),
      domains = listOf("calendly.com"),
      colors = emptyList()
    )
    assertEquals(2, hits.size)
  }

  @Test
  fun testListingLinterFlagsPolicyIssues() {
    val listing = StoreListingData(
      projectId = "p1",
      playStoreTitle = "#1 Best Scheduling App", // Violates claims and #1
      playStoreShortDesc = "Fast appointments",
      avoidNames = listOf("Calendly")
    )
    val issues = ListingLinterEngine.lintListing(listing)
    assertTrue(issues.any { it.message.contains("ranking or promotional claim") })
  }

  @Test
  fun testEngine3DProceduralMeshCreation() {
    val crystal = com.example.engine.Engine3D.createCrystalCube()
    assertTrue(crystal.vertexCount > 0)
    assertTrue(crystal.polygonCount > 0)
    val drone = com.example.engine.Engine3D.createCyberDrone()
    assertTrue(drone.vertices.isNotEmpty())
    assertTrue(drone.faces.isNotEmpty())
  }

  @Test
  fun testEngine3DWavefrontObjExport() {
    val mesh = com.example.engine.Engine3D.createCrystalCube()
    val obj = com.example.engine.Engine3D.exportToObj(mesh)
    assertTrue(obj.contains("v "))
    assertTrue(obj.contains("vn "))
    assertTrue(obj.contains("f "))
  }

  @Test
  fun testEngine3DGltfJsonStructure() {
    val mesh = com.example.engine.Engine3D.createCrystalCube()
    val gltf = com.example.engine.Engine3D.exportGltfJson(mesh)
    assertTrue(gltf.contains("\"asset\""))
    assertTrue(gltf.contains("\"version\": \"2.0\""))
    assertTrue(gltf.contains("\"pbrMetallicRoughness\""))

    val summary = com.example.engine.Engine3D.getGlbStructureSummary(mesh)
    assertTrue(summary.totalByteLength > 0)
  }

  @Test
  fun testEngine3DObjParsing() {
    val objText = """
      # Sample OBJ
      v 0.0 1.0 0.0
      v -1.0 -1.0 0.0
      v 1.0 -1.0 0.0
      f 1 2 3
    """.trimIndent()
    val parsed = com.example.engine.Engine3D.parseObjText(objText, "Triangle.obj")
    assertEquals(3, parsed.vertexCount)
    assertEquals(1, parsed.polygonCount)
    assertEquals("Triangle", parsed.name)
  }

  @Test
  fun testEngine3DLoadModelFromBytesAutoDetect() {
    val objText = "v 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n"
    val mesh = com.example.engine.Engine3D.loadModelFromBytes(objText.toByteArray(), "test.obj")
    assertEquals(3, mesh.vertexCount)
    assertEquals(1, mesh.polygonCount)
  }
}
