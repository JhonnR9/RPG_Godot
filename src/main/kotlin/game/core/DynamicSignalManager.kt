package game.core

import godot.Engine
import godot.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.*
import godot.global.GD
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.KFunction

@Target(AnnotationTarget.FUNCTION)
annotation class ConnectSignal(val signalName: String, val nodePath: String = "")

data class SignalData(
    val nodePath: String,
    val nodeRef: WeakReference<Node>
)

@RegisterClass
class DynamicSignalManager : Node() {

    private val signalMap = mutableMapOf<String, MutableList<SignalData>>()
    private val annotatedFunctionsMap = mutableMapOf<String, MutableList<Triple<WeakReference<Node>, String, String>>>()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val connectedSignals = mutableSetOf<String>()

    @RegisterFunction
    override fun _ready() {
        //getTree()?.root?.connect("child_entered_tree".toGodotName(), Callable(this, "onChildEnteredTree".toGodotName()))
        //getTree()?.root?.connect("child_exiting_tree".toGodotName(), Callable(this, "onChildExitingTree".toGodotName()))
        getTree()?.root?.forEachNode { node ->
            onChildEnteredTree(node)
        }
    }

    @RegisterFunction
    fun onChildEnteredTree(node: Node) {
        val nodePath = node.getPath().path

        node.getSignalList().forEach { signalDict ->
            val signalName = signalDict["name"].toString()

            val signalData = SignalData(nodePath, WeakReference(node))
            signalMap.computeIfAbsent(signalName) { mutableListOf() }.add(signalData)
        }

        getAnnotatedFunctions(node)
        startPendingConnections()
    }

    private fun startPendingConnections() {
        scope.launch {
            delay(50)

            callDeferred("connectCachedSignals".toGodotName())
        }
    }

    @RegisterFunction
    fun connectCachedSignals() {

        signalMap.forEach { (signalName, signalList) ->

            signalList.forEach { signalData ->
                val targetNode = signalData.nodeRef.get()
                if (targetNode != null) {
                    annotatedFunctionsMap[signalName]?.forEach { (receiverRef, methodName, targetPath) ->
                        val receiverNode = receiverRef.get()
                        if (receiverNode != null && (targetPath.isEmpty() || targetPath == signalData.nodePath)) {

                            val connectionKey = "${targetNode.getPath().path}-$signalName-$receiverNode.getPath().path-$methodName"


                            if (!connectedSignals.contains(connectionKey)) {

                                val callable = Callable(receiverNode, methodName.toGodotName())
                                targetNode.callDeferred("connect".toGodotName(), signalName.toGodotName(), callable)

                                connectedSignals.add(connectionKey)
                            }
                        }
                    }
                } else {

                    signalMap[signalName]?.remove(signalData)
                }
            }
        }
    }

    private fun getAnnotatedFunctions(node: Node) {

        node::class.members.forEach { function ->
            val annotation = function.findAnnotation<ConnectSignal>()
            if (annotation != null && function is KFunction<*>) {

                annotatedFunctionsMap.computeIfAbsent(annotation.signalName) { mutableListOf() }
                    .add(Triple(WeakReference(node), function.name, annotation.nodePath))
            }
        }
    }

    private fun Node.forEachNode(action: (Node) -> Unit) {
        action(this)
        for (i in 0 until getChildCount()) {
            getChild(i)?.forEachNode(action)
        }
    }

    @RegisterFunction
    fun onChildExitingTree(node: Node) {
        val nodePath = node.getPath().path
        node.getSignalList().forEach { signalDict ->
            val signalName = signalDict["name"].toString()

            val signalData = SignalData(nodePath, WeakReference(node))
            signalMap[signalName]?.remove(signalData)
        }

        disconnectAnnotatedFunctions(node)
    }

    private fun disconnectAnnotatedFunctions(node: Node) {

        node::class.members.forEach { function ->
            val annotation = function.findAnnotation<ConnectSignal>()
            if (annotation != null && function is KFunction<*>) {

                annotatedFunctionsMap[annotation.signalName]?.removeIf { (receiverRef, methodName, _) ->
                    val receiverNode = receiverRef.get()
                    receiverNode == node && methodName == function.name
                }
            }
        }


        signalMap.forEach { (signalName, signalList) ->
            signalList.forEach { signalData ->
                val targetNode = signalData.nodeRef.get()
                if (targetNode != null && targetNode == node) {
                    val connectionKey = "${targetNode.getPath().path}-$signalName"
                    connectedSignals.remove(connectionKey)

                }
            }
        }
    }

}
