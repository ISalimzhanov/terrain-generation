package terrain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class Terrain(
    @JsonIgnore
    val config: TerrainConfig
) {
    @JsonProperty("terrain")
    val terrainTypeMap = mutableMapOf<Coordinate, TerrainType>()

    init {
        for (x in 0 until config.length)
            for (y in 0 until config.width)
                terrainTypeMap[Coordinate(x, y)] = TerrainType.WATER
    }

    @JsonIgnore
    fun getSquare(): Long {
        return config.length * 1L * config.width
    }

    companion object {
        data class TerrainConfig(
            val length: Int,
            val width: Int,
        )


    }
}