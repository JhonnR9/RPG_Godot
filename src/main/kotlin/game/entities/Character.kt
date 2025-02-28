package game.entities

import game.entities.player.IdleState
import game.entities.player.RunState
import godot.CharacterBody2D
import godot.annotation.RegisterClass
import game.core.statemachine.StateMachine
import godot.annotation.RegisterFunction
import godot.core.Vector2

@RegisterClass
open class Character: CharacterBody2D() {
    var moveDirection: Vector2 = Vector2.ZERO
    protected val rootStateMachine = StateMachine()
    var moveSpeed = 30f

    enum class MovementState{
        IDLE, RUN
    }

    init {
        rootStateMachine.addState(MovementState.IDLE, IdleState(this))
        rootStateMachine.addState(MovementState.RUN, RunState(this))
    }
    @RegisterFunction
    override fun _ready() {
        super._ready()

        rootStateMachine.setInitialState(MovementState.IDLE)
        rootStateMachine.addTransition(MovementState.IDLE, MovementState.RUN) {moveDirection.length() > 0.01f}
        rootStateMachine.addTransition(MovementState.RUN, MovementState.IDLE) {moveDirection.length() < 0.01f}

    }

    @RegisterFunction
    override fun _process(delta: Double){
        super._process(delta)
        rootStateMachine.updateMachine(delta)
    }
}