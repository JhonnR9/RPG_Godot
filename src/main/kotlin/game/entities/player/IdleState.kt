package game.entities.player;
import game.entities.Character
import game.statemachine.State;
import godot.global.GD

class IdleState (owner: Character) : State(owner) {
    override fun onStateEnter() {
        super.onStateEnter()
        val player = owner as Player
        player.animationName="idle"
    }
}
