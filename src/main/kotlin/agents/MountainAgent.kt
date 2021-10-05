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
        private fun distance(x: Double, y: Double): Double {
            return sqrt(x * 1.0 * x + y * y)
        }

        private fun calcHeight(
            center: Coordinate, location: Coordinate,
            regionLength: Int, regionWidth: Int,
            mountainHeight: Double, stepness: Double
        ): Double {
            val d = distance((location.x - center.x) * 1.0 / regionLength, (location.y - center.y) * 1.0 / regionWidth)
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
        return sensor.isValidCoordinate(center) && sensor.getTerrainType(center) != TerrainType.WATER && sensor.getTerrainType(
            center
        ) != TerrainType.MOUNTAIN
    }

    private fun elevateWedge(center: Coordinate, mountainHeight: Double) {
        val regionLength = sqrt(mountainHeight).roundToInt() * 2
        val minX = center.x - regionLength / 2
        val maxX = center.x + (regionLength + 1) / 2
        val minY = center.y - regionLength / 2
        val maxY = center.y - (regionLength + 1) / 2
        for (x in minX..maxX)
            for (y in minY..maxY) {
                val location = Coordinate(x, y)
                val height = calcHeight(
                    center, location,
                    regionLength, regionLength,
                    mountainHeight, config.stepness
                )
                if(height > config.maxHeight){
                    print("WTF")
                }
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
            val direction = TerrainSensor.directions.random()
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
        elevateWedge(center, mountainHeight)
        if (chainSize == config.maxChainSize)
            return
        val newHeight = Random.nextDouble(config.minHeight, config.maxHeight)
        val regionLength = sqrt(newHeight).roundToInt()
        var newCenter = Coordinate(
            center.x + direction.x * regionLength / 2,
            center.y + direction.y * regionLength / 2,
        )
        if (directionSwitchTimer == 0 || !isValidRegion(newCenter)) {
            val curDirectionIndex = TerrainSensor.directions.indexOf(direction)
            for (i in 1..4 step 2) {
                val newDirection = TerrainSensor.directions[(curDirectionIndex + i) % 4]
                newCenter = Coordinate(
                    center.x + newDirection.x * regionLength,
                    center.y + newDirection.y * regionLength
                )
                if (isValidRegion(newCenter)) {
                    generate(newCenter, newDirection, config.directionSwitchFrequency, newHeight, chainSize + 1)
                    break
                }
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