package terrain

import com.fasterxml.jackson.annotation.JsonProperty

enum class TerrainType {
    @JsonProperty("WATER")
    WATER,

    @JsonProperty("PLAIN")
    PLAIN,

    @JsonProperty("SAND")
    SAND,

    @JsonProperty("COAST")
    COAST,

    @JsonProperty("MOUNTAIN")
    MOUNTAIN,

    @JsonProperty("HILL")
    HILL,
}