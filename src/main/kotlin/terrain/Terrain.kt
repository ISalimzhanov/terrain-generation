package terrain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class Terrain(
    @JsonIgnore
    val config: TerrainConfig
) {
    @JsonProperty("terrain")
    val terrainInfo = mutableListOf<MutableList<PointInfo>>()

    init {
        for (x in 0 until config.length) {
            terrainInfo.add(mutableListOf())
            for (y in 0 until config.width) {
                terrainInfo[x].add(PointInfo(TerrainType.WATER, 0.0))
            }
        }
    }

    @JsonIgnore
    fun getSquare(): Long {
        return config.length * 1L * config.width
    }

    @JvmName("getConfig1")
    @JsonProperty("terrainConfig")
    private fun getConfig(): TerrainConfig {
        return config
    }

    companion object {
        data class TerrainConfig(
            @JsonProperty("length")
            val length: Int,
            @JsonProperty("width")
            val width: Int,
        )

        data class PointInfo(
            @JsonProperty("terrainType")
            var terrainType: TerrainType,
            @JsonProperty("height")
            var height: Double,
        )
    }
}