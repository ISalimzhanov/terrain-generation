package agents

import sensors.TerrainSensor
import terrain.Coordinate
import terrain.TerrainType
import java.util.*
import kotlin.math.abs
import kotlin.system.measureTimeMillis

class SmoothingAgent(
    private val sensor: TerrainSensor,
    private val config: SmoothingConfig
) : Agent {
    companion object {
        data class SmoothingConfig(
            val tokens: Int,
        )

        private fun vonNeumanSmoothing(sensor: TerrainSensor, coordinate: Coordinate, order: Int) {
            val queue: Queue<Coordinate> = LinkedList()
            queue.add(coordinate)
            var weightedSum = sensor.getHeight(coordinate) * (order + 1)
            var sumWeights = order + 1
            val used = mutableListOf<Coordinate>()
            val typeCount = mutableMapOf<TerrainType, Int>()
            while (!queue.isEmpty()) {
                val v = queue.remove()
                used.add(v)
                for (dir in TerrainSensor.directions) {
                    val to = Coordinate(v.x + dir.x, v.y + dir.y)
                    val d = abs(to.x - coordinate.x) + abs(to.y - coordinate.y) //Manhattan distance
                    if (!sensor.isValidCoordinate(to) || d > order)
                        continue
                    if (d != order && to !in used)
                        queue.add(to)
                    sumWeights += order - d + 1
                    if (typeCount[sensor.getTerrainType(to)] == null)
                        typeCount[sensor.getTerrainType(to)] = 0
                    else
                        typeCount[sensor.getTerrainType(to)] = order - d + 1
                    weightedSum += sensor.getHeight(to) * (order - d + 1)
                }
            }
            sensor.setHeight(coordinate, weightedSum / sumWeights)
            var maxCount = 0
            var terrainType = sensor.getTerrainType(coordinate)
            for (type in TerrainType.values()) {
                if (type == TerrainType.WATER)
                    continue
                if (typeCount[type] != null) {
                    val count = typeCount[type]!!
                    if (count > maxCount) {
                        maxCount = count
                        terrainType = type
                    }
                }
            }
            if (terrainType == TerrainType.WATER)
                sensor.setTerrainType(coordinate, terrainType)
        }

        fun smooth(sensor: TerrainSensor, startPoint: Coordinate, tokens: Int) {
            var location = startPoint
            for (i in 0 until tokens) {
                vonNeumanSmoothing(sensor, location, 2)
                for (dir in TerrainSensor.directions.shuffled()) {
                    val v = Coordinate(location.x + dir.x, location.y + dir.y)
                    if (sensor.isValidCoordinate(v)) {
                        location = v
                        break
                    }
                }
            }
        }
    }


    override fun generate() {
        val time = measureTimeMillis {
            for (x in 0 until sensor.getTerrainLength())
                for (y in 0 until sensor.getTerrainWidth()) {
                    val v = Coordinate(x, y)
                    smooth(sensor, v, config.tokens)
                }
        }
        println("Execution time in milliseconds: $time")
    }

    override fun getName(): String {
        return "Smoothing Agent"
    }
}