package agents

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import sensors.TerrainSensor
import terrain.Coordinate
import terrain.TerrainType
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class CoastlineAgent(
    private val sensor: TerrainSensor,
    private val config: CoastlineConfig
) : Agent {
    companion object {
        const val name = "Coastline Agent"

        data class CoastlineConfig(
            val landmassSize: Int,
        )

        data class Edge(
            val vertexFromInd: Int,
            val vertexToInd: Int,
            val weight: Int,
        )

        class Graph(
            private val vertices: List<Coordinate>,
            private var edges: List<Edge>
        ) {
            private val dsu = IntArray(vertices.size) { it }//disjoint set union

            init {
                edges = edges.sortedBy { edge -> edge.weight }
            }

            private fun dsuGetRoot(v: Int, cnt: Int = 0): Int {
                return when (val parentV = dsu[v]) {
                    v -> v
                    else -> dsuGetRoot(parentV, cnt + 1)
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

            @DelicateCoroutinesApi
            fun getSpanningTree(size: Int): List<Coordinate> {
                val res = mutableListOf<Coordinate>()
                for (e in edges) {
                    if (res.size == size)
                        break
                    if (dsuGetRoot(e.vertexFromInd) != dsuGetRoot(e.vertexToInd)) {
                        runBlocking {
                            GlobalScope.async {
                                if (vertices[e.vertexFromInd] !in res)
                                    res.add(vertices[e.vertexFromInd])
                                if (vertices[e.vertexToInd] !in res)
                                    res.add(vertices[e.vertexToInd])
                            }.await()
                            GlobalScope.async {
                                dsuUnite(e.vertexFromInd, e.vertexToInd)
                            }.await()
                        }
                    }
                }
                return res
            }
        }
    }

    @DelicateCoroutinesApi
    override fun generate() {
        val directions = listOf(Coordinate(0, -1), Coordinate(-1, 0))
        val vertices = mutableListOf<Coordinate>()
        val edges = mutableListOf<Edge>()
        val l = sensor.getTerrainLength()
        val w = sensor.getTerrainWidth()
        for (x in 1 until w - 1) {
            for (y in 1 until l - 1) {
                val v = Coordinate(x, y)
                vertices.add(v)
                for (dir in directions) {
                    val to = Coordinate(v.x + dir.x, v.y + dir.y)
                    if (sensor.isValidCoordinate(to) && to.x != 0 && to.y != 0) {
                        val vInd = vertices.size - 1
                        val toInd = (to.x - 1) * (l - 2) + to.y - 1
                        edges.add(Edge(vInd, toInd, Random.nextInt(100)))
                    }
                }
            }
        }
        val graph = Graph(vertices, edges)
        val time = measureTimeMillis {
            val randomSpanningTree = graph.getSpanningTree(config.landmassSize)
            for (v in randomSpanningTree) {
                sensor.setTerrainType(v, TerrainType.PLAIN)
            }
        }
        println("Execution time in milliseconds: $time")
    }

    override fun getName(): String {
        return name
    }
}