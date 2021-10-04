package agents

import kotlinx.coroutines.DelicateCoroutinesApi
import sensors.TerrainSensor
import sensors.TerrainSensor.Companion.distance
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
            val limit: Int,
            val landHeight: Double,
        )
    }

    private fun score(
        coordinate: Coordinate,
        attractor: Coordinate,
        repulsor: Coordinate,
    ): Long {
        val de = minOf(
            distance(coordinate, Coordinate(coordinate.x, 0)), //up
            distance(coordinate, Coordinate(coordinate.x, sensor.getTerrainWidth() - 1)), //down
            distance(coordinate, Coordinate(0, coordinate.y)), //left
            distance(coordinate, Coordinate(sensor.getTerrainLength() - 1, coordinate.y)), //right
        )
        val dr = distance(coordinate, repulsor)
        val da = distance(coordinate, attractor)
        return dr - da + 3 * de
    }

    private fun makeLand(coordinate: Coordinate) {
        sensor.setTerrainType(coordinate, TerrainType.PLAIN)
        sensor.setHeight(coordinate, config.landHeight)
    }

    private val landmass = mutableListOf<Coordinate>()

    @DelicateCoroutinesApi
    override fun generate() {
        val time = measureTimeMillis {
            var maxScore = 0L
            var bestPont: Coordinate? = null
            for (i in 0..100) {
                val point = Coordinate(
                    Random.nextInt(0, sensor.getTerrainLength() - 1),
                    Random.nextInt(0, sensor.getTerrainWidth() - 1),
                )
                val pointScore = score(point, point, point)
                if (pointScore > maxScore) {
                    maxScore = pointScore
                    bestPont = point
                }
            }
            landmass.add(bestPont!!)
            makeLand(bestPont)
            generate(bestPont, config.landmassSize - 1)
            landmass.clear()
        }
        println("Execution time in milliseconds: $time")
    }

    private fun generate(
        seedPoint: Coordinate,
        tokens: Int,
    ) {
        if (tokens >= config.limit) {
            generate(landmass.random(), (tokens + 1) / 2)
            generate(landmass.random(), tokens / 2)
            return
        }
        val randomDir = TerrainSensor.directions.random()
        while (sensor.getTerrainType(seedPoint) != TerrainType.WATER) {
            seedPoint.x += randomDir.x
            seedPoint.y += randomDir.y
        }
        seedPoint.x -= randomDir.x
        seedPoint.y -= randomDir.y
        val attractor: Coordinate
        val repulsor: Coordinate
        if (Random.nextBoolean()) {
            val left = Coordinate((0..seedPoint.x).random(), (0 until sensor.getTerrainWidth()).random())
            val right = Coordinate(
                (seedPoint.x + 1 until sensor.getTerrainLength()).random(),
                (0 until sensor.getTerrainWidth()).random()
            )
            if (Random.nextBoolean()) {
                attractor = left
                repulsor = right
            } else {
                attractor = right
                repulsor = left
            }
        } else {
            val up = Coordinate((0 until sensor.getTerrainLength()).random(), (0..seedPoint.y).random())
            val down = Coordinate(
                (0 until sensor.getTerrainLength()).random(),
                (seedPoint.y + 1 until sensor.getTerrainWidth()).random()
            )
            if (Random.nextBoolean()) {
                attractor = up
                repulsor = down
            } else {
                attractor = down
                repulsor = up
            }
        }

        var toGenerate = tokens
        val newLands = mutableListOf(seedPoint)
        while (toGenerate != 0) {
            val point = newLands.random()
            var maxScore = -(1e18).toLong()
            var best: Coordinate? = null
            for (dir in TerrainSensor.directions) {
                val v = Coordinate(point.x + dir.x, point.y + dir.y)
                if (!sensor.isValidCoordinate(v) || sensor.onEdge(v))
                    continue
                if (sensor.getTerrainType(v) == TerrainType.PLAIN) {
                    if (v !in newLands)
                        newLands.add(v)
                    continue
                }
                val scoreV = score(v, attractor, repulsor)
                if (scoreV > maxScore) {
                    maxScore = scoreV
                    best = v
                }
            }
            if (best != null) {
                landmass.add(best)
                newLands.add(best)
                makeLand(best)
                toGenerate--
            }
        }
    }

    override fun getName(): String {
        return name
    }
}