package game.models

import kotlinx.serialization.Serializable

@Serializable
data class Choice(val id: Int, val text: String, val next:Int)
@Serializable
data class Dialog(val id: Int, val text:String, val choices: List<Choice>)

@Serializable
data class NPCModel(val id:Int, val name:String,val age:Int,val  birthday: String, val dialogs: List<Dialog>)

@Serializable
data class Data(val characters: List<NPCModel>)