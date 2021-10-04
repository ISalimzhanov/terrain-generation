import agents.Agent
import agents.CoastlineAgent
import com.fasterxml.jackson.databind.ObjectMapper
import sensors.TerrainSensor
import terrain.Terrain
import java.io.File

fun main(args: Array<String>) {
    val terrain = Terrain(Terrain.Companion.TerrainConfig(length = 100, width = 100))

    val sensor = TerrainSensor(terrain)

    val agents = listOf<Agent>(
        CoastlineAgent(
            sensor = sensor,
            config = CoastlineAgent.Companion.CoastlineConfig((terrain.getSquare() * 0.45).toInt(), 150),
        )
    )
    for (agent in agents) {
        println("${agent.getName()} start generation")
        agent.generate()
        println("${agent.getName()} finished generation")
    }
    val mapper = ObjectMapper()
    mapper.writeValue(File("terrain.json"), terrain)
}