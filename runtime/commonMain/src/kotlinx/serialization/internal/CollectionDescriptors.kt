/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.internal

import kotlinx.serialization.*

internal sealed class ListLikeDescriptor(val elementDesc: SerialDescriptor) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.LIST
    override val elementsCount: Int = 1
    
    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
        name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

    override fun isElementOptional(index: Int): Boolean {
        if (index != 0) throw IllegalStateException("List descriptor has only one child element, index: $index")
        return false
    }

    override fun getElementAnnotations(index: Int): List<Annotation> {
        if (index != 0) throw IndexOutOfBoundsException("List descriptor has only one child element, index: $index")
        return emptyList()
    }

    override fun getElementDescriptor(index: Int): SerialDescriptor {
        if (index != 0) throw IndexOutOfBoundsException("List descriptor has only one child element, index: $index")
        return elementDesc
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ListLikeDescriptor) return false
        if (elementDesc == other.elementDesc && serialName == other.serialName) return true
        return false
    }

    override fun hashCode(): Int {
        return elementDesc.hashCode() * 31 + serialName.hashCode()
    }
}

internal sealed class MapLikeDescriptor(
    override val serialName: String,
    val keyDescriptor: SerialDescriptor,
    val valueDescriptor: SerialDescriptor
) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.MAP
    override val elementsCount: Int = 2
    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
        name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid map index")

    override fun isElementOptional(index: Int): Boolean {
        if (index !in 0..1) throw IllegalStateException("Map descriptor has only two child elements, index: $index")
        return false
    }

    override fun getElementAnnotations(index: Int): List<Annotation> {
        if (index !in 0..1) throw IndexOutOfBoundsException("Map descriptor has only two child elements, index: $index")
        return emptyList()
    }

    override fun getElementDescriptor(index: Int): SerialDescriptor = when (index) {
        0 -> keyDescriptor
        1 -> valueDescriptor
        else -> throw IndexOutOfBoundsException("Map descriptor has only one child element, index: $index")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MapLikeDescriptor) return false

        if (serialName != other.serialName) return false
        if (keyDescriptor != other.keyDescriptor) return false
        if (valueDescriptor != other.valueDescriptor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serialName.hashCode()
        result = 31 * result + keyDescriptor.hashCode()
        result = 31 * result + valueDescriptor.hashCode()
        return result
    }
}

internal const val ARRAY_NAME = "kotlin.Array"
internal const val ARRAY_LIST_NAME = "kotlin.collections.ArrayList"
internal const val LINKED_HASH_SET_NAME = "kotlin.collections.LinkedHashSet"
internal const val HASH_SET_NAME = "kotlin.collections.HashSet"
internal const val LINKED_HASH_MAP_NAME = "kotlin.collections.LinkedHashMap"
internal const val HASH_MAP_NAME = "kotlin.collections.HashMap"

/**
 * Descriptor for primitive arrays, such as [IntArray], [DoubleArray], etc...
 *
 * Can be obtained from corresponding serializers (e.g. [ByteArraySerializer.descriptor])
 */
internal class PrimitiveArrayDescriptor internal constructor(
    primitive: SerialDescriptor
) : ListLikeDescriptor(primitive) {
    override val serialName: String = "${primitive.serialName}Array"
}

internal class ArrayClassDesc(elementDesc: SerialDescriptor) : ListLikeDescriptor(elementDesc) {
    override val serialName: String get() = ARRAY_NAME
}

internal class ArrayListClassDesc(elementDesc: SerialDescriptor) : ListLikeDescriptor(elementDesc) {
    override val serialName: String get() = ARRAY_LIST_NAME
}

internal class NamedListClassDescriptor(override val serialName: String, elementDescriptor: SerialDescriptor) :
    ListLikeDescriptor(elementDescriptor)

internal class LinkedHashSetClassDesc(elementDesc: SerialDescriptor) : ListLikeDescriptor(elementDesc) {
    override val serialName: String get() = LINKED_HASH_SET_NAME
}

internal class HashSetClassDesc(elementDesc: SerialDescriptor) : ListLikeDescriptor(elementDesc) {
    override val serialName: String get() = HASH_SET_NAME
}

internal class NamedMapClassDescriptor(name: String, keyDescriptor: SerialDescriptor, valueDescriptor: SerialDescriptor) :
    MapLikeDescriptor(name, keyDescriptor, valueDescriptor)

internal class LinkedHashMapClassDesc(keyDesc: SerialDescriptor, valueDesc: SerialDescriptor) :
    MapLikeDescriptor(LINKED_HASH_MAP_NAME, keyDesc, valueDesc)

internal class HashMapClassDesc(keyDesc: SerialDescriptor, valueDesc: SerialDescriptor) :
    MapLikeDescriptor(HASH_MAP_NAME, keyDesc, valueDesc)
