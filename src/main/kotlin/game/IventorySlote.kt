package game

import game.core.ConnectSignal
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.global.GD

@RegisterClass
class IventorySlote: Node() {

    @ConnectSignal("mouse_entered" )
    @RegisterFunction
    fun onMouseEntered(){
        GD.print("ok")
    }

}
