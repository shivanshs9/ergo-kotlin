package headout.oss.ergo.utils

import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 21/05/20.
 */
fun TypeName.belongsToType(expectedType: TypeName): Boolean {
    return if (this is ParameterizedTypeName) rawType == expectedType else this == expectedType
}

fun <T : Any> TypeName.belongsToType(expectedKlazz: KClass<T>) = belongsToType(expectedKlazz.asTypeName())