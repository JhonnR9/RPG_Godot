package game.core

import godot.Error
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Callable
import godot.core.StringName
import godot.core.toGodotName
import godot.global.GD
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

/**
 * Annotation to mark functions that should be automatically connected to signals.
 * @property signalName The name of the signal to connect.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ConnectSignal(val signalName: String)

/**
 * DynamicSignalManager is responsible for automatically connecting and disconnecting signals in the Godot scene tree.
 * It listens for nodes entering and exiting the tree and dynamically manages their signal connections.
 */
@RegisterClass
class DynamicSignalManager : Node() {
    /**
     * Called when the node is ready. It initializes the signal connections for all existing children
     * and listens for new nodes being added or removed.
     */
    @RegisterFunction
    override fun _ready() {
        val root = getTree()?.root ?: return
        getAllChildren(root)

        root.connect("child_entered_tree".toGodotName(), Callable(this, "onChildEntered".toGodotName()))
        root.connect("child_exiting_tree".toGodotName(), Callable(this, "onChildExiting".toGodotName()))
    }

    /**
     * Recursively retrieves all children of a given node and connects their signals.
     * @param node The root node to start the search from.
     */
    private fun getAllChildren(node: Node?) {
        node?.getChildren()?.forEach { child ->
            connectSignals(child)
            getAllChildren(child)
        }
    }

    /**
     * Searches for nodes that emit a specific signal.
     * @param signalName The signal to look for.
     * @param node The root node to search within.
     * @return A list of nodes that have the specified signal.
     */
    private fun findNodeWithSignal(signalName: StringName, node: Node?): List<Node> {
        val nodesFounded = mutableListOf<Node>()
        node?.getChildren()?.forEach { child ->
            if (child.hasSignal(signalName)) {
                nodesFounded += child
            }
            nodesFounded += findNodeWithSignal(signalName, child)
        }
        return nodesFounded
    }

    /**
     * Connects functions marked with [ConnectSignal] to their respective signals.
     * @param target The node whose functions should be scanned for signal connections.
     */
    private fun connectSignals(target: Node) {
        val kClass = target::class

        kClass.memberFunctions.forEach { function ->
            function.findAnnotation<ConnectSignal>()?.let { annotation ->
                val signalName = annotation.signalName.toGodotName()
                val methodName = function.name.toGodotName()

                findNodeWithSignal(signalName, getTree()?.root).forEach {
                    val result = it.connect(signalName, Callable(target, methodName))
                    if (result == Error.OK) {
                        GD.print("Connected '${annotation.signalName}' to function '${function.name}' in ${target.name}")
                    } else {
                        GD.pushWarning("Failed to connect '${annotation.signalName}' in ${target.name} with result: $result")
                    }
                }
            }
        }
    }

    /**
     * Handles nodes entering the tree and connects their signals.
     * @param node The newly entered node.
     */
    @RegisterFunction
    fun onChildEntered(node: Node) {
        connectSignals(node)
    }

    /**
     * Handles nodes exiting the tree and disconnects their signals.
     * @param node The node that is about to be removed.
     */
    @RegisterFunction
    fun onChildExiting(node: Node) {
        disconnectSignals(node)
    }

    /**
     * Disconnects functions marked with [ConnectSignal] from their respective signals.
     * @param target The node whose signals should be disconnected.
     */
    private fun disconnectSignals(target: Node) {
        val kClass = target::class
        kClass.declaredMemberFunctions.forEach { function ->
            function.findAnnotation<ConnectSignal>()?.let { annotation ->
                val signalName = annotation.signalName.toGodotName()
                val methodName = function.name.toGodotName()

                if (target.hasSignal(signalName)) {
                    if (target.isConnected(signalName, Callable(target, methodName))) {
                        target.disconnect(signalName, Callable(target, methodName))
                        GD.print("Disconnected '${annotation.signalName}' from function '${function.name}' in ${target.name}")
                    }
                }
            }
        }
    }
}
