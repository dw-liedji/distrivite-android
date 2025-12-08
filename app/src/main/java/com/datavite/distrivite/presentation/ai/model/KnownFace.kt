package com.datavite.distrivite.presentation.ai.model

// Assume that KnownFace is a data class representing a known person's face
data class KnownFace(val id:String, val name: String, val embeddings: List<FloatArray>) {
    override fun toString(): String {
        return "$name have ${embeddings.size} face ids "
    }
}
