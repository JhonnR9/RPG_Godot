package game.entities.player

import game.entities.Character
import game.statemachine.State
import godot.core.Vector2

class RunState(owner: Character): State(owner) {
    override fun onStateEnter() {
        super.onStateEnter()
        val player = owner as Player
        player.animationName="run"
    }
    override fun onStateStay(delta: Double) {
        super.onStateStay(delta)
        owner.velocity = owner.moveDirection.normalized() * owner.moveSpeed
    }

    override fun onStateExit() {
        super.onStateExit()
        owner.velocity = Vector2.ZERO
    }
}