package com.datavite.distrivite.domain.model
import com.google.mlkit.vision.face.Face

data class DomainFaceRecognition(
    val face: Face,
    val embeddings: FloatArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DomainFaceRecognition

        if (face != other.face) return false
        return embeddings.contentEquals(other.embeddings)
    }

    override fun hashCode(): Int {
        var result = face.hashCode()
        result = 31 * result + embeddings.contentHashCode()
        return result
    }
}