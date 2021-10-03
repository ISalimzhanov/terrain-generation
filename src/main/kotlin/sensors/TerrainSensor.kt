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
}