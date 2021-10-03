package terrain

enum class TerrainType(
    val id: Int,
) {
    WATER(0),
    PLAIN(1),
    SAND(2),
    MOUNTAIN(3),
    HILL(4),
}