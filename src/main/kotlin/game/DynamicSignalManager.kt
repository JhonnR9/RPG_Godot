package game

import godot.Error
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.Callable
import godot.core.StringName
import godot.global.GD
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

@Target(AnnotationTarget.FUNCTION)
annotation class ConnectSignal(val signalName: String)

@RegisterClass
class DynamicSignalManager : Node() {
    @RegisterFunction
    override fun _ready() {
        val root = getTree()?.root ?: return
        getAllChildren(root)

        root.connect(StringName("child_entered_tree"), Callable(this, StringName("_on_child_entered")))
        root.connect(StringName("child_exiting_tree"), Callable(this, StringName("_on_child_exiting")))
    }

    private fun getAllChildren(node: Node?) {
        node?.getChildren()?.forEach { child ->
            connectSignals(child)
            getAllChildren(child)

        }
    }

    private fun connectSignals(target: Node) {
        val kClass = target::class

        kClass.memberFunctions.forEach { function ->
            function.findAnnotation<ConnectSignal>()?.let { annotation ->
                val signalName = StringName(annotation.signalName)
                val methodName = StringName(function.name)

                if (!target.hasSignal(signalName)) {
                    GD.pushWarning("Signal '$signalName' not found in ${target.name}")
                    return@forEach
                }

                if (!target.hasMethod(methodName)) {
                    GD.pushWarning("Method '$methodName' not found in ${target.name}")
                    return@forEach
                }

                val result = target.connect(signalName, Callable(target, methodName))
                if (result == Error.OK) {
                    GD.print("Connected '${annotation.signalName}' to function '${function.name}' in ${target.name}")
                } else {
                    GD.pushWarning("Failed to connect '${annotation.signalName}' in ${target.name}")
                }
            }
        }
    }


    @RegisterFunction
    fun _on_child_entered(node: Node) {
        connectSignals(node)
    }

    @RegisterFunction
    fun _on_child_exiting(node: Node) {
        disconnectSignals(node)
    }

    private fun disconnectSignals(target: Node) {
        val kClass = target::class
        kClass.declaredMemberFunctions.forEach { function ->
            function.findAnnotation<ConnectSignal>()?.let { annotation ->
                val signalName = StringName(annotation.signalName)
                val methodName = StringName(function.name)

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
