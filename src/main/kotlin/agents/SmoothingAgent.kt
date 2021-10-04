package agents

import sensors.TerrainSensor
import terrain.Coordinate
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

        private fun vonNeumannAverageHeight(sensor: TerrainSensor, coordinate: Coordinate, order: Int): Double {
            val queue: Queue<Coordinate> = LinkedList()
            queue.add(coordinate)
            var weightedSum = sensor.getHeight(coordinate) * (order + 1)
            var sumWeights = order + 1
            val used = mutableListOf<Coordinate>()
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
                    weightedSum += sensor.getHeight(to) * (order - d + 1)
                }
            }
            return weightedSum / sumWeights
        }

        fun smooth(sensor: TerrainSensor, startPoint: Coordinate, tokens: Int) {
            var location = startPoint
            for (i in 0 until tokens) {
                val averageHeight = vonNeumannAverageHeight(sensor, location, 2)
                sensor.setHeight(location, averageHeight)
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