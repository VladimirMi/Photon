package io.github.vladimirmi.photon.data.network

import com.squareup.moshi.*
import io.github.vladimirmi.photon.data.models.Tag
import io.realm.RealmList
import io.realm.RealmModel


/**
 * Created by Vladimir Mikhalev 04.06.2017.
 */

class RealmListJsonAdapter<T : RealmModel>
(val elementAdapter: JsonAdapter<T>) : JsonAdapter<RealmList<T>>() {

    companion object {
        val FACTORY: Factory = Factory { type, _, moshi ->
            val rawType: Class<*> = Types.getRawType(type)

            if (rawType == RealmList::class.java) {
                val elementType = Types.collectionElementType(type, RealmList::class.java)
                val elementAdapter = moshi.adapter<RealmModel>(elementType)
                return@Factory RealmListJsonAdapter(elementAdapter).nullSafe()
            }
            null
        }
    }

    override fun fromJson(reader: JsonReader): RealmList<T> {
        val result = RealmList<T>()
        reader.beginArray()
        while (reader.hasNext()) {
            result.add(elementAdapter.fromJson(reader))
        }
        reader.endArray()
        return result
    }

    override fun toJson(writer: JsonWriter, value: RealmList<T>) {
        writer.beginArray()
        for (element in value) {
            elementAdapter.toJson(writer, element)
        }
        writer.endArray()
    }
}

class TagJsonAdapter {
    @FromJson fun fromJson(tag: String): Tag {
        return Tag(tag)
    }

    @ToJson fun toJson(tag: Tag): String {
        return tag.tag
    }
}