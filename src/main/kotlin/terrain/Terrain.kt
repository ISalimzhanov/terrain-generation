package terrain

class Terrain(val config: TerrainConfig) {
    val terrainTypeMap = mutableMapOf<Coordinate, TerrainType>()

    init {
        for (x in 0 until config.length)
            for (y in 0 until config.width)
                terrainTypeMap[Coordinate(x, y)] = TerrainType.WATER
    }

    companion object {
        data class TerrainConfig(
            val length: Int,
            val width: Int,
        )
    }
}