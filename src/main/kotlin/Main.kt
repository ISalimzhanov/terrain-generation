import agents.BeachAgent
import agents.CoastlineAgent
import agents.MountainAgent
import agents.SmoothingAgent
import com.fasterxml.jackson.databind.ObjectMapper
import sensors.TerrainSensor
import terrain.Terrain
import visual.Visualizer
import java.io.File
import kotlin.math.roundToInt

fun main(args: Array<String>) {
    val terrain = Terrain(Terrain.Companion.TerrainConfig(length = 500, width = 500))

    val sensor = TerrainSensor(terrain)

    val coastlineConfig = CoastlineAgent.Companion.CoastlineConfig((terrain.getSquare() * 0.4).toInt(), 500, 5.0)
    val smoothingConfig = SmoothingAgent.Companion.SmoothingConfig(4)
    val beachConfig = BeachAgent.Companion.BeachConfig(
        mountainHeightLimit = 100.0,
        sizeWalk = (coastlineConfig.landmassSize * 0.005).roundToInt(),
        frustrationRange = 0.1,
        smoothingConfig = smoothingConfig,
        beachHeight = 3.0,
    )
    val mountainConfig = MountainAgent.Companion.MountainConfig(
        stepness = 3.5,
        maxHeight = 100.0,
        minHeight = 60.0,
        directionSwitchFrequency = 1,
        tokens = 4,
        maxChainSize = 1000,
    )
    val agents = listOf(
        CoastlineAgent(
            sensor = sensor,
            config = coastlineConfig,
        ),
        MountainAgent(
            sensor = sensor,
            config = mountainConfig,
        ),
        BeachAgent(
            sensor = sensor,
            config = beachConfig
        ),
        SmoothingAgent(
            sensor = sensor,
            config = smoothingConfig,
        )
    )
    for (agent in agents) {
        println("${agent.getName()} start generation")
        agent.generate()
        println("${agent.getName()} finished generation")
    }
    val mapper = ObjectMapper()
    mapper.writeValue(File("terrain.json"), terrain)
    val visualizer = Visualizer(sensor)
    visualizer.visualize()
}