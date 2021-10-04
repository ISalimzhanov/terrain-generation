package sensors

import terrain.Coordinate
import terrain.Terrain
import terrain.TerrainType

class TerrainSensor(private val terrain: Terrain) {
    fun getTerrainLength(): Int {
        return terrain.config.length
    }

    fun getTerrainWidth(): Int {
        return terrain.config.width
    }

    fun isValidCoordinate(coordinate: Coordinate): Boolean {
        return coordinate.x in (0 until terrain.config.length) && coordinate.y in (0 until terrain.config.width)
    }

    fun isValidCoordinate(
        minX: Int, maxX: Int,
        minY: Int, maxY: Int,
        coordinate: Coordinate
    ): Boolean {
        return coordinate.x in (minX..maxX) && coordinate.y in (minY..maxY)
    }

    fun getTerrainType(coordinate: Coordinate): TerrainType {
        if (!isValidCoordinate(coordinate))
            throw IndexOutOfBoundsException("not valid coordinate")
        return terrain.terrainTypeMap[coordinate]!!
    }

    fun setTerrainType(coordinate: Coordinate, terrainType: TerrainType) {
        if (!isValidCoordinate(coordinate))
            throw IndexOutOfBoundsException("not valid coordinate")
        terrain.terrainTypeMap[coordinate] = terrainType
    }

    companion object {
        fun distance(a: Coordinate, b: Coordinate): Long {
            return (a.x - b.x) * 1L * (a.x - b.x) + (a.y - b.y) * 1L * (a.y - b.y)
        }

        val directions = listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(0, -1), Coordinate(-1, 0))
    }
}