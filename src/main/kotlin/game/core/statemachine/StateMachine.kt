package game.core.statemachine

import godot.global.GD

class StateMachine {
    private val states: MutableMap<Enum<*>, State> = mutableMapOf()
    private var currentState: State? = null
    private var parent: StateMachine? = null

    private val children: MutableList<StateMachine> = mutableListOf()
    private val transitions: MutableList<Transition> = mutableListOf()

    private data class Transition(
        val from: Enum<*>,
        val to: Enum<*>,
        val condition: () -> Boolean
    )

    fun addState(key: Enum<*>, state: State) {
        states.putIfAbsent(key, state)
    }

    fun setInitialState(key: Enum<*>) {
        states[key]?.let {
            currentState = it
            currentState?.onStateEnter()
        } ?: GD.print("Initial state $key not found!")
    }

    fun changeState(key: Enum<*>) {
        states[key]?.let { newState ->
            if (currentState != newState) {
                currentState?.onStateExit()
                currentState = newState
                currentState?.onStateEnter()
            }
        } ?: GD.print("State $key not found!")
    }

    fun addChild(child: StateMachine) {
        child.parent = this
        children.add(child)
    }

    fun removeChild(child: StateMachine) {
        children.remove(child)
        child.parent = null
    }

    fun removeState(key: Enum<*>) {
        states.remove(key)
    }

    private fun getState(): State? = currentState

    private fun getAllActiveStates(): List<State> {
        return listOfNotNull(currentState) + children.mapNotNull { it.getState() }
    }

    fun addTransition(from: Enum<*>, to: Enum<*>, condition: () -> Boolean) {
        transitions.add(Transition(from, to, condition))
    }

    fun updateMachine(delta: Double) {
        children.forEach { it.updateMachine(delta) }
        if (currentState == null) return

        val currentKey = states.entries.find { it.value == currentState }?.key

        transitions.firstOrNull {
            it.from == currentKey && it.condition()
        }?.let {
            changeState(it.to)
        }

        val activeStates = getAllActiveStates()

        activeStates.forEach {
            it.onStateStay(delta)
        }

    }

}
