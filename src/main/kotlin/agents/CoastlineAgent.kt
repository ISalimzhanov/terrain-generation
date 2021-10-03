package agents

import sensors.TerrainSensor
import terrain.Coordinate
import terrain.TerrainType
import kotlin.random.Random

class CoastlineAgent(
    private val sensor: TerrainSensor,
    private val config: CoastlineConfig
) {
    companion object {
        data class CoastlineConfig(
            val tokens: Int,
            val coastHeight: Double,
            val limit: Int,
        )

        data class Edge(
            val vertexFromInd: Int,
            val vertexToInd: Int,
            val weight: Int,
        )

        class Graph(
            private val vertices: List<Coordinate>,
            private val edges: List<Edge>
        ) {
            private val dsu = mutableMapOf<Int, Int>() //disjoint set union

            init {
                for (i in vertices.indices) {
                    dsu[i] = i
                }
            }

            private fun dsuGetRoot(v: Int): Int {
                return when (val parentV = dsu[v]!!) {
                    v -> v
                    else -> dsuGetRoot(parentV)
                }
            }

            private fun dsuUnite(a: Int, b: Int) {
                var rootA = dsuGetRoot(a)
                var rootB = dsuGetRoot(b)
                if (Random.nextBoolean()) {
                    rootA = rootB.also { rootB = rootA }
                }
                dsu[rootA] = rootB
            }

            fun getSpanningTree(tokens: Int): List<Coordinate> {
                val res = mutableListOf<Coordinate>()
                for (e in edges) {
                    if (res.size == tokens)
                        break
                    if (dsuGetRoot(e.vertexFromInd) != dsuGetRoot(e.vertexToInd)) {
                        if (vertices[e.vertexFromInd] !in res)
                            res.add(vertices[e.vertexFromInd])
                        if (vertices[e.vertexToInd] !in res)
                            res.add(vertices[e.vertexToInd])
                        dsuUnite(e.vertexFromInd, e.vertexToInd)
                    }
                }
                return res
            }
        }
    }

    fun generate() {
        val directions = listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(-1, 0), Coordinate(0, -1))
        val vertices = mutableListOf<Coordinate>()
        val edges = mutableListOf<Edge>()
        for (x in 1 until sensor.getTerrainLength() - 1)
            for (y in 1 until sensor.getTerrainWidth() - 1) {
                val v = Coordinate(x, y)
                vertices.add(v)
                for (dir in directions) {
                    val to = Coordinate(v.x + dir.x, v.y + dir.y)
                    if (sensor.isValidCoordinate(to) && to !in vertices) {
                        val vInd = vertices.size - 1
                        val toInd = vertices.indexOf(to)
                        edges.add(Edge(vInd, toInd, Random.nextInt(100)))
                    }
                }
            }
        val graph = Graph(vertices, edges)
        val randomSpanningTree = graph.getSpanningTree(config.tokens)
        for (v in randomSpanningTree) {
            sensor.setTerrainType(v, TerrainType.PLAIN)
        }
    }
}