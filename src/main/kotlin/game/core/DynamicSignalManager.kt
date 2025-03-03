package game.core

import godot.Error
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Callable
import godot.core.NodePath
import godot.core.StringName
import godot.core.toGodotName
import godot.global.GD
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

@Target(AnnotationTarget.FUNCTION)
annotation class ConnectSignal(val signalName: String, val startFindNodePath: String = "",val isRecursive:Boolean = true)

@RegisterClass
class DynamicSignalManager : Node() {
    private val connectedSignals = mutableSetOf<Pair<String, String>>()

    @RegisterFunction
    override fun _ready() {
        val root = getTree()?.root ?: return
        getAllChildren(root)

        root.connect("child_entered_tree".toGodotName(), Callable(this, "onChildEntered".toGodotName()))
        root.connect("child_exiting_tree".toGodotName(), Callable(this, "onChildExiting".toGodotName()))
    }

    private fun getAllChildren(node: Node?) {
        node?.getChildren()?.forEach { child ->
            connectSignals(child)
            getAllChildren(child)
        }
    }

    private fun findNodeWithSignal(signalName: StringName, node: Node?, isRecursive: Boolean): List<Node> {
        val nodesFounded = mutableListOf<Node>()

        node?.let {
            if (it.hasSignal(signalName)) {
                nodesFounded.add(it)
            }
            if (!isRecursive){
                return nodesFounded
            }
            it.getChildren().forEach { child ->
                nodesFounded.addAll(findNodeWithSignal(signalName, child, true))
            }
        }

        return nodesFounded
    }

    private fun connectSignals(target: Node) {
        val kClass = target::class

        kClass.memberFunctions.forEach { function ->
            function.findAnnotation<ConnectSignal>()?.let { annotation ->
                val signalName = annotation.signalName.toGodotName()
                val methodName = function.name.toGodotName()
                val startFindNodePath = annotation.startFindNodePath
                val isRecursive = annotation.isRecursive

                val rootNode = if (startFindNodePath.isNotEmpty()) {
                    getTree()?.root?.getNode(NodePath(startFindNodePath))
                } else {
                    getTree()?.root
                }

                if (rootNode == null) {
                    GD.pushWarning("Path '$startFindNodePath' not found. Skipping signal connection.")
                    return@let
                }

                findNodeWithSignal(signalName, rootNode, isRecursive).forEach {

                    if (!connectedSignals.contains(signalName.toString() to "${methodName}_${target.objectID}")) {
                        if (!it.isConnected(signalName, Callable(target, methodName))) {
                            val result = it.connect(signalName, Callable(target, methodName))
                            if (result == Error.OK) {
                                GD.print("Connected '${annotation.signalName}' to function '${function.name}' in ${target.name}")

                                connectedSignals.add(signalName.toString() to "${methodName}_${target.objectID}")
                            } else {
                                GD.pushWarning("Failed to connect '${annotation.signalName}' in ${target.name} with result: $result")
                            }
                        }
                    }
                }
            }
        }
    }

    @RegisterFunction
    fun onChildEntered(node: Node) {
        connectSignals(node)
    }

    @RegisterFunction
    fun onChildExiting(node: Node) {
        disconnectSignals(node)
    }

    private fun disconnectSignals(target: Node) {
        val kClass = target::class
        kClass.declaredMemberFunctions.forEach { function ->
            function.findAnnotation<ConnectSignal>()?.let { annotation ->
                val signalName = annotation.signalName.toGodotName()
                val methodName = function.name.toGodotName()

                if (target.hasSignal(signalName) && target.isConnected(signalName, Callable(target, methodName))) {
                    target.disconnect(signalName, Callable(target, methodName))
                    GD.print("Disconnected '${annotation.signalName}' from function '${function.name}' in ${target.name}")

                    connectedSignals.remove(signalName.toString() to "${methodName}_${target.objectID}")
                }
            }
        }
    }
}
