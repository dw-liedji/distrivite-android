package com.datavite.distrivite.data.remote.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

object NoneToNullStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NullableString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val raw = runCatching { decoder.decodeString() }.getOrNull()
        return when (raw?.lowercase()) {
            null, "", "none", "null" -> null
            else -> raw
        }
    }

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "None") // Optional: or encodeNull() if your backend supports true JSON null.
    }
}
