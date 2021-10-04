package terrain

import com.fasterxml.jackson.annotation.JsonValue

data class Coordinate(
    var x: Int,
    var y: Int,
) {
    @Override
    @JsonValue
    override fun toString(): String {
        return "$x, $y"
    }
}