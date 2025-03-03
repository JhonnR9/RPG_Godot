package game.entities.player

import game.entities.Character
import godot.AnimatedSprite2D
import godot.Input
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.NodePath
import godot.core.StringName
import  game.core.ConnectSignal
import godot.global.GD


@RegisterClass
class Player : Character() {
    private var sprite: AnimatedSprite2D? = null
    var animationName:String = "down"

    @RegisterFunction
    override fun _ready() {
        super._ready()
        sprite = getNode(NodePath("Sprite")) as AnimatedSprite2D
    }

    @RegisterFunction
    @ConnectSignal("ui_test", "/root/World/Node2D", isRecursive = false)
    fun Test(number: Int){
        GD.print("Funcionou uhuuu $number")
    }



    @RegisterFunction
    override fun _process(delta: Double) {
        super._process(delta)
        updateMoveDirection()
        setAnimation(animationName)
        moveAndSlide()
    }
    private fun updateMoveDirection (){
        moveDirection = Input.getVector(
            StringName("run_left"),
            StringName("run_right"),
            StringName("run_up"),
            StringName("run_down")
        )
    }

    private fun setAnimation(name: String) {
        val newAnimationName = StringName("${name}_${getLastMoveDirectionName()}")
        if (newAnimationName != sprite?.animation) {
            if (sprite?.animation != newAnimationName) {
                sprite?.setAnimation(newAnimationName)
                sprite?.play()
            }
        }

    }

    private fun getLastMoveDirectionName(): String {
        return when {
            moveDirection.x > 0.1 -> "right"
            moveDirection.x < -0.1 -> "left"
            moveDirection.y > 0.1 -> "down"
            moveDirection.y < -0.1 -> "up"
            else -> sprite?.animation.toString().substringAfter('_')
        }
    }
}
