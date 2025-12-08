package com.datavite.distrivite.presentation.ai

import android.graphics.Bitmap
import com.datavite.distrivite.presentation.ai.model.KnownFace
import com.datavite.distrivite.presentation.ai.model.FaceRecognitionResult
import com.google.mlkit.vision.face.Face
import javax.inject.Inject
import kotlin.math.sqrt

class FaceRecogniser @Inject constructor (
    private val faceEmbeddingProcessor:FaceEmbeddingProcessor,
    private val eulerThreshold: Float = 0.6f, // Threshold for determining if a face matches. The common practice is to use a Euclidean distance threshold of 0.6 for verification tasks.
    private val cosineThreshold: Float = 0.4f, // Threshold for determining if a face matches. A common threshold for cosine similarity is 0.4 to 0.5.
) {
    fun getFaceEmbeddingProcessor() = faceEmbeddingProcessor
    // Recognize a face and find the closest match from known faces
    fun recognize(image: Bitmap, face:Face, knownFaces: List<KnownFace>): FaceRecognitionResult {
        // Extract the embedding for the given face
        val embedding = faceEmbeddingProcessor.process(image)

        // Find the closest match among known faces
        val (bestKnownFace, bestMatchDistance) = findClosestMatch(embedding, knownFaces)

        // Determine if the best match is a valid recognition based on the threshold
        return if (bestMatchDistance < eulerThreshold) {
            FaceRecognitionResult(
                id = bestKnownFace.id,
                name = bestKnownFace.name,
                confidence = 1 - bestMatchDistance,
                face = face
            )
        } else {
            FaceRecognitionResult(
                id = "dr-42-liedjify-wenkack-sikam-josephine-secret-id",
                name = "Unknown",
                confidence = 0f,
                face = face,
                isUnknownFace = true
            )
        }
    }

    // Find the closest match among known faces based on cosine similarity
    private fun findClosestMatch(embedding: FloatArray, knownFaces: List<KnownFace>): Pair<KnownFace, Float> {
        var bestMatchFace = KnownFace(
            id = "",
            name = "Unknown",
            emptyList()
        )

        var bestMatchDistance = Float.MAX_VALUE

        for (knownFace in knownFaces) {
            val distances = mutableListOf<Float>()
            for (knownFaceEmbedding in knownFace.embeddings){
                distances.add(l2NormalizeEuclideanDistance(embedding, knownFaceEmbedding))
            }
            val distanceMean = distances.average().toFloat()
            if ( distanceMean < bestMatchDistance) {
                bestMatchDistance = distanceMean
                bestMatchFace = KnownFace(
                    knownFace.id,
                    name = knownFace.name,
                    embeddings = knownFace.embeddings
                )
            }
        }
        return Pair(bestMatchFace, bestMatchDistance)
    }

    private fun l2Normalize(embedding: FloatArray): FloatArray {
        // Calculate the L2 norm (Euclidean length) of the vector
        val norm = sqrt(embedding.sumOf { (it * it).toDouble() }).toFloat()

        // Return the normalized vector
        return if (norm > 0) {
            embedding.map { it / norm }.toFloatArray()
        } else {
            embedding // Return original vector if norm is zero to avoid division by zero
        }
    }

    private fun l2NormalizeCosineDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
        // Normalize the embeddings
        val normalized1 = l2Normalize(embedding1)
        val normalized2 = l2Normalize(embedding2)

        // Calculate the dot product of the normalized embeddings
        val dotProduct = normalized1.zip(normalized2).sumOf { (a, b) -> (a * b).toDouble() }

        // Calculate cosine similarity (since vectors are normalized, their magnitudes are 1)
        val cosineSimilarity = dotProduct.toFloat()

        // Return cosine distance
        return (1 - cosineSimilarity)
    }

    // Calculate the cosine distance between two embeddings
    private fun cosineDistanceWithNotNormalization(embedding1: FloatArray, embedding2: FloatArray): Float {
        // Calculate the dot product of the two embeddings
        val dotProduct = embedding1.zip(embedding2).sumOf { (a, b) -> (a * b).toDouble() }.toFloat()

        // Calculate the magnitude of the first embedding
        val magnitude1 = sqrt(embedding1.map { it * it }.sum())

        // Calculate the magnitude of the second embedding
        val magnitude2 = sqrt(embedding2.map { it * it }.sum())

        // Return the cosine distance
        return 1 - (dotProduct / (magnitude1 * magnitude2))
    }

    private fun l2NormalizeEuclideanDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
        // Normalize the embeddings
        val normalized1 = l2Normalize(embedding1)
        val normalized2 = l2Normalize(embedding2)

        // Calculate the sum of squared differences of normalized vectors
        val sumOfSquares = normalized1.zip(normalized2).sumOf { (a, b) -> ((a - b) * (a - b)).toDouble() }

        // Return the square root of the sum
        return sqrt(sumOfSquares).toFloat()
    }

    private fun euclideanDistanceWithNotNormalization(embedding1: FloatArray, embedding2: FloatArray): Float {
        // Calculate the sum of squared differences
        val sumOfSquares = embedding1.zip(embedding2).sumOf { (a, b) -> ((a - b) * (a - b)).toDouble() }
        // Return the square root of the sum
        return sqrt(sumOfSquares).toFloat()
    }
}
