package game

import JsonDataParse
import game.repository.Repository
import godot.Input
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.StringName
import godot.core.signal1
import godot.global.GD
import kotlinx.serialization.json.Json


@RegisterClass
class UTest: Node() {
    @RegisterSignal
    val uiTest by signal1<Int>("number")

    @RegisterFunction
    override fun _ready() {
        super._ready()
       /* val json = JsonDataParse("src/main/resources/NPCs.json")
        val file = Json.encodeToString(json.data)
        val repository = Repository()
        repository.save(file)
        val text = repository.getData()
        GD.print(text)*/
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        super._process(delta)
        if (Input.isActionJustPressed(StringName("Test"))) {
            uiTest.emitSignal(999)
        }
    }

}
