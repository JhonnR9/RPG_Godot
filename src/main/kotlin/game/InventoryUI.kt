package game

import godot.Control
import godot.PanelContainer
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import  game.core.ConnectSignal
import godot.Node

import godot.core.Callable
import godot.core.NodePath
import godot.core.toGodotName
import godot.global.GD

@RegisterClass
class InventoryUi: Control() {
    private val slots: MutableList<PanelContainer> = mutableListOf()

    @RegisterFunction
    override fun _ready() {
        getChildren().forEach(){
            if ( it is PanelContainer){
                slots.add(it)

            }
        }
        //GD.print(getPath().path)
    }



}
