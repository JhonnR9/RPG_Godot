package game.statemachine

import game.entities.Character

abstract class State (protected var owner: Character) {
    open fun onStateEnter(){}
    open fun onStateStay(delta: Double){}
    open fun onStateExit(){}

}