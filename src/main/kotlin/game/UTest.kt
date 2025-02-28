package game

import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.signal0
import godot.global.GD

@RegisterClass
class UTest: Node() {
    @RegisterSignal
    val ui_test by signal0()


    @RegisterFunction
    override fun _ready() {
        ui_test.emit()
    }

}
