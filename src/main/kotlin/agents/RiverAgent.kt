package agents

import sensors.TerrainSensor
import terrain.Coordinate
import terrain.TerrainType
import java.util.*
import kotlin.math.abs

class RiverAgent(
    private val sensor: TerrainSensor,
    private val beachAgent: BeachAgent,
    private val config: RiverConfig,
) : Agent {
    companion object {
        data class RiverConfig(
            val tokens: Int,
            val minLength: Int,
        )

        private fun d(a: Coordinate, b: Coordinate): Int {
            return abs(b.x - a.x) + abs(b.y - a.y)
        }
    }


    private fun generate(coast: Coordinate, mountain: Coordinate) {
        val queue: Queue<Coordinate> = LinkedList()
        queue.add(coast)
        val path = mutableListOf<Coordinate>()
        while (!queue.isEmpty()) {
            val v = queue.remove()
            if(v != coast)
                path.add(v)
            if (v == mountain)
                break
            for (dir in TerrainSensor.directions.shuffled()) {
                val to = Coordinate(v.x + dir.x, v.y + dir.y)
                if (!sensor.isValidCoordinate(to))
                    continue
                val terrainType = sensor.getTerrainType(to)
                if (terrainType != TerrainType.WATER && terrainType != TerrainType.MOUNTAIN &&
                    d(to, mountain) < d(v, mountain)
                ) {
                    queue.add(to)
                    break
                }
            }
        }
        for (point in path) {
            beachAgent.randomCoastWalk(point)
            sensor.setHeight(point, 0.0)
            sensor.setTerrainType(point, TerrainType.WATER)
        }
    }

    private fun isMountainBase(location: Coordinate): Boolean {
        for (dir in TerrainSensor.directions) {
            val to = Coordinate(location.x + dir.x, location.y + dir.y)
            val terrainType = sensor.getTerrainType(to)
            if (sensor.isValidCoordinate(to) && terrainType != TerrainType.WATER && terrainType != TerrainType.MOUNTAIN)
                return true
        }
        return false
    }

    override fun generate() {
        val coastline = mutableListOf<Coordinate>()
        val mountains = mutableListOf<Coordinate>()
        for (x in 0 until sensor.getTerrainLength())
            for (y in 0 until sensor.getTerrainWidth()) {
                val location = Coordinate(x, y)
                val terrainType = sensor.getTerrainType(location)
                if (terrainType == TerrainType.COAST)
                    coastline.add(location)
                if (terrainType == TerrainType.MOUNTAIN && isMountainBase(location))
                    mountains.add(location)
            }
        for (i in 1..config.tokens) {
            var coast = coastline.random()
            var mountain = mountains.random()
            while (d(coast, mountain) < config.minLength) {
                coast = coastline.random()
                mountain = mountains.random()
            }
            generate(coast, mountain)
        }
    }

    override fun getName(): String {
        return "River Agent"
    }
}