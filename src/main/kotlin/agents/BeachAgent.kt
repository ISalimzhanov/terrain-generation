package agents

import sensors.TerrainSensor
import terrain.Coordinate
import terrain.TerrainType
import java.util.*
import kotlin.random.Random

class BeachAgent(
    private val sensor: TerrainSensor,
    private val config: BeachConfig,
) : Agent {
    private val used = mutableListOf<MutableList<Boolean>>()

    init {
        for (x in 0 until sensor.getTerrainLength()) {
            used.add(mutableListOf())
            for (y in 0 until sensor.getTerrainWidth()) {
                used[x].add(false)
            }
        }
    }

    companion object {
        class BeachConfig(
            val sizeWalk: Int,
            val frustrationRange: Double,
            val beachHeight: Double,
        )
    }

    private fun makeBeach(location: Coordinate) {
        var height = config.beachHeight
        height += Random.nextDouble(-config.frustrationRange, config.frustrationRange) * height
        sensor.setTerrainType(location, TerrainType.SAND)
        sensor.setHeight(location, height)
    }

    fun randomCoastWalk(coast: Coordinate) {
        val queue: Queue<Coordinate> = LinkedList()
        queue.add(coast)
        var cnt = 0
        while (!queue.isEmpty()) {
            val v = queue.remove()
            cnt++
            if (cnt == config.sizeWalk)
                break
            makeBeach(v)
            for (dir in TerrainSensor.directions.shuffled()) {
                val to = Coordinate(v.x + dir.x, v.y + dir.y)
                if (!sensor.isValidCoordinate(to) || used[to.x][to.y])
                    continue
                val terrainType = sensor.getTerrainType(to)
                if (terrainType == TerrainType.WATER || terrainType == TerrainType.MOUNTAIN)
                    continue
                used[to.x][to.y] = true
                queue.add(to)
            }
        }
    }

    private fun isCoast(location: Coordinate): Boolean {
        if (sensor.getTerrainType(location) == TerrainType.WATER)
            return false
        for (dir in TerrainSensor.directions) {
            val v = Coordinate(location.x + dir.x, location.y + dir.y)
            if (sensor.getTerrainType(v) == TerrainType.WATER)
                return true
        }
        return false
    }

    override fun generate() {
        val coastline = mutableListOf<Coordinate>()
        for (x in 0 until sensor.getTerrainLength())
            for (y in 0 until sensor.getTerrainWidth()) {
                val location = Coordinate(x, y)
                if (isCoast(location)) {
                    coastline.add(location)
                }
            }
        for (coast in coastline) {
            if (!used[coast.x][coast.y]) {
                randomCoastWalk(coast)
            }
            sensor.setTerrainType(coast, TerrainType.COAST)
        }
    }

    override fun getName(): String {
        return "Beach Agent"
    }

}