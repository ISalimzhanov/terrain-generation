import agents.*
import com.fasterxml.jackson.databind.ObjectMapper
import sensors.TerrainSensor
import terrain.Terrain
import visual.Visualizer
import java.io.File
import kotlin.math.roundToInt

fun main(args: Array<String>) {
    val terrain = Terrain(Terrain.Companion.TerrainConfig(length = 512, width = 512))

    val sensor = TerrainSensor(terrain)

    val continentConfig = ContinentAgent.Companion.ContinentConfig((terrain.getSquare() * 0.4).toInt(), 500, 5.0)
    val smoothingConfig = SmoothingAgent.Companion.SmoothingConfig(4)
    val beachConfig = BeachAgent.Companion.BeachConfig(
        sizeWalk = (continentConfig.landmassSize * 0.005).roundToInt(),
        frustrationRange = 0.1,
        beachHeight = 1.0,
    )
    val mountainConfig = MountainAgent.Companion.MountainConfig(
        stepness = 5.0,
        maxHeight = 20.0,
        minHeight = 15.0,
        directionSwitchFrequency = 3,
        tokens = 1,
        maxChainSize = 20,
    )
    val riverConfig = RiverAgent.Companion.RiverConfig(
        tokens = 5,
        minLength = 50,
    )
    val beachAgent = BeachAgent(
        sensor = sensor,
        config = beachConfig
    )
    val agents = listOf(
        ContinentAgent(
            sensor = sensor,
            config = continentConfig,
        ),
        MountainAgent(
            sensor = sensor,
            config = mountainConfig,
        ),
        beachAgent,
        RiverAgent(
            sensor = sensor,
            beachAgent = beachAgent,
            config = riverConfig
        ),
        SmoothingAgent(
            sensor = sensor,
            config = smoothingConfig,
        ),
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