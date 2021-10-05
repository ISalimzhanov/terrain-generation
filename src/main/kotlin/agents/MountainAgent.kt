package agents

import sensors.TerrainSensor
import terrain.Coordinate
import terrain.TerrainType
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class MountainAgent(
    private val sensor: TerrainSensor,
    private val config: MountainConfig
) : Agent {
    companion object {
        private val directions =
            mutableListOf(Coordinate(1, 1), Coordinate(1, -1), Coordinate(-1, -1), Coordinate(-1, 1))

        private fun distance(x: Double, y: Double): Double {
            return sqrt(x * 1.0 * x + y * y)
        }

        private fun calcHeight(
            center: Coordinate, location: Coordinate,
            mountainHeight: Double, stepness: Double
        ): Double {
            val d = distance(
                (location.x - center.x) * 1.0 / sqrt(mountainHeight),
                (location.y - center.y) * 1.0 / sqrt(mountainHeight)
            )
            return mountainHeight / (d.pow(stepness) + 1)
        }

        data class MountainConfig(
            val stepness: Double,
            val maxHeight: Double,
            val minHeight: Double,
            val directionSwitchFrequency: Int,
            val maxChainSize: Int,
            val tokens: Int,
        )
    }

    private fun isValidRegion(center: Coordinate): Boolean {
        if (!sensor.isValidCoordinate(center))
            return false
        return sensor.getTerrainType(center) != TerrainType.WATER && sensor.getHeight(center) < config.minHeight
    }

    private fun elevateWedge(center: Coordinate, mountainHeight: Double) {
        val regionLength = (mountainHeight).roundToInt()
        val minX = center.x - regionLength / 2
        val maxX = center.x + (regionLength + 1) / 2
        val minY = center.y - regionLength / 2
        val maxY = center.y + (regionLength + 1) / 2
        for (x in minX..maxX)
            for (y in minY..maxY) {
                val location = Coordinate(x, y)
                if (!isValidRegion(location) || sensor.getTerrainType(location) == TerrainType.WATER)
                    continue
                val height = calcHeight(
                    center, location,
                    mountainHeight, config.stepness
                )
                sensor.setHeight(location, height)
                sensor.setTerrainType(location, TerrainType.MOUNTAIN)
            }
    }

    override fun generate() {
        val landmass = mutableListOf<Coordinate>()
        for (x in 0 until sensor.getTerrainLength())
            for (y in 0 until sensor.getTerrainWidth()) {
                val location = Coordinate(x, y)
                if (sensor.getTerrainType(location) != TerrainType.WATER)
                    landmass.add(location)
            }
        for (i in 1..config.tokens) {
            var center = landmass.random()
            while (sensor.getTerrainType(center) == TerrainType.MOUNTAIN)
                center = landmass.random()
            val direction = directions.random()
            generate(
                center,
                direction,
                config.directionSwitchFrequency,
                Random.nextDouble(config.minHeight, config.maxHeight)
            )
        }
    }

    private fun generate(
        center: Coordinate,
        direction: Coordinate,
        directionSwitchTimer: Int,
        mountainHeight: Double,
        chainSize: Int = 0
    ) {
        if (chainSize == config.maxChainSize)
            return
        elevateWedge(center, mountainHeight)
        val newHeight = Random.nextDouble(config.minHeight, config.maxHeight)
        val regionLength = (newHeight / 2).roundToInt()
        var newCenter = Coordinate(
            center.x + direction.x * regionLength,
            center.y + direction.y * regionLength,
        )
        if (directionSwitchTimer == 0 || !isValidRegion(newCenter)) {
            val curDirectionIndex = directions.indexOf(direction)
            val newDirection = directions[(curDirectionIndex + 3) % 4]
            newCenter = Coordinate(
                center.x + newDirection.x * regionLength,
                center.y + newDirection.y * regionLength,
            )
            val check = isValidRegion(newCenter)
            if (check) {
                generate(newCenter, newDirection, config.directionSwitchFrequency, newHeight, chainSize + 1)
            }
        } else {
            generate(
                newCenter,
                direction,
                directionSwitchTimer - 1,
                newHeight,
                chainSize + 1
            )
        }
    }

    override fun getName(): String {
        return "Mountain Agent"
    }
}