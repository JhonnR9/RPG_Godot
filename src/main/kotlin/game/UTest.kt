package game

import godot.Input
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.Callable
import godot.core.StringName
import godot.core.signal1
import godot.core.toGodotName
import godot.global.GD

@RegisterClass
class UTest: Node() {
    @RegisterSignal
    val uiTest by signal1<Int>("number")

    @RegisterFunction
    override fun _process(delta: Double) {
        super._process(delta)
        if (Input.isActionJustPressed(StringName("Test"))) {
            uiTest.emitSignal(999)
        }
    }

}
