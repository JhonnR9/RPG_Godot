package game.entities.player;
import game.entities.Character
import game.core.statemachine.State;

class IdleState (owner: Character) : State(owner) {
    override fun onStateEnter() {
        super.onStateEnter()
        val player = owner as Player
        player.animationName="idle"
    }
}
