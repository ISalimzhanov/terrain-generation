package terrain

import com.fasterxml.jackson.annotation.JsonValue

data class Coordinate(
    val x: Int,
    val y: Int,
) {
    @Override
    @JsonValue
    override fun toString(): String {
        return "$x, $y"
    }
}