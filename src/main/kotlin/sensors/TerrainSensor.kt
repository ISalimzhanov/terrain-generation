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
        return terrain.terrainInfo[coordinate.x][coordinate.y].terrainType
    }

    fun setTerrainType(coordinate: Coordinate, terrainType: TerrainType) {
        if (!isValidCoordinate(coordinate))
            throw IndexOutOfBoundsException("not valid coordinate")
        terrain.terrainInfo[coordinate.x][coordinate.y].terrainType = terrainType
    }

    fun getHeight(coordinate: Coordinate): Double {
        if (!isValidCoordinate(coordinate))
            throw IndexOutOfBoundsException("not valid coordinate")
        return terrain.terrainInfo[coordinate.x][coordinate.y].height
    }

    fun setHeight(coordinate: Coordinate, height: Double) {
        if (!isValidCoordinate(coordinate))
            throw IndexOutOfBoundsException("not valid coordinate")
        terrain.terrainInfo[coordinate.x][coordinate.y].height = height
    }

    fun onEdge(v: Coordinate): Boolean {
        return v.x == 0 || v.y == 0 || v.x == terrain.config.length - 1 || v.y == terrain.config.width - 1
    }

    companion object {
        fun distance(a: Coordinate, b: Coordinate): Long {
            return (a.x - b.x) * 1L * (a.x - b.x) + (a.y - b.y) * 1L * (a.y - b.y)
        }

        val directions = listOf(Coordinate(0, 1), Coordinate(1, 0), Coordinate(0, -1), Coordinate(-1, 0))
    }
}