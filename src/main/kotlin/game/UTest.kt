package game

import godot.Input
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.Callable
import godot.core.StringName
import godot.core.signal0
import godot.core.toGodotName
import godot.global.GD

@RegisterClass
class UTest: Node() {
    @RegisterSignal
    val uiTest by signal0()

    @RegisterFunction
    override fun _process(delta: Double) {
        super._process(delta)
        if (Input.isActionJustPressed(StringName("Test"))) {
            uiTest.emitSignal()
        }
    }

}
