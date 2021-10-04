package visual

import org.jzy3d.analysis.AbstractAnalysis
import org.jzy3d.analysis.AnalysisLauncher
import org.jzy3d.chart.factories.EmulGLChartFactory
import org.jzy3d.colors.Color
import org.jzy3d.colors.ColorMapper
import org.jzy3d.colors.colormaps.ColorMapRainbow
import org.jzy3d.maths.Range
import org.jzy3d.plot3d.builder.Func3D
import org.jzy3d.plot3d.builder.SurfaceBuilder
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid
import org.jzy3d.plot3d.rendering.canvas.Quality
import sensors.TerrainSensor
import terrain.Coordinate
import kotlin.math.roundToInt

class Visualizer(
    private val sensor: TerrainSensor,
) : AbstractAnalysis(EmulGLChartFactory()) {
    override fun init() {
        val func = Func3D { x, y ->
            sensor.getHeight(Coordinate(x.roundToInt(), y.roundToInt()))
        }
        val range = Range(0F, (sensor.getTerrainLength() - 1).toFloat())

        val surface = SurfaceBuilder().orthonormal(OrthonormalGrid(range, 80), func)

        surface.colorMapper = ColorMapper(ColorMapRainbow(), surface, Color(255f, 0f, 255f, 255f))
        surface.faceDisplayed = true
        surface.wireframeDisplayed = true

        val quality = Quality.Advanced()

        chart = EmulGLChartFactory().newChart(quality)
        chart.add(surface)
        chart.startAnimation()
    }

    fun visualize() = AnalysisLauncher.open(this)
}