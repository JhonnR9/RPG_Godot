package game

import JsonDataParse
import game.core.ConnectSignal
import godot.Input
import godot.Node
import godot.ProjectSettings
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.StringName
import godot.core.signal1
import godot.global.GD


@RegisterClass
class UTest: Node() {
    @RegisterSignal
    val uiTest by signal1<Int>("number")

    @RegisterFunction
    override fun _ready() {
        super._ready()
        val json = JsonDataParse("src/main/resources/NPCs.json")
        GD.print(json.data.characters[0].name)
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        super._process(delta)
        if (Input.isActionJustPressed(StringName("Test"))) {
            uiTest.emitSignal(999)
        }
    }

}
